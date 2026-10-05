package com.backendSiege.api.controller;

import com.backendSiege.api.mapper.AlerteMapperImpl;
import com.backendSiege.api.mapper.HistoriqueMapperImpl;
import com.backendSiege.api.security.JwtAuthenticationFilter;
import com.backendSiege.domain.models.Alerte;
import com.backendSiege.domain.models.Historique;
import com.backendSiege.domain.models.TypeHistoriqueEnum;
import com.backendSiege.domain.services.AlerteService;
import com.backendSiege.domain.services.HistoriqueService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {HistoriqueController.class, AlerteController.class},
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtAuthenticationFilter.class))
@AutoConfigureMockMvc(addFilters = false)
@Import({HistoriqueMapperImpl.class, AlerteMapperImpl.class})
class HistoriqueAlerteControllerTest {

    private static final UUID ID_LOT = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID ID_ENTREPOT = UUID.fromString("770e8400-e29b-41d4-a716-446655440011");
    private static final UUID ID_HISTORIQUE = UUID.fromString("33333333-3333-3333-3333-333333333333");
    private static final UUID ID_ZONE = UUID.fromString("22222222-2222-2222-2222-222222222222");

    private static final Historique HISTORIQUE = new Historique(ID_HISTORIQUE, "ALERTE", LocalDateTime.of(2026, 1, 11, 9, 0), null, ID_ZONE, "Alerte");
    private static final String HISTORIQUE_JSON = "{\"idHistorique\":\"" + ID_HISTORIQUE + "\",\"type\":\"ALERTE\","
            + "\"dateEmission\":\"2026-01-11T09:00:00\",\"zonePrecedente\":null,\"zoneActuelle\":\"" + ID_ZONE + "\",\"detail\":\"Alerte\"}";

    @Autowired
    MockMvc mockMvc;
    @MockitoBean
    HistoriqueService historiqueService;
    @MockitoBean
    AlerteService alerteService;

    // ------------------------------------------------------------------ historique

    @Test
    void historique_sans_type_n_applique_aucun_filtre() throws Exception {
        when(historiqueService.findAllHistoriqueForLot(ID_LOT, null)).thenReturn(List.of(HISTORIQUE));

        mockMvc.perform(get("/history/" + ID_LOT))
                .andExpect(status().isOk())
                .andExpect(content().string("[" + HISTORIQUE_JSON + "]"));
    }

    @Test
    void historique_avec_un_type_vide_n_applique_aucun_filtre() throws Exception {
        when(historiqueService.findAllHistoriqueForLot(ID_LOT, null)).thenReturn(List.of());

        mockMvc.perform(get("/history/" + ID_LOT).param("type", " ")).andExpect(status().isOk()).andExpect(content().string("[]"));
    }

    @Test
    void historique_interprete_le_type_sans_tenir_compte_de_la_casse_et_accepte_alert() throws Exception {
        when(historiqueService.findAllHistoriqueForLot(ID_LOT, TypeHistoriqueEnum.ALERTE)).thenReturn(List.of(HISTORIQUE));
        when(historiqueService.findAllHistoriqueForLot(ID_LOT, TypeHistoriqueEnum.SHIPPED)).thenReturn(List.of());

        mockMvc.perform(get("/history/" + ID_LOT + "?type=alert")).andExpect(content().string("[" + HISTORIQUE_JSON + "]"));
        mockMvc.perform(get("/history/" + ID_LOT + "?type=Alerte")).andExpect(content().string("[" + HISTORIQUE_JSON + "]"));
        mockMvc.perform(get("/history/" + ID_LOT + "?type=shipped")).andExpect(content().string("[]"));
    }

    @Test
    void historique_avec_un_type_inconnu_est_une_requete_invalide_sans_corps() throws Exception {
        mockMvc.perform(get("/history/" + ID_LOT + "?type=inconnu")).andExpect(status().isBadRequest()).andExpect(content().string(""));

        verifyNoInteractions(historiqueService);
    }

    // ------------------------------------------------------------------ alertes

    @Test
    void alertes_transmettent_l_entrepot_et_la_periode() throws Exception {
        when(alerteService.getAllAlertesByEntrepotId(ID_ENTREPOT, LocalDateTime.of(2026, 1, 1, 10, 0), LocalDateTime.of(2026, 1, 2, 0, 0)))
                .thenReturn(List.of(new Alerte("a1", "HAUTETEMPERATURE", "Trop chaud", LocalDateTime.of(2026, 1, 12, 10, 5), ID_ZONE.toString())));

        mockMvc.perform(get("/alertes?idEntrepot=" + ID_ENTREPOT + "&startDate=2026-01-01T10:00&endDate=2026-01-02T00:00"))
                .andExpect(status().isOk())
                .andExpect(content().string("[{\"idAlerte\":\"a1\",\"typeAlerte\":\"HAUTETEMPERATURE\",\"description\":\"Trop chaud\","
                        + "\"dateEmission\":\"2026-01-12T10:05:00\",\"idZone\":\"" + ID_ZONE + "\"}]"));
    }

    @Test
    void alertes_sans_periode_ne_bornent_pas_la_recherche_et_ignorent_le_pays() throws Exception {
        when(alerteService.getAllAlertesByEntrepotId(ID_ENTREPOT, null, null)).thenReturn(List.of());

        mockMvc.perform(get("/alertes?idEntrepot=" + ID_ENTREPOT + "&pays=bresil")).andExpect(status().isOk()).andExpect(content().string("[]"));
        verify(alerteService).getAllAlertesByEntrepotId(ID_ENTREPOT, null, null);
    }

    @Test
    void alertes_exigent_un_identifiant_d_entrepot_valide() throws Exception {
        mockMvc.perform(get("/alertes")).andExpect(status().isBadRequest());
        mockMvc.perform(get("/alertes?idEntrepot=pas-un-uuid")).andExpect(status().isBadRequest());

        verifyNoInteractions(alerteService);
    }
}
