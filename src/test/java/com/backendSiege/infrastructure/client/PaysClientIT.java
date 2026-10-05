package com.backendSiege.infrastructure.client;

import com.backendSiege.domain.exceptions.PaysIndisponibleException;
import com.backendSiege.support.FakePaysServer;
import com.backendSiege.support.FakePaysServer.RequestRecue;
import com.backendSiege.support.PaysTestClients;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.groups.Tuple.tuple;

/** Comportement du client HTTP d'un pays face à un vrai serveur : appels tolérants (rien en cas d'erreur) et stricts (exception). */
class PaysClientIT {

    record Element(String nom, Integer valeur) {
    }

    record Corps(String texte) {
    }

    FakePaysServer serveur;
    PaysClient client;

    @BeforeEach
    void demarrer() {
        serveur = new FakePaysServer();
        client = PaysTestClients.client("BRESIL", serveur);
    }

    @AfterEach
    void arreter() {
        serveur.couper();
    }

    // ------------------------------------------------------------------ appels tolérants

    @Test
    void lire_renvoie_le_contenu_d_une_reponse_2xx() {
        serveur.get("/api/elements/1", "{\"nom\":\"a\",\"valeur\":3,\"inconnu\":true}");

        assertThat(client.lire(uri -> uri.path("elements/{id}").build(1), Element.class, "test").blockOptional())
                .contains(new Element("a", 3));
    }

    @Test
    void lire_ne_renvoie_rien_pour_une_erreur_http_un_corps_vide_ou_illisible_ou_un_pays_injoignable() {
        serveur.repond("GET", "/api/erreur", 500, "{}");
        serveur.repond("GET", "/api/vide", 200, "");
        serveur.get("/api/illisible", "pas du json");

        assertThat(client.lire(uri -> uri.path("absent").build(), Element.class, "test").blockOptional()).isEmpty();
        assertThat(client.lire(uri -> uri.path("erreur").build(), Element.class, "test").blockOptional()).isEmpty();
        assertThat(client.lire(uri -> uri.path("vide").build(), Element.class, "test").blockOptional()).isEmpty();
        assertThat(client.lire(uri -> uri.path("illisible").build(), Element.class, "test").blockOptional()).isEmpty();
        serveur.couper();
        assertThat(client.lire(uri -> uri.path("elements/1").build(), Element.class, "test").blockOptional()).isEmpty();
    }

    @Test
    void lister_renvoie_les_elements_d_une_reponse_2xx() {
        serveur.get("/api/elements", "[{\"nom\":\"a\",\"valeur\":1},{\"nom\":\"b\",\"valeur\":null}]");

        assertThat(client.lister(uri -> uri.path("elements").build(), Element.class, "test").collectList().block())
                .containsExactly(new Element("a", 1), new Element("b", null));
    }

    @Test
    void lister_ne_renvoie_rien_pour_une_erreur_http_ou_un_pays_injoignable() {
        serveur.repond("GET", "/api/erreur", 503, "[]");

        assertThat(client.lister(uri -> uri.path("absent").build(), Element.class, "test").collectList().block()).isEmpty();
        assertThat(client.lister(uri -> uri.path("erreur").build(), Element.class, "test").collectList().block()).isEmpty();
        serveur.couper();
        assertThat(client.lister(uri -> uri.path("elements").build(), Element.class, "test").collectList().block()).isEmpty();
    }

    @Test
    void lister_et_envoyer_ne_renvoient_rien_si_la_reponse_est_illisible() {
        serveur.get("/api/liste-illisible", "[{pas du json");
        serveur.repond("POST", "/api/illisible", 200, "pas du json");

        assertThat(client.lister(uri -> uri.path("liste-illisible").build(), Element.class, "test").collectList().block()).isEmpty();
        assertThat(client.envoyer(HttpMethod.POST, uri -> uri.path("illisible").build(), new Corps("x"), Element.class, "test").blockOptional())
                .isEmpty();
    }

    @Test
    void envoyer_transmet_le_corps_en_json_et_renvoie_la_reponse() {
        serveur.repond("POST", "/api/elements", 201, "{\"nom\":\"cree\",\"valeur\":1}");

        Optional<Element> reponse = client.envoyer(HttpMethod.POST, uri -> uri.path("elements").build(), new Corps("bonjour"), Element.class, "test")
                .blockOptional();

        assertThat(reponse).contains(new Element("cree", 1));
        assertThat(serveur.requetes()).extracting(RequestRecue::methode, RequestRecue::chemin, RequestRecue::corps)
                .containsExactly(tuple("POST", "/api/elements", "{\"texte\":\"bonjour\"}"));
    }

    @Test
    void envoyer_ne_renvoie_rien_pour_une_erreur_http_ou_un_pays_injoignable() {
        serveur.repond("PUT", "/api/elements", 400, "{}");

        assertThat(client.envoyer(HttpMethod.PUT, uri -> uri.path("elements").build(), new Corps("x"), Element.class, "test").blockOptional()).isEmpty();
        serveur.couper();
        assertThat(client.envoyer(HttpMethod.PUT, uri -> uri.path("elements").build(), new Corps("x"), Element.class, "test").blockOptional()).isEmpty();
    }

    // ------------------------------------------------------------------ appels stricts

    @Test
    void listerOuEchouer_renvoie_la_liste_d_une_reponse_2xx_meme_vide() {
        serveur.get("/api/elements", "[{\"nom\":\"a\",\"valeur\":1}]");
        serveur.get("/api/aucun", "[]");

        assertThat(client.listerOuEchouer(uri -> uri.path("elements").build(), Element.class)).containsExactly(new Element("a", 1));
        assertThat(client.listerOuEchouer(uri -> uri.path("aucun").build(), Element.class)).isEmpty();
    }

    @Test
    void listerOuEchouer_signale_un_pays_indisponible_pour_une_erreur_http_une_reponse_illisible_ou_un_pays_injoignable() {
        serveur.repond("GET", "/api/erreur", 500, "{}");
        serveur.get("/api/illisible", "pas du json");

        assertThatThrownBy(() -> client.listerOuEchouer(uri -> uri.path("absent").build(), Element.class))
                .isInstanceOf(PaysIndisponibleException.class).hasMessageContaining("BRESIL");
        assertThatThrownBy(() -> client.listerOuEchouer(uri -> uri.path("erreur").build(), Element.class))
                .isInstanceOf(PaysIndisponibleException.class);
        assertThatThrownBy(() -> client.listerOuEchouer(uri -> uri.path("illisible").build(), Element.class))
                .isInstanceOf(PaysIndisponibleException.class);
        serveur.couper();
        assertThatThrownBy(() -> client.listerOuEchouer(uri -> uri.path("elements").build(), Element.class))
                .isInstanceOf(PaysIndisponibleException.class);
    }

    @Test
    void envoyerOuEchouer_renvoie_la_reponse_ou_rien_si_le_corps_est_vide() {
        serveur.repond("PUT", "/api/elements", 200, "{\"nom\":\"ok\",\"valeur\":2}");
        serveur.repond("POST", "/api/vide", 200, "");

        assertThat(client.envoyerOuEchouer(HttpMethod.PUT, uri -> uri.path("elements").build(), new Corps("x"), Element.class))
                .contains(new Element("ok", 2));
        assertThat(client.envoyerOuEchouer(HttpMethod.POST, uri -> uri.path("vide").build(), null, Element.class)).isEmpty();
        assertThat(serveur.requetes("/api/vide")).extracting(RequestRecue::corps).containsExactly("");
    }

    @Test
    void envoyerOuEchouer_signale_un_pays_indisponible_pour_une_erreur_http_ou_un_pays_injoignable() {
        serveur.repond("PUT", "/api/refus", 400, "{\"status\":400}");

        assertThatThrownBy(() -> client.envoyerOuEchouer(HttpMethod.PUT, uri -> uri.path("refus").build(), new Corps("x"), Element.class))
                .isInstanceOf(PaysIndisponibleException.class)
                .hasCauseInstanceOf(org.springframework.web.reactive.function.client.WebClientResponseException.class);
        serveur.couper();
        assertThatThrownBy(() -> client.envoyerOuEchouer(HttpMethod.PUT, uri -> uri.path("refus").build(), new Corps("x"), Element.class))
                .isInstanceOf(PaysIndisponibleException.class);
    }

    @Test
    void les_chemins_sont_relatifs_a_l_adresse_du_pays_et_les_variables_sont_encodees() {
        serveur.get("/api/elements/a%20b", "{\"nom\":\"espace\",\"valeur\":1}");

        assertThat(client.lire(uri -> uri.path("elements/{id}").build("a b"), Element.class, "test").blockOptional())
                .contains(new Element("espace", 1));
        assertThat(List.copyOf(serveur.requetes()).get(0).chemin()).isEqualTo("/api/elements/a%20b");
    }
}
