package com.backendSiege.infrastructure.config;

import com.backendSiege.domain.models.Pays;
import com.backendSiege.domain.models.Tolerances;
import com.backendSiege.infrastructure.client.adapter.PaysAdapter;
import com.backendSiege.infrastructure.client.PaysClients;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.MapConfigurationPropertySource;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PaysConfigurationTest {

    private static PaysProperties lier(Map<String, String> proprietes) {
        return new Binder(new MapConfigurationPropertySource(proprietes)).bind("app.country-adapters", PaysProperties.class).get();
    }

    private static Map<String, String> deuxPays() {
        Map<String, String> proprietes = new LinkedHashMap<>();
        proprietes.put("app.country-adapters.mapping.BRESIL.url", "http://bresil:8080/api/");
        proprietes.put("app.country-adapters.mapping.BRESIL.tolerences.temperature-ideale", "29");
        proprietes.put("app.country-adapters.mapping.BRESIL.tolerences.humidite-ideale", "55");
        proprietes.put("app.country-adapters.mapping.BRESIL.tolerences.tolerance-temperature", "3");
        proprietes.put("app.country-adapters.mapping.BRESIL.tolerences.tolerance-humidite", "2");
        proprietes.put("app.country-adapters.mapping.colombie.url", "http://colombie:8080/api/");
        return proprietes;
    }

    @Test
    void les_proprietes_sont_liees_avec_leurs_noms_historiques() {
        PaysProperties proprietes = lier(deuxPays());

        assertThat(proprietes.mapping()).containsOnlyKeys("BRESIL", "colombie");
        assertThat(proprietes.mapping().get("BRESIL").url()).isEqualTo("http://bresil:8080/api/");
        assertThat(proprietes.mapping().get("BRESIL").tolerences()).isEqualTo(new PaysProperties.Tolerances(29, 55, 3, 2));
        assertThat(proprietes.mapping().get("colombie").tolerences()).isNull();
    }

    @Test
    void sans_configuration_il_n_y_a_aucun_pays() {
        assertThat(new PaysProperties(null).mapping()).isEmpty();
    }

    @Test
    void l_adaptateur_expose_les_pays_avec_le_nom_configure_et_leurs_tolerances() {
        PaysAdapter adaptateur = new PaysAdapter(lier(deuxPays()));

        List<Pays> pays = adaptateur.findAll();

        assertThat(pays).containsExactly(
                new Pays("BRESIL", "http://bresil:8080/api/", new Tolerances(29, 55, 3, 2)),
                new Pays("colombie", "http://colombie:8080/api/", null));
    }

    @Test
    void un_client_est_cree_par_pays_sous_son_code_normalise() {
        PaysClients clients = new PaysClientsConfig().paysClients(lier(deuxPays()));

        assertThat(clients.get("BRESIL").code()).isEqualTo("BRESIL");
        assertThat(clients.get("COLOMBIE").code()).isEqualTo("COLOMBIE");
        assertThatThrownBy(() -> clients.get("colombie")).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void les_jetons_durent_un_jour_par_defaut() {
        JwtProperties proprietes = new Binder(new MapConfigurationPropertySource(Map.of("app.security.jwt.secret", "s")))
                .bind("app.security.jwt", JwtProperties.class).get();

        assertThat(proprietes.secret()).isEqualTo("s");
        assertThat(proprietes.expiration()).isEqualTo(java.time.Duration.ofHours(24));
    }

    @Test
    void la_duree_des_jetons_est_configurable() {
        JwtProperties proprietes = new Binder(new MapConfigurationPropertySource(
                Map.of("app.security.jwt.secret", "s", "app.security.jwt.expiration", "2h"))).bind("app.security.jwt", JwtProperties.class).get();

        assertThat(proprietes.expiration()).isEqualTo(java.time.Duration.ofHours(2));
    }
}
