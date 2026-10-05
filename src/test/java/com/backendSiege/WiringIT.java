package com.backendSiege;

import com.backendSiege.domain.services.AlerteService;
import com.backendSiege.domain.services.AuthService;
import com.backendSiege.domain.services.HistoriqueService;
import com.backendSiege.domain.services.InventaireService;
import com.backendSiege.domain.services.MesureService;
import com.backendSiege.domain.services.PaysService;
import com.backendSiege.domain.services.ReferentielService;
import com.backendSiege.domain.services.ReportingService;
import com.backendSiege.infrastructure.client.PaysClients;
import com.backendSiege.infrastructure.config.JwtProperties;
import com.backendSiege.support.IntegrationTestBase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/** Assemblage : les services du domaine, créés par {@code DomainConfig}, sont tous disponibles avec leurs adaptateurs. */
class WiringIT extends IntegrationTestBase {

    @Autowired
    ApplicationContext contexte;
    @Autowired
    PaysService paysService;
    @Autowired
    PaysClients paysClients;
    @Autowired
    JwtProperties jwtProperties;

    @Test
    void chaque_service_du_domaine_est_un_bean_unique() {
        for (Class<?> type : List.of(PaysService.class, InventaireService.class, MesureService.class, ReferentielService.class,
                HistoriqueService.class, AlerteService.class, ReportingService.class, AuthService.class)) {
            assertThat(contexte.getBeanNamesForType(type)).as(type.getSimpleName()).hasSize(1);
        }
    }

    @Test
    void les_services_du_domaine_ne_sont_pas_des_composants_scannes() {
        for (Class<?> type : List.of(InventaireService.class, AuthService.class, PaysService.class)) {
            assertThat(type.isAnnotationPresent(Service.class)).isFalse();
            assertThat(type.isAnnotationPresent(Component.class)).isFalse();
        }
    }

    @Test
    void les_pays_configures_sont_ceux_de_l_application_avec_un_client_chacun() {
        assertThat(paysService.codes()).containsExactly("BRESIL", "COLOMBIE", "EQUATEUR");
        for (String code : paysService.codes()) {
            assertThat(paysClients.get(code).code()).isEqualTo(code);
        }
    }

    @Test
    void la_configuration_des_jetons_provient_de_l_application() {
        assertThat(jwtProperties.expiration()).isEqualTo(Duration.ofHours(24));
        assertThat(jwtProperties.secret()).hasSize(64);
    }
}
