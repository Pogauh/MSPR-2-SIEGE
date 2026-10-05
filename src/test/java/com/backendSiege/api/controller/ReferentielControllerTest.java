package com.backendSiege.api.controller;

import com.backendSiege.api.mapper.ReferentielMapperImpl;
import com.backendSiege.api.security.JwtAuthenticationFilter;
import com.backendSiege.domain.exceptions.PaysIndisponibleException;
import com.backendSiege.domain.models.Entrepot;
import com.backendSiege.domain.models.Exploitation;
import com.backendSiege.domain.models.Pays;
import com.backendSiege.domain.models.Tolerances;
import com.backendSiege.domain.models.Zone;
import com.backendSiege.domain.services.ReferentielService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ReferentielController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtAuthenticationFilter.class))
@AutoConfigureMockMvc(addFilters = false)
@Import(ReferentielMapperImpl.class)
class ReferentielControllerTest {

    private static final UUID ID_EXPLOITATION = UUID.fromString("aa0e8400-e29b-41d4-a716-446655440022");
    private static final UUID ID_ENTREPOT = UUID.fromString("770e8400-e29b-41d4-a716-446655440011");
    private static final UUID ID_ZONE = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final UUID ID_CAPTEUR = UUID.fromString("b793e242-053a-4def-9cd8-96c990a586c9");

    @Autowired
    MockMvc mockMvc;
    @MockitoBean
    ReferentielService service;

    @Test
    void pays_renvoie_l_url_et_les_tolerances_de_chaque_pays_dans_l_ordre_de_la_configuration() throws Exception {
        when(service.getAllPays()).thenReturn(List.of(
                new Pays("BRESIL", "http://bresil/api/", new Tolerances(29, 55, 3, 2)),
                new Pays("COLOMBIE", "http://colombie/api/", new Tolerances(26, 80, 3, 2))));

        mockMvc.perform(get("/countries"))
                .andExpect(status().isOk())
                .andExpect(content().string("{"
                        + "\"BRESIL\":{\"tolerences\":{\"humiditeIdeale\":55,\"temperatureIdeale\":29,\"toleranceHumidite\":2,\"toleranceTemperature\":3},"
                        + "\"url\":\"http://bresil/api/\"},"
                        + "\"COLOMBIE\":{\"tolerences\":{\"humiditeIdeale\":80,\"temperatureIdeale\":26,\"toleranceHumidite\":2,\"toleranceTemperature\":3},"
                        + "\"url\":\"http://colombie/api/\"}}"));
    }

    @Test
    void pays_sans_tolerances_les_renvoie_a_null() throws Exception {
        when(service.getAllPays()).thenReturn(List.of(new Pays("BRESIL", "http://bresil/api/", null)));

        mockMvc.perform(get("/countries"))
                .andExpect(content().string("{\"BRESIL\":{\"tolerences\":null,\"url\":\"http://bresil/api/\"}}"));
    }

    @Test
    void exploitations_d_un_pays() throws Exception {
        when(service.getAllExploitationsByPays("bresil")).thenReturn(List.of(new Exploitation(ID_EXPLOITATION, "Ferme", "Rue 1")));

        mockMvc.perform(get("/farms/bresil"))
                .andExpect(status().isOk())
                .andExpect(content().string("[{\"idExploitation\":\"" + ID_EXPLOITATION + "\",\"nom\":\"Ferme\",\"adresse\":\"Rue 1\"}]"));
    }

    @Test
    void entrepots_d_un_pays() throws Exception {
        when(service.getAllEntrepotsByPays("colombie")).thenReturn(List.of(new Entrepot(ID_ENTREPOT, "Santos", "Principal")));

        mockMvc.perform(get("/warehouses/colombie"))
                .andExpect(status().isOk())
                .andExpect(content().string("[{\"idEntrepot\":\"" + ID_ENTREPOT + "\",\"localisation\":\"Santos\",\"nom\":\"Principal\"}]"));
    }

    @Test
    void exploitations_et_entrepots_d_un_pays_qui_ne_repond_pas_renvoient_200_sans_corps() throws Exception {
        when(service.getAllExploitationsByPays("bresil")).thenThrow(new PaysIndisponibleException("BRESIL", new RuntimeException()));
        when(service.getAllEntrepotsByPays("bresil")).thenThrow(new PaysIndisponibleException("BRESIL", new RuntimeException()));

        mockMvc.perform(get("/farms/bresil")).andExpect(status().isOk()).andExpect(content().string(""));
        mockMvc.perform(get("/warehouses/bresil")).andExpect(status().isOk()).andExpect(content().string(""));
    }

    @Test
    void zones_d_un_entrepot() throws Exception {
        when(service.getAllZonesByEntrepot("bresil", ID_ENTREPOT)).thenReturn(List.of(
                new Zone(ID_ZONE, "A", ID_ENTREPOT, ID_CAPTEUR, 3L), new Zone(UUID.fromString("44444444-4444-4444-4444-444444444444"), "B", ID_ENTREPOT, null, null)));

        mockMvc.perform(get("/warehouses/" + ID_ENTREPOT + "/zones?pays=bresil"))
                .andExpect(status().isOk())
                .andExpect(content().string("[{\"idZone\":\"" + ID_ZONE + "\",\"nomZone\":\"A\",\"idEntrepot\":\"" + ID_ENTREPOT + "\","
                        + "\"idCapteur\":\"" + ID_CAPTEUR + "\",\"lotCount\":3},"
                        + "{\"idZone\":\"44444444-4444-4444-4444-444444444444\",\"nomZone\":\"B\",\"idEntrepot\":\"" + ID_ENTREPOT + "\","
                        + "\"idCapteur\":null,\"lotCount\":null}]"));
    }

    @Test
    void zones_d_un_pays_qui_ne_repond_pas_laissent_remonter_l_erreur() {
        when(service.getAllZonesByEntrepot("bresil", ID_ENTREPOT)).thenThrow(new PaysIndisponibleException("BRESIL", new RuntimeException()));

        assertThatThrownBy(() -> mockMvc.perform(get("/warehouses/" + ID_ENTREPOT + "/zones?pays=bresil")))
                .hasCauseInstanceOf(PaysIndisponibleException.class);
    }

    @Test
    void zones_exigent_le_pays() throws Exception {
        mockMvc.perform(get("/warehouses/" + ID_ENTREPOT + "/zones")).andExpect(status().isBadRequest());
    }
}
