package com.backendSiege.infrastructure.adapters;

import com.backendSiege.domain.model.Historique;
import com.backendSiege.domain.port.HistoriquePort;
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
public class HistoriqueAdapter implements HistoriquePort {

    private static final Logger log = LoggerFactory.getLogger(HistoriqueAdapter.class);
    private final Map<String, WebClient> countryWebClients;

    public HistoriqueAdapter(Map<String, WebClient> countryWebClients) {
        this.countryWebClients = countryWebClients;
    }

    @Override
    public List<Historique> findHistoriqueByLotId(UUID idLot, String type) {
        return Flux.fromIterable(countryWebClients.values())
                .flatMap(webClient -> webClient.get()
                        .uri(uriBuilder -> uriBuilder
                                .path("historique/allHistoriquesForLot/{idLot}")
                                .queryParamIfPresent("type", java.util.Optional.ofNullable(type))
                                .build(idLot))
                        .exchangeToFlux(response -> {
                            if (response.statusCode().is2xxSuccessful()) {
                                return response.bodyToFlux(Historique.class)
                                        .doOnError(err -> log.error("Erreur de parsing récupération des historiques pour un lot", err));
                            }
                            return Flux.empty();
                        })
                        .onErrorResume(ex -> {
                            log.error("Appel getLot KO pour cet adapter", ex);
                            return Mono.empty();
                        })
                )
                .collectList()
                .blockOptional()
                .orElse(List.of());
    }
}
