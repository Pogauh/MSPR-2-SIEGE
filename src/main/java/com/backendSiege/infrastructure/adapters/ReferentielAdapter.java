package com.backendSiege.infrastructure.adapters;

import com.backendSiege.api.dto.MesureResponseDto;
import com.backendSiege.domain.model.Entrepot;
import com.backendSiege.domain.model.Exploitation;
import com.backendSiege.domain.model.ZoneResponse;
import com.backendSiege.domain.port.ReferentielPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
public class ReferentielAdapter implements ReferentielPort {

    private static final Logger log = LoggerFactory.getLogger(MesureAdapter.class);
    private final Map<String, WebClient> countryWebClients;

    public ReferentielAdapter(Map<String, WebClient> countryWebClients) {
        this.countryWebClients = countryWebClients;
    }

    @Override
    public List<Exploitation> findAllExploitationsByPays(String pays) {
        WebClient client = countryWebClients.get(pays.toUpperCase());

        if (client == null) {
            log.error("Tentative d'appel sur un pays non configuré : {}", pays);
            return null;
        }
        return client.get()
                .uri("/exploitation/allExploitations")
                .retrieve()
                .bodyToFlux(Exploitation.class)
                .collectList()
                .doOnError(e -> log.error("Erreur lors de l'appel pour le pays {}", pays))
                .onErrorResume(ex -> {
                    log.error("Erreur technique lors de l'appel pays {}", pays, ex);
                    return Mono.empty();
                })
                .block();
    }

    @Override
    public List<Entrepot> findAllEntrepotsByPays(String pays) {
        WebClient client = countryWebClients.get(pays.toUpperCase());

        if (client == null) {
            log.error("Tentative d'appel sur un pays non configuré : {}", pays);
            return null;
        }
        return client.get()
                .uri("/entrepots")
                .retrieve()
                .bodyToFlux(Entrepot.class)
                .collectList()
                .doOnError(e -> log.error("Erreur lors de l'appel pour le pays {}", pays))
                .onErrorResume(ex -> {
                    log.error("Erreur technique lors de l'appel pays {}", pays, ex);
                    return Mono.empty();
                })
                .block();
    }

    @Override
    public List<ZoneResponse> findAllZonesByEntrepot(String pays, UUID entrepotId) {
        WebClient client = countryWebClients.get(pays.toUpperCase());

        if (client == null) {
            log.error("Tentative d'appel sur un pays non configuré : {}", entrepotId);
            return null;
        }
      return client.get()
                .uri("/entrepots/{entrepotId}/zones", entrepotId)
                .retrieve()
                .bodyToFlux(ZoneResponse.class)
                .collectList()
                .doOnError(e -> log.error("Erreur lors de l'appel pour le pays FRANCE"))
                .onErrorResume(ex -> {
                    log.error("Erreur technique lors de l'appel pays FRANCE", ex);
                    return Mono.empty();
                })
                .block();
    }
}