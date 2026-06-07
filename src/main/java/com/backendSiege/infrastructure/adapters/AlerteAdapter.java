package com.backendSiege.infrastructure.adapters;

import com.backendSiege.domain.model.Alerte;
import com.backendSiege.domain.model.Historique;
import com.backendSiege.domain.port.AlertePort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
public class AlerteAdapter implements AlertePort {

    private static final Logger log = LoggerFactory.getLogger(AlerteAdapter.class);
    private final Map<String, WebClient> countryWebClients;

    public AlerteAdapter(Map<String, WebClient> countryWebClients) {
        this.countryWebClients = countryWebClients;
    }

    @Override
    public List<Alerte> findAlertesByEntrepotId(String pays, UUID idEntrepot, LocalDateTime startDate, LocalDateTime endDate) {
        return Flux.fromIterable(countryWebClients.values())
                .flatMap(webClient -> webClient.get()
                        .uri(uriBuilder -> uriBuilder
                                .path("alertes")
                                .queryParam("idEntrepot", idEntrepot.toString())
                                .queryParamIfPresent("startDate", java.util.Optional.ofNullable(startDate))
                                .queryParamIfPresent("endDate", java.util.Optional.ofNullable(endDate))
                                .build())
                        .exchangeToFlux(response -> {
                            if (response.statusCode().is2xxSuccessful()) {
                                return response.bodyToFlux(Alerte.class)
                                        .doOnError(err -> log.error("Erreur de parsing récupération des alertes pour une zone", err));
                            }
                            return Flux.empty();
                        })
                        .onErrorResume(ex -> {
                            log.error("Appel findAlertesByEntrepotId KO pour l'adapter AlerteAdapter", ex);
                            return Mono.empty();
                        })
                )
                .collectList()
                .blockOptional()
                .orElse(List.of());
    }
}
