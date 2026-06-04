package com.backendSiege.infrastructure.adapters;

import com.backendSiege.domain.port.MesurePort;
import com.backendSiege.api.dto.MesureResponseDto;
import com.backendSiege.domain.model.CapteurSummary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
public class MesureAdapter implements MesurePort {

    private static final Logger log = LoggerFactory.getLogger(MesureAdapter.class);
    private final Map<String, WebClient> countryWebClients;

    public MesureAdapter(Map<String, WebClient> countryWebClients) {
        this.countryWebClients = countryWebClients;
    }

    @Override
    public MesureResponseDto findMesureById(String nomPays, UUID id) {
        WebClient client = countryWebClients.get(nomPays.toUpperCase());

        if (client == null) {
            log.error("Tentative d'appel sur un pays non configuré : {}", nomPays);
            return null;
        }

        return client.get()
                .uri("/mesures/getMesure/{id}", id)
                .retrieve()
                .bodyToMono(MesureResponseDto.class)
                .doOnError(e -> log.error("Erreur lors de l'appel pour le pays {}", nomPays))
                .onErrorResume(ex -> {
                    log.error("Erreur technique lors de l'appel pays {}", nomPays, ex);
                    return Mono.empty();
                })
                .block();
    }

    public List<MesureResponseDto> findMesuresByCapteurId(String pays, UUID idCapteur) {
        WebClient client = countryWebClients.get(pays.toUpperCase());

        if (client == null) return List.of();

        return client.get()
                .uri("/capteurs/{id}/mesures", idCapteur)
                .retrieve()
                .bodyToFlux(MesureResponseDto.class)
                .onErrorResume(e -> Flux.empty())
                .collectList()
                .block();
    }

    @Override
    public List<CapteurSummary> findCapteurSummaryByEntrepotId(String pays, UUID idEntrepot) {
        WebClient client = countryWebClients.get(pays.toUpperCase());

        if (client == null) return List.of();
        return client.get()
                .uri("/entrepots/{entrepotId}/capteurSummary", idEntrepot)
                .retrieve()
                .bodyToFlux(CapteurSummary.class)
                .onErrorResume(e -> Flux.empty())
                .collectList()
                .block();
    }
}
