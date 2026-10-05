package com.backendSiege.api.controller;

import com.backendSiege.api.mapper.ReportingMapperImpl;
import com.backendSiege.api.security.JwtAuthenticationFilter;
import com.backendSiege.domain.exceptions.PaysIntrouvableException;
import com.backendSiege.domain.exceptions.PaysNonSupporteException;
import com.backendSiege.domain.exceptions.PaysObligatoireException;
import com.backendSiege.domain.models.ReportingGlobal;
import com.backendSiege.domain.models.ReportingQuality;
import com.backendSiege.domain.models.ReportingStock;
import com.backendSiege.domain.services.ReportingService;
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
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ReportingController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtAuthenticationFilter.class))
@AutoConfigureMockMvc(addFilters = false)
@Import(ReportingMapperImpl.class)
class ReportingControllerTest {

    private static final LocalDateTime DEBUT = LocalDateTime.of(2026, 1, 1, 0, 0);
    private static final LocalDateTime FIN = LocalDateTime.of(2026, 1, 31, 0, 0);
    private static final String PERIODE = "startDate=2026-01-01T00:00:00&endDate=2026-01-31T00:00:00";

    @Autowired
    MockMvc mockMvc;
    @MockitoBean
    ReportingService service;

    @Test
    void global_renvoie_les_indicateurs_consolides_dans_l_ordre_du_contrat() throws Exception {
        when(service.getReportingGlobal()).thenReturn(new ReportingGlobal(40, 45.0, 95.0, 10));

        mockMvc.perform(get("/reporting/global"))
                .andExpect(status().isOk())
                .andExpect(content().string("{\"avgAgeDays\":45.0,\"complianceRate\":95.0,\"countMouvementLast30Days\":10,\"totalLots\":40}"));
    }

    @Test
    void quality_d_un_pays_transmet_le_pays_et_la_periode() throws Exception {
        when(service.getReportingQuality("bresil", DEBUT, FIN)).thenReturn(new ReportingQuality(4, 80.0, 1));

        mockMvc.perform(get("/reporting/quality?pays=bresil&" + PERIODE))
                .andExpect(status().isOk())
                .andExpect(content().string("{\"alertCount\":4,\"complianceRate\":80.0,\"nonComplianceCount\":1}"));
    }

    @Test
    void quality_sans_pays_est_consolide_sur_tous_les_pays() throws Exception {
        when(service.getReportingQuality(null, DEBUT, FIN)).thenReturn(new ReportingQuality(5, 70.0, 3));

        mockMvc.perform(get("/reporting/quality?" + PERIODE))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"alertCount\":5,\"complianceRate\":70.0,\"nonComplianceCount\":3}", true));
    }

    @Test
    void quality_d_un_pays_non_supporte_est_une_requete_invalide() throws Exception {
        when(service.getReportingQuality("atlantide", DEBUT, FIN)).thenThrow(new PaysNonSupporteException("atlantide"));

        mockMvc.perform(get("/reporting/quality?pays=atlantide&" + PERIODE)).andExpect(status().isBadRequest());
    }

    @Test
    void stock_renvoie_le_stock_du_pays_dans_l_ordre_du_contrat() throws Exception {
        UUID urgent = UUID.fromString("f1f1f1f1-f1f1-f1f1-f1f1-f1f1f1f1f1f1");
        when(service.getReportingStock("bresil", DEBUT, FIN)).thenReturn(new ReportingStock(3, 12.5, 66.7, List.of(urgent)));

        mockMvc.perform(get("/reporting/stock?pays=bresil&" + PERIODE))
                .andExpect(status().isOk())
                .andExpect(content().string("{\"totalLots\":3,\"avgAgeDays\":12.5,\"complianceRate\":66.7,\"lotIdsUrgentFifo\":[\"" + urgent + "\"]}"));
    }

    @Test
    void stock_d_un_pays_sans_reponse_renvoie_des_zeros_et_une_liste_nulle() throws Exception {
        when(service.getReportingStock("bresil", DEBUT, FIN)).thenReturn(ReportingStock.vide());

        mockMvc.perform(get("/reporting/stock?pays=bresil&" + PERIODE))
                .andExpect(content().string("{\"totalLots\":0,\"avgAgeDays\":0.0,\"complianceRate\":0.0,\"lotIdsUrgentFifo\":null}"));
    }

    @Test
    void stock_sans_pays_est_une_requete_invalide() throws Exception {
        when(service.getReportingStock("", DEBUT, FIN)).thenThrow(new PaysObligatoireException());

        mockMvc.perform(get("/reporting/stock?pays=&" + PERIODE)).andExpect(status().isBadRequest());
        mockMvc.perform(get("/reporting/stock?" + PERIODE)).andExpect(status().isBadRequest());
    }

    @Test
    void stock_d_un_pays_inconnu_est_introuvable() throws Exception {
        when(service.getReportingStock("atlantide", DEBUT, FIN)).thenThrow(new PaysIntrouvableException("atlantide"));

        mockMvc.perform(get("/reporting/stock?pays=atlantide&" + PERIODE)).andExpect(status().isNotFound());
        verify(service).getReportingStock("atlantide", DEBUT, FIN);
    }

    @Test
    void quality_et_stock_exigent_les_deux_dates() throws Exception {
        mockMvc.perform(get("/reporting/quality")).andExpect(status().isBadRequest());
        mockMvc.perform(get("/reporting/quality?startDate=2026-01-01T00:00:00")).andExpect(status().isBadRequest());
        mockMvc.perform(get("/reporting/stock?pays=bresil")).andExpect(status().isBadRequest());
        mockMvc.perform(get("/reporting/stock?pays=bresil&endDate=2026-01-31T00:00:00")).andExpect(status().isBadRequest());
    }
}
