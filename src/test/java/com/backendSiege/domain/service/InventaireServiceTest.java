package com.backendSiege.domain.service;

import com.backendSiege.domain.model.Entrepot;
import com.backendSiege.domain.model.Exploitation;
import com.backendSiege.domain.model.Inventaire;
import com.backendSiege.domain.model.Pays;
import com.backendSiege.domain.port.InventairePort;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

class InventaireServiceTest {

    @Test
    void getInventaireMondial_shouldReturnDataFromPort() {
        InventairePort port = Mockito.mock(InventairePort.class);
        InventaireService service = new InventaireService(port);

        Entrepot entrepot = new Entrepot(UUID.randomUUID(), "Paris", "Depot A");
        Exploitation exploitation = new Exploitation(UUID.randomUUID(), "Exploit 1", "1 rue test");
        Pays pays = new Pays(UUID.randomUUID(), "France");
        Inventaire inventaire = new Inventaire(UUID.randomUUID(), "OK", 10, 1, LocalDateTime.now(), LocalDateTime.now(), entrepot, exploitation, pays);

        when(port.findInventaireMondial()).thenReturn(Collections.singletonList(inventaire));

        List<Inventaire> result = service.getInventaireMondial();

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getEntrepot().getNom()).isEqualTo("Depot A");
    }
}