package com.backendSiege;

import com.backendSiege.support.IntegrationTestBase;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.backendSiege.support.Json.json;
import static com.backendSiege.support.Json.lire;
import static org.assertj.core.api.Assertions.assertThat;

/** Contrat de l'authentification : ce que reçoit le front à l'inscription, à la connexion et sur {@code /api/auth/me}. */
class AuthContractIT extends IntegrationTestBase {

    private static final Pattern TOKEN = Pattern.compile("^\\{\"token\":\"([^\"]+)\"}$");
    private static final Pattern ME = Pattern.compile(
            "^\\{\"id\":\"[0-9a-f-]{36}\",\"email\":\"%s\",\"roles\":\\[%s],\"countryId\":null}$");

    private static String emailUnique() {
        return "auth-" + UUID.randomUUID() + "@siege.test";
    }

    private static String corpsInscription(String email, String role) {
        return role == null
                ? json("{'email':'%s','password':'secret'}").formatted(email)
                : json("{'email':'%s','password':'secret','role':'%s'}").formatted(email, role);
    }

    private static String charge(String jeton) {
        String[] parties = jeton.split("\\.");
        assertThat(parties).as("un JWT a trois parties").hasSize(3);
        return new String(Base64.getUrlDecoder().decode(parties[1]), StandardCharsets.UTF_8);
    }

    @Test
    void inscription_sans_role_donne_le_role_HQ_et_renvoie_un_jeton() {
        String email = emailUnique();

        Reponse reponse = post("/api/auth/register", null, corpsInscription(email, null));

        assertThat(reponse.status()).isEqualTo(200);
        assertThat(reponse.entetes().firstValue("Content-Type")).hasValueSatisfying(t -> assertThat(t).startsWith("application/json"));
        Matcher jeton = TOKEN.matcher(reponse.corps());
        assertThat(jeton.matches()).as(reponse.corps()).isTrue();

        var charge = lire(charge(jeton.group(1)));
        assertThat(charge.get("sub").asString()).isEqualTo(email);
        assertThat(charge.get("roles").toString()).isEqualTo("[\"ROLE_HQ\"]");
        assertThat(charge.get("exp").asLong() - charge.get("iat").asLong()).as("validité d'un jour").isBetween(86_400L, 86_401L);
    }

    @Test
    void inscription_avec_un_role_le_reporte_dans_le_jeton_et_dans_me() {
        String email = emailUnique();

        Reponse inscription = post("/api/auth/register", null, corpsInscription(email, "ROLE_QUALITY"));
        String jeton = jetonDe(inscription);

        assertThat(lire(charge(jeton)).get("roles").toString()).isEqualTo("[\"ROLE_QUALITY\"]");
        Reponse me = get("/api/auth/me", jeton);
        assertThat(me.status()).isEqualTo(200);
        assertThat(me.entetes().firstValue("Content-Type")).hasValueSatisfying(t -> assertThat(t).startsWith("application/json"));
        assertThat(me.corps()).matches(ME.pattern().formatted(Pattern.quote(email), "\"ROLE_QUALITY\""));
    }

    @Test
    void inscription_en_administrateur_est_toujours_refusee_en_403_sans_corps_et_n_enregistre_rien() {
        String email = emailUnique();

        Reponse anonyme = post("/api/auth/register", null, corpsInscription(email, "ROLE_ADMIN"));
        Reponse parUnAdministrateur = post("/api/auth/register", jetonAdmin(), corpsInscription(email, "ROLE_ADMIN"));

        for (Reponse refus : new Reponse[]{anonyme, parUnAdministrateur}) {
            assertThat(refus.status()).isEqualTo(403);
            assertThat(refus.corps()).isEmpty();
        }
        assertThat(post("/api/auth/login", null, json("{'email':'%s','password':'secret'}").formatted(email)).status())
                .as("aucun utilisateur n'a été créé").isEqualTo(403);
        assertThat(post("/api/auth/register", null, corpsInscription(email, null)).status())
                .as("l'e-mail n'a pas été réservé par la demande refusée").isEqualTo(200);
    }

    @Test
    void les_autres_roles_restent_obtenables_par_inscription() {
        for (String role : new String[]{"ROLE_HQ", "ROLE_WAREHOUSE_MANAGER", "ROLE_QUALITY", "ROLE_FARM_MANAGER", "ROLE_SUPPLY_CHAIN"}) {
            String jeton = jetonDe(post("/api/auth/register", null, corpsInscription(emailUnique(), role)));

            assertThat(lire(charge(jeton)).get("roles").toString()).isEqualTo("[\"" + role + "\"]");
        }
    }

    @Test
    void inscription_d_un_email_deja_utilise_est_refusee_en_403_sans_corps() {
        String email = emailUnique();
        post("/api/auth/register", null, corpsInscription(email, null));

        Reponse doublon = post("/api/auth/register", null, corpsInscription(email, null));

        assertThat(doublon.status()).isEqualTo(403);
        assertThat(doublon.corps()).isEmpty();
    }

    @Test
    void connexion_renvoie_le_jeton_et_pose_le_cookie_du_jeton() {
        String email = emailUnique();
        post("/api/auth/register", null, corpsInscription(email, "ROLE_QUALITY"));

        Reponse reponse = post("/api/auth/login", null, json("{'email':'%s','password':'secret'}").formatted(email));

        assertThat(reponse.status()).isEqualTo(200);
        String jeton = jetonDe(reponse);
        assertThat(lire(charge(jeton)).get("sub").asString()).isEqualTo(email);
        String cookie = reponse.entetes().firstValue("Set-Cookie").orElseThrow();
        assertThat(cookie).startsWith("token=" + jeton + ";");
        assertThat(cookie).contains("Max-Age=86400").contains("Path=/").contains("HttpOnly");
    }

    @Test
    void connexion_refusee_en_403_sans_corps_si_le_mot_de_passe_est_faux_ou_l_email_inconnu() {
        String email = emailUnique();
        post("/api/auth/register", null, corpsInscription(email, null));

        Reponse mauvaisMotDePasse = post("/api/auth/login", null, json("{'email':'%s','password':'faux'}").formatted(email));
        Reponse emailInconnu = post("/api/auth/login", null, json("{'email':'%s','password':'secret'}").formatted(emailUnique()));

        assertThat(mauvaisMotDePasse.status()).isEqualTo(403);
        assertThat(mauvaisMotDePasse.corps()).isEmpty();
        assertThat(mauvaisMotDePasse.entetes().firstValue("Set-Cookie")).isEmpty();
        assertThat(emailInconnu.status()).isEqualTo(403);
        assertThat(emailInconnu.corps()).isEmpty();
    }

    @Test
    void inscription_avec_un_corps_illisible_ou_un_role_inconnu_est_refusee_en_403() {
        assertThat(post("/api/auth/register", null, "{pas du json").status()).isEqualTo(403);
        assertThat(post("/api/auth/register", null, corpsInscription(emailUnique(), "ROLE_INCONNU")).status()).isEqualTo(403);
    }

    @Test
    void sans_jeton_ou_avec_un_jeton_invalide_l_acces_est_refuse_en_403_sans_corps() {
        for (String chemin : new String[]{"/api/auth/me", "/lots", "/countries", "/reporting/global"}) {
            Reponse sansJeton = get(chemin, null);
            assertThat(sansJeton.status()).as(chemin + " sans jeton").isEqualTo(403);
            assertThat(sansJeton.corps()).isEmpty();
            assertThat(get(chemin, "pas.un.jeton").status()).as(chemin + " jeton invalide").isEqualTo(403);
        }
    }

    @Test
    void un_jeton_signe_avec_une_autre_cle_est_refuse() {
        String jetonAdmin = jetonAdmin();
        String[] parties = jetonAdmin.split("\\.");
        String falsifie = parties[0] + "." + parties[1] + "." + "AAAA" + parties[2].substring(4);

        assertThat(get("/lots", falsifie).status()).isEqualTo(403);
    }

    @Test
    void la_documentation_openapi_est_publique_sans_jeton() {
        Reponse reponse = get("/v3/api-docs", null);

        assertThat(reponse.status()).isEqualTo(200);
        assertThat(lire(reponse.corps()).get("components").get("securitySchemes").get("bearerAuth").get("scheme").asString())
                .isEqualTo("bearer");
    }

    @Test
    void l_interface_swagger_est_publique_sans_jeton() {
        Reponse reponse = get("/swagger-ui/index.html", null);

        assertThat(reponse.status()).isEqualTo(200);
        assertThat(reponse.corps()).contains("swagger");
    }

    private String jetonDe(Reponse reponse) {
        Matcher jeton = TOKEN.matcher(reponse.corps());
        assertThat(jeton.matches()).as(reponse.corps()).isTrue();
        return jeton.group(1);
    }
}
