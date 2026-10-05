# FutureKawa Backend (siège) - MSPR2

Backend Spring Boot du **siège** de la chaîne FutureKawa. Il **agrège les backends des pays producteurs** (Brésil, Colombie,
Équateur…) en appelant leur API REST, authentifie les utilisateurs (JWT, rôles) et expose au **front** une API unique :
lots, mouvements de stock, mesures des capteurs, alertes, référentiel et reporting consolidé.

Le siège ne stocke que ses **utilisateurs** (PostgreSQL) ; toutes les données métier restent dans les backends des pays
(projet `backend_pays`).

```
ESP32 ──MQTT──▶ backend pays ──┐
                (un par pays)  ├──REST──▶ backend siège ◀── front
                               │              │
                               ┘          PostgreSQL (utilisateurs)
```

## 1) Règles métier

| Sujet | Règle |
|---|---|
| Pays | Un pays est désigné par son nom (`bresil`, `Colombie`…), **sans tenir compte de la casse ni des espaces autour**. Les pays sont ceux de `app.country-adapters.mapping` (BRESIL, COLOMBIE, EQUATEUR). |
| Lectures agrégées | Sans pays précisé, le siège interroge **tous les pays en parallèle** et fusionne les réponses. **Un pays en erreur ou injoignable est ignoré** : il ne bloque pas les autres. |
| Lots | `GET /lots` renvoie les lots du pays demandé (ou de tous si `pays` est absent **ou inconnu**), **du plus anciennement stocké au plus récent**, les lots sans date de stockage en dernier. |
| FIFO | Le lot à expédier en premier est le **plus ancien de tous les pays** (à date égale, le dernier pays interrogé l'emporte). Avec un pays inconnu : aucun lot. |
| Quantité | `GET /api/lot/total` additionne les quantités en stock ; un pays en erreur compte pour 0 ; aucune quantité → `404`. |
| Reporting global | Lots et mouvements sont **additionnés** ; âge moyen et taux de conformité sont **pondérés par le nombre de lots** de chaque pays. |
| Reporting qualité | Alertes et lots non conformes sont additionnés ; le taux de conformité est une **moyenne glissante** des taux non nuls (exacte pour 2 pays, voir « Évolutions recommandées »). Avec `pays` : le rapport de ce pays seul. |
| Reporting stock | **Un seul pays**, obligatoire. |
| Écritures | Création, changement de statut, transfert de zone et sortie de stock sont **relayés au pays désigné** par `pays` (obligatoire). Les règles métier (transfert vers la même zone, statuts…) sont celles du pays : le siège ne les revalide pas. |
| Alertes | L'identifiant d'un entrepôt est unique : `GET /alertes` interroge tous les pays, seul le sien répond. |

## 2) Stack technique

Java 21 · Spring Boot 4.0 (Web MVC, Security, Data JPA, WebFlux `WebClient`) · PostgreSQL 15 · JJWT · MapStruct · Lombok ·
springdoc-openapi 3 · Maven · Docker / Docker Compose.
Tests : JUnit 6, Mockito, AssertJ, ArchUnit, H2, JaCoCo.

## 3) Architecture hexagonale

```
 front ──REST──▶ [ api ] ───▶ [ domain ] ◀─── [ infrastructure ] ───▶ backends pays (REST) · PostgreSQL
                  contrôleurs    services        clients HTTP des pays
                  DTO, mappers   ports           persistance, jetons JWT
                  sécurité       modèles
                                 (sans framework)
```

`api` et `infrastructure` dépendent du domaine ; ils ne se connaissent pas et le domaine ne les connaît pas.

### Arborescence

```
src/main/java/com/backendSiege
├── BackendSiegeApplication.java
├── api/                          ← adaptateur entrant REST
│   ├── controller/               contrôleurs (…Controller) : reçoivent des DTO, appellent un service, renvoient des DTO
│   ├── dto/  (+ auth/)           DTO de requête / réponse (records) = contrat de l'API
│   ├── mapper/                   MapStruct : modèles du domaine <-> DTO
│   ├── exception/                traduction des erreurs métier en réponses HTTP
│   ├── security/                 SecurityConfig (rôles par route), JwtAuthenticationFilter, CustomUserDetailsService
│   └── config/                   OpenApiConfig (documentation Swagger)
├── domain/                       ← le cœur : ne dépend de rien (ni Spring, ni JPA, ni Jackson, ni Reactor)
│   ├── models/                   Lot, Historique, Mesure, Alerte, Pays, User… + règles (Lot.lePlusAncien, ReportingGlobal.consolider…)
│   ├── ports/                    interfaces vers l'extérieur : InventairePort, MesurePort, ReportingPort, UserPort, TokenPort…
│   ├── services/                 cas d'usage : InventaireService, ReportingService, AuthService, PaysService…
│   └── exceptions/               erreurs métier (PaysIndisponibleException, PaysNonSupporteException, EmailDejaUtiliseException…)
└── infrastructure/               ← adaptateurs sortants et technique
    ├── config/                   DomainConfig (assemblage des services), PaysProperties, JwtProperties, PaysClientsConfig
    ├── client/                   adaptateurs REST vers les backends pays
    │   ├── PaysClient(s)         client HTTP d'un pays (appels tolérants / stricts) et registre des pays
    │   ├── adapter/              réalisent les ports (InventaireAdapter, MesureAdapter, ReportingAdapter…)
    │   ├── dto/                  contrat des API des backends pays (…PaysDto)
    │   └── mapper/               DTO des pays <-> domaine
    ├── persistence/              entity/ (UserEntity) · repository/ (Spring Data) · mapper/ · adapter/ (UserAdapter)
    └── security/                 JwtTokenAdapter (jetons), PasswordEncoderAdapter (BCrypt)
```

### Règles à respecter (vérifiées par `ArchitectureTest`)

- Le **domaine** n'importe ni `org.springframework`, ni `jakarta`, ni Jackson, ni MapStruct, ni Reactor, ni `api`, ni `infrastructure`.
  Ses services sont de simples classes Java, instanciées par `infrastructure/config/DomainConfig`.
- **`api`** et **`infrastructure`** dépendent du domaine mais jamais l'un de l'autre.
- Un contrôleur ne connaît que des **services** et des **mappers** (jamais un port ni un adaptateur).
- Le client HTTP réactif (`WebClient`, Reactor) n'existe que dans `infrastructure/client` : la **logique métier** (fusion, tri,
  consolidation des rapports, choix du pays) est dans le domaine, les adaptateurs ne font que **appeler les pays et traduire**.
- Les **entités JPA** n'existent que dans `infrastructure/persistence/entity`.
- Chaque adaptateur réalise un **port** du domaine ; les **DTO** de l'API et ceux des pays sont distincts des modèles du domaine.

### Flux principaux

**Lecture agrégée** — `GET /lots`
1. `InventaireController` appelle `InventaireService.getAllLots(pays)`.
2. `PaysService` désigne les pays à interroger (celui demandé, sinon tous).
3. `InventaireAdapter` appelle `GET lots/allLotsFromAllEntrepots` sur chaque pays **en parallèle** (`PaysClient`) et traduit les réponses (`PaysMapper`).
4. Le service trie les lots ; le contrôleur les traduit en DTO.

**Écriture** — `PUT /lot/{idLot}/zones?pays=…` : `InventaireService` vérifie que le pays existe, puis `InventaireAdapter` relaie `PUT lots/{idLot}/zone` à ce pays seul.

**Authentification** — `JwtAuthenticationFilter` lit `Authorization: Bearer …`, `AuthService.authenticate` identifie l'utilisateur (`TokenPort` + `UserPort`),
`SecurityConfig` applique les rôles de la route.

## 4) Utilisateurs et rôles

Table `users` (+ `user_roles`) créée par `postgres/init_siege.sql` puis complétée par Hibernate (`ddl-auto: update`). Utilisateurs de test, mot de passe **`password123`** :

| E-mail | Rôle | Pays |
|---|---|---|
| `admin@siege.fr` | `ROLE_ADMIN` | — |
| `boss@siege.fr` | `ROLE_HQ` | — |
| `qualite@siege.fr` | `ROLE_QUALITY` | — |
| `entrepot@siege.fr` | `ROLE_WAREHOUSE_MANAGER` | colombie |
| `ops@siege.fr` | `ROLE_FARM_MANAGER` | colombie |
| `supply@siege.fr` | `ROLE_SUPPLY_CHAIN` | colombie |

**On ne devient jamais administrateur par l'API** : `POST /api/auth/register` refuse `ROLE_ADMIN` (règle de `AuthService`, vérifiée dans le domaine
pour tous les points d'entrée). Un administrateur se crée directement en base (comme dans `init_siege.sql`).

Le jeton JWT (HS512, 24 h) est renvoyé par `POST /api/auth/login` (et déposé dans le cookie HttpOnly `token`) ; il s'envoie en `Authorization: Bearer <jeton>`.

## 5) Configuration

Tout se règle par variables d'environnement (valeurs par défaut dans `src/main/resources/application.yml`).

| Variable | Défaut | Rôle |
|---|---|---|
| `DB_HOST` / `DB_PORT` / `DB_USER` / `DB_PASSWORD` | `localhost` / `5434` / `admin_siege` / `password_siege` | PostgreSQL du siège (base `db_siege_mondial`) |
| `URL_BRESIL` | `http://localhost:8080/api/` | API du backend du Brésil |
| `URL_COLOMBIE` | `http://localhost:8079/api/` | API du backend de la Colombie |
| `URL_EQUATEUR` | `http://localhost:8078/api/` | API du backend de l'Équateur |
| `TEMPERATURE_IDEALE` / `HUMIDITE_IDEALE` / `TOLERANCE_TEMPERATURE` / `TOLERANCE_HUMIDITE` | selon le pays | Plage climatique exposée par `GET /countries` (le même nom de variable s'applique aux 3 pays) |
| `JWT_SECRET` | clé historique du dépôt | Clé de signature des jetons (64 caractères au minimum). **À définir en production** ; la changer invalide les jetons déjà délivrés. |
| `SERVER_PORT` | `8080` | Port d'écoute (8085 dans `docker-compose.yml`) |

Les pays se déclarent sous `app.country-adapters.mapping.<PAYS>` (`url`, `tolerences.*`) : ajouter un pays = ajouter une entrée, sans code.

## 6) Lancement local

Prérequis : Java 21, Maven 3.9+, Docker.

```powershell
docker compose up -d db-siege                 # PostgreSQL du siège (5434) avec les utilisateurs de test
$env:SERVER_PORT = "8085"                     # 8080 est déjà pris par le backend du Brésil dans la configuration par défaut
mvn spring-boot:run
```

Pour toute la chaîne (3 pays, siège, front) : `docker compose up -d` ; le front est sur `http://localhost:3000`, le siège sur `http://localhost:8085`.

Swagger : `http://localhost:8085/swagger-ui/index.html` (bouton *Authorize* : coller le jeton de `POST /api/auth/login`).

## 7) API REST

Les réponses ci-dessous sont le **contrat** consommé par le front : ne pas les modifier sans concertation
(`*ContractIT` les verrouillent à l'octet près : champs, ordre des champs, valeurs nulles, statuts).

Toutes les routes exigent un jeton, sauf `register`, `login` et la documentation. La colonne *Rôles* donne les rôles autorisés (`—` = tout utilisateur authentifié).
`pays` est le nom d'un pays (voir §1).

### Authentification — `/api/auth`

| Méthode et chemin | Description | Réponse |
|---|---|---|
| `POST /api/auth/register` | Inscrit un utilisateur `{ email, password, role? }` (sans rôle : `ROLE_HQ`). **`ROLE_ADMIN` est toujours refusé** (`403`, rien n'est enregistré), même si l'appelant est administrateur. | `{ token }` |
| `POST /api/auth/login` | Connexion `{ email, password }` | `{ token }` + cookie `token` |
| `GET /api/auth/me` | Utilisateur connecté | `{ id, email, roles, countryId }` |

### Lots et mouvements

| Méthode et chemin | Rôles | Description | Réponse |
|---|---|---|---|
| `GET /lots?pays=` | — | Lots, du plus ancien au plus récent | `LotResponseDto[]` |
| `GET /lot/{id}` | — | Un lot (le premier pays qui le possède) | `LotResponseDto` / `404` |
| `GET /lot/oldest?pays=` | — | Lot à expédier en premier (FIFO) | `LotResponseDto` / `404` |
| `GET /lot/{idLot}/mesures` | — | Mesures des zones traversées par le lot | `[{ temperature, humidite, dateMesure }]` |
| `GET /api/lot/total` | — | Quantité totale en stock | entier / `404` si 0 |
| `POST /lot?pays=` | ADMIN, FARM_MANAGER | Crée un lot `{ status, quantite, caracteristique, dateProduction, idExploitation, idZone }` | `{ idLot, status }` |
| `PUT /lot/{idLot}/status?pays=` | ADMIN, WAREHOUSE_MANAGER, QUALITY | Change le statut `{ status }` | `{ idLot, status }` |
| `PUT /lot/{idLot}/zones?pays=` | ADMIN, WAREHOUSE_MANAGER | Transfère le lot `{ idZone }` | `{ idLot, status }` |
| `POST /mouvements/stockout?idLot=&pays=` | ADMIN, WAREHOUSE_MANAGER, FARM_MANAGER | Sortie de stock | `{ idHistorique, status }` |
| `GET /history/{idLot}?type=` | — | Historique du lot ; `type` : `TRANSFERT`, `ALERTE` (ou `ALERT`), `SHIPPED`, sans tenir compte de la casse | `HistoriqueResponseDto[]` / `400` sans corps si le type est inconnu |

`LotResponseDto` : `{ idLot, status, quantite, caracteristique, dateSortie, dateStockage, dateProduction, emplacement, idExploitation, idEntrepot, historique: [{ idHistorique, type, dateEmission, zonePrecedente, zoneActuelle, detail }] }`.

### Mesures, référentiel, alertes

| Méthode et chemin | Rôles | Description |
|---|---|---|
| `GET /api/mesures/{mesureId}?pays=` | — | Une mesure `{ idMesure, temperature, humidite, dateMesure, idCapteur }` |
| `GET /readings/{idZone}/?pays=&startDate=&endDate=` | — | Mesures d'une zone sur une période (`2026-03-15T10:00`) — **le `/` final est obligatoire** |
| `GET /zones/{idZone}/readings/latest?pays=` | — | Dernière mesure d'une zone |
| `GET /warehouses/{idEntrepot}/readings/summary?pays=` | — | Par zone : `{ zone_id, zone_name, last_temp, last_humidity, avg_temp_24h, avg_humidity_24h }` (snake_case) |
| `GET /countries` | — | Pays configurés : `{ "BRESIL": { tolerences: { humiditeIdeale, temperatureIdeale, toleranceHumidite, toleranceTemperature }, url } }` |
| `GET /farms/{pays}` | — | `[{ idExploitation, nom, adresse }]` |
| `GET /warehouses/{pays}` | — | `[{ idEntrepot, localisation, nom }]` |
| `GET /warehouses/{entrepotId}/zones?pays=` | — | `[{ idZone, nomZone, idEntrepot, idCapteur, lotCount }]` |
| `GET /alertes?idEntrepot=&startDate=&endDate=` | ADMIN, WAREHOUSE_MANAGER, FARM_MANAGER, QUALITY, SUPPLY_CHAIN | `[{ idAlerte, typeAlerte, description, dateEmission, idZone }]` (`idEntrepot` obligatoire, dates facultatives) |

### Reporting

| Méthode et chemin | Rôles | Réponse |
|---|---|---|
| `GET /reporting/global` | ADMIN, HQ | `{ avgAgeDays, complianceRate, countMouvementLast30Days, totalLots }` |
| `GET /reporting/quality?pays=&startDate=&endDate=` | ADMIN, QUALITY, HQ | `{ alertCount, complianceRate, nonComplianceCount }` (`pays` facultatif) |
| `GET /reporting/stock?pays=&startDate=&endDate=` | ADMIN, SUPPLY_CHAIN, HQ | `{ totalLots, avgAgeDays, complianceRate, lotIdsUrgentFifo }` (`pays` obligatoire) |

Un pays qui ne répond pas donne un rapport de zéros (`lotIdsUrgentFifo: null` pour le stock).

### Comportements historiques conservés

Ces comportements sont **voulus par le contrat actuel** (le front et les tests en dépendent) même s'ils sont discutables ; voir « Évolutions recommandées ».

- **Toute erreur est renvoyée en `403` sans corps** : identifiants de connexion faux, e-mail déjà inscrit, route inconnue, paramètre manquant
  ou invalide (UUID, date, pays inconnu du reporting), exception du siège. Cause : Spring Security refuse à un contexte anonyme la
  redirection interne vers `/error`. Seuls `404` (lot / quantité / lot le plus ancien introuvable) et `400` (type d'historique inconnu)
  sont renvoyés tels quels, car les contrôleurs les posent directement.
- **Pays inconnu ou qui ne donne pas de réponse exploitable** : `200` **sans corps** pour `POST /lot`, `PUT /lot/{id}/status`, `PUT /lot/{id}/zones`,
  `POST /mouvements/stockout` (pays inconnu seulement), `GET /api/mesures/{id}`, `GET /zones/{id}/readings/latest`, `GET /farms/{pays}` et
  `GET /warehouses/{pays}` (pays injoignable) ; liste vide `[]` pour les autres lectures de liste.
- **Transfert de zone et sortie de stock refusés par le pays** (ou pays injoignable) : erreur du siège, donc `403` (le pays répond `400`/`404`).
- `GET /warehouses/{entrepotId}/zones` avec un pays injoignable : erreur, donc `403`.
- Les listes ne sont pas paginées.

## 8) Tests

```powershell
mvn test       # tests unitaires et d'architecture (rapides, sans Docker)
mvn verify     # + tests d'intégration (*IT) : API complète, clients HTTP, persistance ; rapport de couverture JaCoCo
```

| Type | Contenu |
|---|---|
| **Unitaires** (`*Test`) | Modèles et règles du domaine (consolidation des rapports, FIFO…), services (Mockito sur les ports), contrôleurs (`@WebMvcTest` : routes, paramètres, JSON exact, codes), mappers MapStruct, filtre JWT, jetons, liaison de la configuration. |
| **Architecture** (`ArchitectureTest`) | Sens des dépendances, domaine sans framework, entités JPA confinées, contrôleurs et ports bien utilisés, `WebClient` confiné aux clients des pays (ArchUnit). |
| **Contrat d'API** (`*ContractIT`) | **Boîte noire** : le siège démarre pour de vrai (port aléatoire, base H2, sécurité JWT) face à trois **faux backends pays** (`FakePaysServer`). Chaque route, chaque rôle, les erreurs, les pays en panne et les réponses atypiques des pays sont vérifiés, JSON comparé exactement ; les requêtes reçues par les pays (chemins, paramètres, corps) aussi. Doit rester vert quelle que soit l'organisation interne. |
| **Clients des pays** (`PaysClientIT`, `*AdapterIT`, `LecturesAdaptersIT`) | Chaque adaptateur face à un faux backend HTTP : chemins, paramètres, corps, parallélisme, tolérance aux erreurs. |
| **Persistance** (`UserAdapterIT`) | Utilisateurs enregistrés pour de vrai (`@DataJpaTest`, H2) : identifiant généré, rôles, unicité de l'e-mail. |
| **Assemblage** (`WiringIT`) | Services du domaine instanciés par `DomainConfig`, pays et clients configurés. |

- Les tests unitaires tournent avec Surefire, les `*IT` avec Failsafe (`mvn verify`). Aucun n'a besoin de Docker ni du réseau.
- Couverture : `mvn verify` écrit le rapport JaCoCo (unitaires + intégration) dans `target/site/jacoco/index.html`.

CI GitHub Actions (`.github/workflows/maven.yml`) : `mvn verify`, publication du rapport de couverture (artefact `jacoco-report-<run>`), analyse SonarQube
(prévue mais désactivée, voir §9) puis publication de l'image Docker.

## 9) Qualité de code et couverture (SonarQube)

**Couverture JaCoCo.** `mvn verify` mesure la couverture des tests unitaires et d'intégration et écrit le rapport dans
`target/site/jacoco/index.html` (HTML) et `target/site/jacoco/jacoco.xml` (lu par SonarQube). Le code généré par Lombok
(`lombok.config`) et les implémentations MapStruct sont ignorés. En CI, le rapport est publié comme artefact `jacoco-report-<run>`.

**SonarQube en local.** Il est défini dans le `docker-compose.yml` sous le profil `sonar` : il n'est **pas** démarré par un simple
`docker compose up`. C'est le même SonarQube (même port, mêmes noms de conteneurs) que celui du backend pays : une seule instance suffit pour
les trois projets (pays, siège, front) ; **s'il tourne déjà, ne pas le relancer ici**.

```powershell
docker compose --profile sonar up -d     # SonarQube + sa base PostgreSQL : http://localhost:9000 (admin / admin, à changer au premier accès)
```

1. Créer un jeton dans SonarQube : *My Account > Security > Generate Tokens* (type « Global Analysis Token »).
2. Lancer les tests puis l'analyse. Le projet `futurekawa-backend-siege` est créé à la première analyse :

```powershell
mvn verify
mvn sonar:sonar "-Dsonar.token=<jeton>"      # ou : $env:SONAR_TOKEN = "<jeton>"; mvn sonar:sonar
```

3. Consulter `http://localhost:9000/dashboard?id=futurekawa-backend-siege` : couverture, bugs, vulnérabilités, duplications, Quality Gate.

`docker compose --profile sonar down` arrête SonarQube (`-v` efface aussi ses données). Le profil désactive le contrôle `vm.max_map_count`
d'Elasticsearch (`SONAR_ES_BOOTSTRAP_CHECKS_DISABLE`) : réservé au développement.

**SonarQube en CI — prévu mais désactivé.** Le job `sonar` de `maven.yml` appelle le workflow réutilisable `.github/workflows/sonar.yml`.
Il est ignoré tant que la variable de dépôt `SONAR_ENABLED` n'est pas à `true`, comme pour le backend pays et le front. Pour l'activer :

1. démarrer SonarQube (`docker compose --profile sonar up -d`) sur la machine qui héberge le runner ;
2. enregistrer un runner **self-hosted** (Windows + Docker) sur le dépôt, car SonarQube écoute sur `localhost:9000` ;
3. créer le secret `SONAR_TOKEN` (*Settings > Secrets and variables > Actions > Secrets*) ;
4. créer la variable `SONAR_ENABLED` = `true` (*… > Variables*).

L'analyse tourne dans un conteneur `maven:3.9-eclipse-temurin-21` (seul Docker est requis sur le runner), réutilise les classes et le rapport de
couverture produits par le job de build, attend la Quality Gate (`sonar.qualitygate.wait=true`) et publie `sonar-report.json` /
`sonar-issues.json` en artefacts.

La configuration de l'analyse (clé du projet, exclusions de couverture : `BackendSiegeApplication` et les DTO de l'API et des pays ; exclusion des
tests de la détection de duplication) est dans les propriétés `sonar.*` du `pom.xml`.

## 10) Points de vigilance pour les contributeurs

- Respecter la séparation hexagonale : une nouvelle règle métier va dans `domain` (modèle ou service), un nouvel appel à un pays
  derrière un **port** réalisé par un adaptateur de `infrastructure/client`.
- Ne pas casser les **DTO** de `api/dto` (noms de champs, **ordre des champs**, valeurs nulles, statuts) : le front les consomme. Les DTO déclarent
  leur ordre (`@JsonPropertyOrder`) et `*ContractIT` le vérifie.
- Les DTO des pays (`infrastructure/client/dto`) sont tolérants : champs inconnus ignorés, champs absents à `null`, jamais de valeur par défaut inventée.
- Les appels aux pays sont **tolérants** (`PaysClient.lire/lister/envoyer` : rien en cas d'erreur) ou **stricts** (`…OuEchouer` : `PaysIndisponibleException`).
  Choisir selon le contrat décrit au §7.
- Les services du domaine ne sont **pas** des `@Service` : les déclarer dans `DomainConfig`.
- `docker-compose.yml` contient des identifiants SMTP des backends pays en clair : à sortir du dépôt (variables d'environnement) et à révoquer.