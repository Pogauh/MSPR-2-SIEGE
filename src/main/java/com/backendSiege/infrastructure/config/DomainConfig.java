package com.backendSiege.infrastructure.config;

import com.backendSiege.domain.ports.AlertePort;
import com.backendSiege.domain.ports.HistoriquePort;
import com.backendSiege.domain.ports.InventairePort;
import com.backendSiege.domain.ports.MesurePort;
import com.backendSiege.domain.ports.PasswordHasherPort;
import com.backendSiege.domain.ports.PaysPort;
import com.backendSiege.domain.ports.ReferentielPort;
import com.backendSiege.domain.ports.ReportingPort;
import com.backendSiege.domain.ports.TokenPort;
import com.backendSiege.domain.ports.UserPort;
import com.backendSiege.domain.services.AlerteService;
import com.backendSiege.domain.services.AuthService;
import com.backendSiege.domain.services.HistoriqueService;
import com.backendSiege.domain.services.InventaireService;
import com.backendSiege.domain.services.MesureService;
import com.backendSiege.domain.services.PaysService;
import com.backendSiege.domain.services.ReferentielService;
import com.backendSiege.domain.services.ReportingService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Assemble le domaine : les services métier ne connaissent pas Spring, c'est cette configuration
 * qui les instancie en leur fournissant les adaptateurs qui réalisent les ports.
 */
@Configuration
public class DomainConfig {

    @Bean
    public PaysService paysService(PaysPort paysPort) {
        return new PaysService(paysPort);
    }

    @Bean
    public InventaireService inventaireService(InventairePort inventairePort, PaysService paysService) {
        return new InventaireService(inventairePort, paysService);
    }

    @Bean
    public MesureService mesureService(MesurePort mesurePort, PaysService paysService) {
        return new MesureService(mesurePort, paysService);
    }

    @Bean
    public ReferentielService referentielService(ReferentielPort referentielPort, PaysService paysService) {
        return new ReferentielService(referentielPort, paysService);
    }

    @Bean
    public HistoriqueService historiqueService(HistoriquePort historiquePort, PaysService paysService) {
        return new HistoriqueService(historiquePort, paysService);
    }

    @Bean
    public AlerteService alerteService(AlertePort alertePort, PaysService paysService) {
        return new AlerteService(alertePort, paysService);
    }

    @Bean
    public ReportingService reportingService(ReportingPort reportingPort, PaysService paysService) {
        return new ReportingService(reportingPort, paysService);
    }

    @Bean
    public AuthService authService(UserPort userPort, PasswordHasherPort passwordHasherPort, TokenPort tokenPort) {
        return new AuthService(userPort, passwordHasherPort, tokenPort);
    }
}
