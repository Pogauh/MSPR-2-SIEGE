package com.backendSiege.api.controller;

import com.backendSiege.api.mapper.InventaireDtoMapper;
import com.backendSiege.api.mapper.InventaireDtoMapperImpl;
import com.backendSiege.domain.model.Entrepot;
import com.backendSiege.domain.model.Exploitation;
import com.backendSiege.domain.model.Inventaire;
import com.backendSiege.domain.model.Pays;
import com.backendSiege.domain.service.InventaireService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.UUID;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class InventaireControllerTest {

    private InventaireService inventaireService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        inventaireService = mock(InventaireService.class);
        InventaireDtoMapper mapper = new InventaireDtoMapperImpl();
        mockMvc = MockMvcBuilders.standaloneSetup(new InventaireController(inventaireService, mapper)).build();
    }

    @Test
    void getInventaireMondial_shouldReturnOkWithPayload() throws Exception {
        Entrepot entrepot = new Entrepot(UUID.randomUUID(), "Bordeaux", "Depot Sud");
        Exploitation exploitation = new Exploitation(UUID.randomUUID(), "Exploit Sud", "5 quai");
        Pays pays = new Pays(UUID.randomUUID(), "France");
        Inventaire inventaire = new Inventaire(UUID.randomUUID(), "OK", 50, 2, LocalDateTime.now(), LocalDateTime.now(), entrepot, exploitation, pays);

        when(inventaireService.getInventaireMondial()).thenReturn(Collections.singletonList(inventaire));

        mockMvc.perform(get("/api/inventaires/mondial")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].entrepotNom").value("Depot Sud"))
                .andExpect(jsonPath("$[0].paysNom").value("France"));
    }

    @Test
    void getInventaireMondial_shouldReturnNoContentWhenEmpty() throws Exception {
        when(inventaireService.getInventaireMondial()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/inventaires/mondial")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }
}

