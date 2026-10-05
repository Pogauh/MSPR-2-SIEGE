package com.backendSiege.api.controller;

import com.backendSiege.api.mapper.HistoriqueMapperImpl;
import com.backendSiege.api.mapper.LotMapperImpl;
import com.backendSiege.api.security.JwtAuthenticationFilter;
import com.backendSiege.domain.exceptions.PaysIndisponibleException;
import com.backendSiege.domain.models.Historique;
import com.backendSiege.domain.models.Lot;
import com.backendSiege.domain.models.MesureLot;
import com.backendSiege.domain.models.NouveauLot;
import com.backendSiege.domain.models.ResultatLot;
import com.backendSiege.domain.models.ResultatSortieStock;
import com.backendSiege.domain.services.InventaireService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = InventaireController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtAuthenticationFilter.class))
@AutoConfigureMockMvc(addFilters = false)
@Import({LotMapperImpl.class, HistoriqueMapperImpl.class})
class InventaireControllerTest {

    private static final UUID ID_LOT = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID ID_ZONE = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final UUID ID_HISTORIQUE = UUID.fromString("33333333-3333-3333-3333-333333333333");
    private static final UUID ID_EXPLOITATION = UUID.fromString("aa0e8400-e29b-41d4-a716-446655440022");
    private static final UUID ID_ENTREPOT = UUID.fromString("770e8400-e29b-41d4-a716-446655440011");

    private static final Lot LOT = new Lot(ID_LOT, "COMPLIANT", 120, 2, null, LocalDateTime.of(2026, 1, 10, 8, 30),
            LocalDate.of(2025, 12, 1), ID_ZONE.toString(), ID_EXPLOITATION, ID_ENTREPOT,
            List.of(new Historique(ID_HISTORIQUE, "TRANSFERT", LocalDateTime.of(2026, 1, 11, 9, 0), null, ID_ZONE, "Transfert")));
    private static final String LOT_JSON = "{\"idLot\":\"" + ID_LOT + "\",\"status\":\"COMPLIANT\",\"quantite\":120,\"caracteristique\":2,"
            + "\"dateSortie\":null,\"dateStockage\":\"2026-01-10T08:30:00\",\"dateProduction\":\"2025-12-01\","
            + "\"emplacement\":\"" + ID_ZONE + "\",\"idExploitation\":\"" + ID_EXPLOITATION + "\",\"idEntrepot\":\"" + ID_ENTREPOT + "\","
            + "\"historique\":[{\"idHistorique\":\"" + ID_HISTORIQUE + "\",\"type\":\"TRANSFERT\",\"dateEmission\":\"2026-01-11T09:00:00\","
            + "\"zonePrecedente\":null,\"zoneActuelle\":\"" + ID_ZONE + "\",\"detail\":\"Transfert\"}]}";

    @Autowired
    MockMvc mockMvc;
    @MockitoBean
    InventaireService service;

    // ------------------------------------------------------------------ lecture

    @Test
    void total_renvoie_la_quantite_en_stock() throws Exception {
        when(service.getQuantiteTotale()).thenReturn(150);

        mockMvc.perform(get("/api/lot/total")).andExpect(status().isOk()).andExpect(content().string("150"));
    }

    @Test
    void total_nul_est_introuvable() throws Exception {
        when(service.getQuantiteTotale()).thenReturn(0);

        mockMvc.perform(get("/api/lot/total")).andExpect(status().isNotFound()).andExpect(content().string(""));
    }

    @Test
    void lot_renvoie_le_lot_dans_l_ordre_du_contrat() throws Exception {
        when(service.getLotById(ID_LOT)).thenReturn(Optional.of(LOT));

        mockMvc.perform(get("/lot/" + ID_LOT)).andExpect(status().isOk()).andExpect(content().string(LOT_JSON));
    }

    @Test
    void lot_inconnu_est_introuvable() throws Exception {
        when(service.getLotById(ID_LOT)).thenReturn(Optional.empty());

        mockMvc.perform(get("/lot/" + ID_LOT)).andExpect(status().isNotFound()).andExpect(content().string(""));
    }

    @Test
    void lot_avec_un_identifiant_invalide_est_une_requete_invalide() throws Exception {
        mockMvc.perform(get("/lot/pas-un-uuid")).andExpect(status().isBadRequest());
    }

    @Test
    void lots_renvoie_les_lots_du_service_dans_l_ordre_recu() throws Exception {
        when(service.getAllLots("bresil")).thenReturn(List.of(LOT, LOT));

        mockMvc.perform(get("/lots?pays=bresil"))
                .andExpect(status().isOk())
                .andExpect(content().string("[" + LOT_JSON + "," + LOT_JSON + "]"));
    }

    @Test
    void lots_sans_pays_interroge_le_service_sans_filtre_et_renvoie_une_liste_vide_s_il_n_y_a_rien() throws Exception {
        when(service.getAllLots(null)).thenReturn(List.of());

        mockMvc.perform(get("/lots")).andExpect(status().isOk()).andExpect(content().string("[]"));
    }

    @Test
    void mesures_d_un_lot() throws Exception {
        when(service.getAllMesuresByLotId(ID_LOT)).thenReturn(List.of(new MesureLot(27.5, 56.2, LocalDateTime.of(2026, 1, 12, 10, 0))));

        mockMvc.perform(get("/lot/" + ID_LOT + "/mesures"))
                .andExpect(status().isOk())
                .andExpect(content().string("[{\"temperature\":27.5,\"humidite\":56.2,\"dateMesure\":\"2026-01-12T10:00:00\"}]"));
    }

    @Test
    void lot_le_plus_ancien_d_un_pays_ou_de_tous() throws Exception {
        when(service.getOldestLot("bresil")).thenReturn(Optional.of(LOT));
        when(service.getOldestLot(null)).thenReturn(Optional.empty());

        mockMvc.perform(get("/lot/oldest?pays=bresil")).andExpect(status().isOk()).andExpect(content().string(LOT_JSON));
        mockMvc.perform(get("/lot/oldest")).andExpect(status().isNotFound()).andExpect(content().string(""));
    }

    // ------------------------------------------------------------------ écritures

    @Test
    void creation_transmet_le_pays_et_le_lot_a_creer_et_renvoie_le_resultat() throws Exception {
        when(service.createLot("colombie", new NouveauLot("COMPLIANT", 120, 2, LocalDate.of(2026, 3, 15), ID_EXPLOITATION, ID_ZONE)))
                .thenReturn(Optional.of(new ResultatLot(ID_LOT, "CREATED")));

        mockMvc.perform(post("/lot?pays=colombie").contentType(MediaType.APPLICATION_JSON).content(
                        "{\"status\":\"COMPLIANT\",\"quantite\":120,\"caracteristique\":2,\"dateProduction\":\"2026-03-15\","
                                + "\"idExploitation\":\"" + ID_EXPLOITATION + "\",\"idZone\":\"" + ID_ZONE + "\",\"inconnu\":1}"))
                .andExpect(status().isOk())
                .andExpect(content().string("{\"idLot\":\"" + ID_LOT + "\",\"status\":\"CREATED\"}"));
    }

    @Test
    void creation_sans_resultat_renvoie_200_sans_corps() throws Exception {
        when(service.createLot("atlantide", new NouveauLot(null, null, null, null, null, null))).thenReturn(Optional.empty());

        mockMvc.perform(post("/lot?pays=atlantide").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isOk())
                .andExpect(content().string(""));
    }

    @Test
    void creation_exige_le_pays_et_un_corps_lisible() throws Exception {
        mockMvc.perform(post("/lot").contentType(MediaType.APPLICATION_JSON).content("{}")).andExpect(status().isBadRequest());
        mockMvc.perform(post("/lot?pays=bresil").contentType(MediaType.APPLICATION_JSON).content("{pas du json")).andExpect(status().isBadRequest());
    }

    @Test
    void changement_de_statut_transmet_le_pays_le_lot_et_le_statut() throws Exception {
        when(service.updateStatusLot("bresil", ID_LOT, "BLOCKED")).thenReturn(Optional.of(new ResultatLot(ID_LOT, "OK")));
        when(service.updateStatusLot("bresil", ID_LOT, null)).thenReturn(Optional.empty());

        mockMvc.perform(put("/lot/" + ID_LOT + "/status?pays=bresil").contentType(MediaType.APPLICATION_JSON).content("{\"status\":\"BLOCKED\"}"))
                .andExpect(status().isOk())
                .andExpect(content().string("{\"idLot\":\"" + ID_LOT + "\",\"status\":\"OK\"}"));
        mockMvc.perform(put("/lot/" + ID_LOT + "/status?pays=bresil").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isOk())
                .andExpect(content().string(""));
    }

    @Test
    void transfert_de_zone_transmet_le_pays_le_lot_et_la_zone() throws Exception {
        when(service.updateEmplacementLot("bresil", ID_LOT, ID_ZONE)).thenReturn(Optional.of(new ResultatLot(ID_LOT, "OK")));

        mockMvc.perform(put("/lot/" + ID_LOT + "/zones?pays=bresil").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"idZone\":\"" + ID_ZONE + "\"}"))
                .andExpect(status().isOk())
                .andExpect(content().string("{\"idLot\":\"" + ID_LOT + "\",\"status\":\"OK\"}"));
    }

    @Test
    void transfert_vers_un_pays_qui_ne_repond_pas_laisse_remonter_l_erreur() {
        when(service.updateEmplacementLot("bresil", ID_LOT, ID_ZONE)).thenThrow(new PaysIndisponibleException("BRESIL", new RuntimeException()));

        assertThatThrownBy(() -> mockMvc.perform(put("/lot/" + ID_LOT + "/zones?pays=bresil").contentType(MediaType.APPLICATION_JSON)
                .content("{\"idZone\":\"" + ID_ZONE + "\"}")))
                .hasCauseInstanceOf(PaysIndisponibleException.class);
    }

    @Test
    void sortie_de_stock_transmet_le_pays_et_le_lot() throws Exception {
        when(service.stockOut("bresil", ID_LOT)).thenReturn(Optional.of(new ResultatSortieStock(ID_HISTORIQUE, "CREATED")));

        mockMvc.perform(post("/mouvements/stockout?idLot=" + ID_LOT + "&pays=bresil"))
                .andExpect(status().isOk())
                .andExpect(content().string("{\"idHistorique\":\"" + ID_HISTORIQUE + "\",\"status\":\"CREATED\"}"));
        verify(service).stockOut("bresil", ID_LOT);
    }

    @Test
    void sortie_de_stock_exige_le_lot_et_le_pays() throws Exception {
        mockMvc.perform(post("/mouvements/stockout?pays=bresil")).andExpect(status().isBadRequest());
        mockMvc.perform(post("/mouvements/stockout?idLot=" + ID_LOT)).andExpect(status().isBadRequest());
    }
}
