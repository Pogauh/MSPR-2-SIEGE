package com.backendSiege.api.controller;

import com.backendSiege.api.mapper.CapteurSummaryMapperImpl;
import com.backendSiege.api.mapper.MesureMapperImpl;
import com.backendSiege.api.security.JwtAuthenticationFilter;
import com.backendSiege.domain.models.CapteurSummary;
import com.backendSiege.domain.models.Mesure;
import com.backendSiege.domain.services.MesureService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = MesureController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtAuthenticationFilter.class))
@AutoConfigureMockMvc(addFilters = false)
@Import({MesureMapperImpl.class, CapteurSummaryMapperImpl.class})
class MesureControllerTest {

    private static final UUID ID_MESURE = UUID.fromString("99999999-9999-9999-9999-999999999999");
    private static final UUID ID_CAPTEUR = UUID.fromString("b793e242-053a-4def-9cd8-96c990a586c9");
    private static final UUID ID_ZONE = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final UUID ID_ENTREPOT = UUID.fromString("770e8400-e29b-41d4-a716-446655440011");

    private static final Mesure MESURE = new Mesure(ID_MESURE, 27.5, 56.2, LocalDateTime.of(2026, 1, 12, 10, 0), ID_CAPTEUR);
    private static final String MESURE_JSON = "{\"idMesure\":\"" + ID_MESURE + "\",\"temperature\":27.5,\"humidite\":56.2,"
            + "\"dateMesure\":\"2026-01-12T10:00:00\",\"idCapteur\":\"" + ID_CAPTEUR + "\"}";

    @Autowired
    MockMvc mockMvc;
    @MockitoBean
    MesureService service;

    @Test
    void mesure_est_renvoyee_dans_l_ordre_du_contrat() throws Exception {
        when(service.getMesureById("bresil", ID_MESURE)).thenReturn(Optional.of(MESURE));

        mockMvc.perform(get("/api/mesures/" + ID_MESURE + "?pays=bresil")).andExpect(status().isOk()).andExpect(content().string(MESURE_JSON));
    }

    @Test
    void mesure_absente_renvoie_200_sans_corps() throws Exception {
        when(service.getMesureById("bresil", ID_MESURE)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/mesures/" + ID_MESURE + "?pays=bresil")).andExpect(status().isOk()).andExpect(content().string(""));
    }

    @Test
    void mesure_exige_le_pays() throws Exception {
        mockMvc.perform(get("/api/mesures/" + ID_MESURE)).andExpect(status().isBadRequest());
    }

    @Test
    void resume_des_capteurs_est_renvoye_en_snake_case_avec_les_decimales_du_pays() throws Exception {
        when(service.getCapteurSummaryByEntrepotId("bresil", ID_ENTREPOT)).thenReturn(List.of(
                new CapteurSummary(ID_ZONE, "A", new BigDecimal("27.50"), new BigDecimal("56.20"), new BigDecimal("27.125"), new BigDecimal("56"))));

        mockMvc.perform(get("/warehouses/" + ID_ENTREPOT + "/readings/summary?pays=bresil"))
                .andExpect(status().isOk())
                .andExpect(content().string("[{\"zone_id\":\"" + ID_ZONE + "\",\"zone_name\":\"A\",\"last_temp\":27.50,\"last_humidity\":56.20,"
                        + "\"avg_temp_24h\":27.125,\"avg_humidity_24h\":56}]"));
    }

    @Test
    void mesures_d_une_zone_transmettent_le_pays_et_la_periode() throws Exception {
        when(service.getMesuresByZoneId("bresil", ID_ZONE, LocalDateTime.of(2026, 1, 1, 10, 0), LocalDateTime.of(2026, 1, 2, 0, 0)))
                .thenReturn(List.of(MESURE));

        mockMvc.perform(get("/readings/" + ID_ZONE + "/?pays=bresil&startDate=2026-01-01T10:00&endDate=2026-01-02T00:00"))
                .andExpect(status().isOk())
                .andExpect(content().string("[" + MESURE_JSON + "]"));
    }

    @Test
    void mesures_d_une_zone_exigent_pays_et_dates() throws Exception {
        mockMvc.perform(get("/readings/" + ID_ZONE + "/?startDate=2026-01-01T10:00&endDate=2026-01-02T00:00")).andExpect(status().isBadRequest());
        mockMvc.perform(get("/readings/" + ID_ZONE + "/?pays=bresil&startDate=2026-01-01T10:00")).andExpect(status().isBadRequest());
        mockMvc.perform(get("/readings/" + ID_ZONE + "/?pays=bresil&startDate=hier&endDate=demain")).andExpect(status().isBadRequest());
    }

    @Test
    void la_route_des_mesures_d_une_zone_se_termine_par_une_barre_oblique() throws Exception {
        mockMvc.perform(get("/readings/" + ID_ZONE + "?pays=bresil&startDate=2026-01-01T10:00&endDate=2026-01-02T00:00"))
                .andExpect(status().isNotFound());
    }

    @Test
    void derniere_mesure_d_une_zone_est_relayee_ou_renvoie_200_sans_corps() throws Exception {
        when(service.getLastMesureByZoneId("bresil", ID_ZONE)).thenReturn(Optional.of(MESURE));
        when(service.getLastMesureByZoneId("colombie", ID_ZONE)).thenReturn(Optional.empty());

        mockMvc.perform(get("/zones/" + ID_ZONE + "/readings/latest?pays=bresil")).andExpect(status().isOk()).andExpect(content().string(MESURE_JSON));
        mockMvc.perform(get("/zones/" + ID_ZONE + "/readings/latest?pays=colombie")).andExpect(status().isOk()).andExpect(content().string(""));
    }
}
