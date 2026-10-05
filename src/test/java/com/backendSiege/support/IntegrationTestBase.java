package com.backendSiege.support;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Base des tests d'intégration « boîte noire » : le siège démarre pour de vrai (port aléatoire, base H2, sécurité JWT)
 * et trois faux backends pays (Brésil, Colombie, Équateur) tiennent la place des vrais.
 * Les tests ne connaissent que le contrat HTTP : ils doivent rester verts quelle que soit l'organisation interne du code.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public abstract class IntegrationTestBase {

    protected static final FakePaysServer BRESIL = new FakePaysServer();
    protected static final FakePaysServer COLOMBIE = new FakePaysServer();
    protected static final FakePaysServer EQUATEUR = new FakePaysServer();
    protected static final List<FakePaysServer> TOUS_LES_PAYS = List.of(BRESIL, COLOMBIE, EQUATEUR);

    private static final HttpClient CLIENT = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.NEVER)
            .connectTimeout(Duration.ofSeconds(5))
            .build();
    private static final Map<String, String> JETONS_PAR_ROLE = new ConcurrentHashMap<>();
    private static final Pattern TOKEN = Pattern.compile("\"token\"\\s*:\\s*\"([^\"]+)\"");

    /** Hachage BCrypt de « password123 », celui des utilisateurs de {@code postgres/init_siege.sql}. */
    private static final String HASH_PASSWORD123 = "$2a$10$gxHepxcprQtRsiubAFktLudW6tlOdkWfN7a03OjMxog1oVpoVaIIG";

    @Value("${local.server.port}")
    private int port;

    @Autowired
    private JdbcTemplate jdbc;

    @DynamicPropertySource
    static void configurerLesPays(DynamicPropertyRegistry registry) {
        registry.add("app.country-adapters.mapping.BRESIL.url", BRESIL::baseUrl);
        registry.add("app.country-adapters.mapping.COLOMBIE.url", COLOMBIE::baseUrl);
        registry.add("app.country-adapters.mapping.EQUATEUR.url", EQUATEUR::baseUrl);
    }

    @BeforeEach
    void reinitialiserLesPays() {
        TOUS_LES_PAYS.forEach(FakePaysServer::reinitialiser);
    }

    /** Réponse du siège. */
    public record Reponse(int status, String corps, java.net.http.HttpHeaders entetes) {
    }

    protected Reponse appeler(String methode, String chemin, String jeton, String corps) {
        HttpRequest.Builder requete = HttpRequest.newBuilder(URI.create("http://localhost:" + port + chemin))
                .timeout(Duration.ofSeconds(20));
        if (jeton != null) {
            requete.header("Authorization", "Bearer " + jeton);
        }
        if (corps != null) {
            requete.header("Content-Type", "application/json");
        }
        requete.method(methode, corps == null ? HttpRequest.BodyPublishers.noBody() : HttpRequest.BodyPublishers.ofString(corps));
        try {
            HttpResponse<String> reponse = CLIENT.send(requete.build(), HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            return new Reponse(reponse.statusCode(), reponse.body(), reponse.headers());
        } catch (IOException e) {
            throw new IllegalStateException("Appel " + methode + " " + chemin + " impossible", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(e);
        }
    }

    protected Reponse get(String chemin, String jeton) {
        return appeler("GET", chemin, jeton, null);
    }

    protected Reponse post(String chemin, String jeton, String corps) {
        return appeler("POST", chemin, jeton, corps);
    }

    protected Reponse put(String chemin, String jeton, String corps) {
        return appeler("PUT", chemin, jeton, corps);
    }

    /**
     * Crée en base un utilisateur du rôle demandé (comme le fait {@code postgres/init_siege.sql}, car l'inscription par l'API
     * n'accorde pas tous les rôles : jamais celui d'administrateur), se connecte et renvoie son jeton JWT.
     */
    protected String jetonPour(String role) {
        String email = "user-" + UUID.randomUUID() + "@siege.test";
        UUID id = UUID.randomUUID();
        jdbc.update("insert into users (id, email, password, country_id) values (?, ?, ?, null)", id, email, HASH_PASSWORD123);
        jdbc.update("insert into user_roles (user_id, role) values (?, ?)", id, role);

        Reponse reponse = post("/api/auth/login", null, "{\"email\":\"%s\",\"password\":\"password123\"}".formatted(email));
        Matcher matcher = TOKEN.matcher(reponse.corps());
        if (reponse.status() != 200 || !matcher.find()) {
            throw new IllegalStateException("Connexion impossible : " + reponse.status() + " " + reponse.corps());
        }
        return matcher.group(1);
    }

    /** Jeton d'un utilisateur du rôle demandé, créé une seule fois pour toute la suite de tests (la connexion coûte une vérification BCrypt). */
    protected String jetonDuRole(String role) {
        return JETONS_PAR_ROLE.computeIfAbsent(role, this::jetonPour);
    }

    protected String jetonAdmin() {
        return jetonDuRole("ROLE_ADMIN");
    }
}
