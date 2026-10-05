package com.backendSiege.domain.services;

import com.backendSiege.domain.exceptions.PaysIndisponibleException;
import com.backendSiege.domain.models.Entrepot;
import com.backendSiege.domain.models.Exploitation;
import com.backendSiege.domain.models.Pays;
import com.backendSiege.domain.models.Zone;
import com.backendSiege.domain.ports.ReferentielPort;
import com.backendSiege.domain.support.Fixtures;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static com.backendSiege.domain.support.Fixtures.ID_ENTREPOT;
import static com.backendSiege.domain.support.Fixtures.ID_ZONE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReferentielServiceTest {

    @Mock
    ReferentielPort port;

    private ReferentielService service() {
        return new ReferentielService(port, Fixtures.paysService());
    }

    @Test
    void getAllPays_renvoie_les_pays_configures() {
        assertThat(service().getAllPays()).extracting(Pays::code).containsExactly("BRESIL", "COLOMBIE", "EQUATEUR");
    }

    @Test
    void getAllExploitationsByPays_interroge_le_pays_designe() {
        List<Exploitation> exploitations = List.of(new Exploitation(UUID.randomUUID(), "Ferme", "Rue 1"));
        when(port.findExploitations("BRESIL")).thenReturn(exploitations);

        assertThat(service().getAllExploitationsByPays("bresil")).isEqualTo(exploitations);
    }

    @Test
    void getAllEntrepotsByPays_interroge_le_pays_designe() {
        List<Entrepot> entrepots = List.of(new Entrepot(ID_ENTREPOT, "Santos", "Principal"));
        when(port.findEntrepots("COLOMBIE")).thenReturn(entrepots);

        assertThat(service().getAllEntrepotsByPays(" Colombie")).isEqualTo(entrepots);
    }

    @Test
    void getAllZonesByEntrepot_interroge_le_pays_designe() {
        List<Zone> zones = List.of(new Zone(ID_ZONE, "A", ID_ENTREPOT, UUID.randomUUID(), 3L));
        when(port.findZones("EQUATEUR", ID_ENTREPOT)).thenReturn(zones);

        assertThat(service().getAllZonesByEntrepot("equateur", ID_ENTREPOT)).isEqualTo(zones);
    }

    @Test
    void un_pays_inconnu_n_a_ni_exploitation_ni_entrepot_ni_zone_et_n_interroge_personne() {
        ReferentielService service = service();

        assertThat(service.getAllExploitationsByPays("atlantide")).isEmpty();
        assertThat(service.getAllEntrepotsByPays("atlantide")).isEmpty();
        assertThat(service.getAllZonesByEntrepot("atlantide", ID_ENTREPOT)).isEmpty();

        verifyNoInteractions(port);
    }

    @Test
    void un_pays_qui_ne_repond_pas_laisse_remonter_l_erreur() {
        when(port.findEntrepots("BRESIL")).thenThrow(new PaysIndisponibleException("BRESIL", new RuntimeException("boom")));

        assertThatThrownBy(() -> service().getAllEntrepotsByPays("bresil")).isInstanceOf(PaysIndisponibleException.class);
    }
}
