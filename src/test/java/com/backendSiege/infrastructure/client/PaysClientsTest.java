package com.backendSiege.infrastructure.client;

import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PaysClientsTest {

    private static PaysClient client(String code) {
        return new PaysClient(code, WebClient.builder().baseUrl("http://" + code.toLowerCase() + "/api/").build());
    }

    private final PaysClients clients = new PaysClients(Map.of(
            "BRESIL", client("BRESIL"), "COLOMBIE", client("COLOMBIE"), "EQUATEUR", client("EQUATEUR")));

    @Test
    void get_renvoie_le_client_du_pays() {
        assertThat(clients.get("COLOMBIE").code()).isEqualTo("COLOMBIE");
    }

    @Test
    void get_d_un_code_inconnu_est_une_erreur_de_programmation() {
        assertThatThrownBy(() -> clients.get("ATLANTIDE"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("ATLANTIDE");
    }

    @Test
    void les_codes_sont_normalises_en_majuscules_donc_get_ne_trouve_pas_une_saisie_brute() {
        assertThatThrownBy(() -> clients.get("bresil")).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void clients_conserve_l_ordre_demande() {
        assertThat(clients.clients(List.of("EQUATEUR", "BRESIL"))).extracting(PaysClient::code).containsExactly("EQUATEUR", "BRESIL");
    }

    @Test
    void clients_ignore_un_code_inconnu() {
        assertThat(clients.clients(List.of("BRESIL", "ATLANTIDE", "COLOMBIE"))).extracting(PaysClient::code)
                .containsExactly("BRESIL", "COLOMBIE");
    }

    @Test
    void clients_sans_code_est_vide() {
        assertThat(clients.clients(List.of())).isEmpty();
    }

    @Test
    void le_registre_est_fige_a_sa_creation() {
        Map<String, PaysClient> source = new LinkedHashMap<>();
        source.put("BRESIL", client("BRESIL"));
        PaysClients figes = new PaysClients(source);
        source.put("COLOMBIE", client("COLOMBIE"));

        assertThat(figes.clients(List.of("BRESIL", "COLOMBIE"))).extracting(PaysClient::code).containsExactly("BRESIL");
    }
}
