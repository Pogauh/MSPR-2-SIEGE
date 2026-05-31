package com.backendSiege.infrastructure.adapters;

import com.backendSiege.domain.port.MesurePort;
import com.backendSiege.api.dto.MesureResponseDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

@Component
public class MesureAdapter implements MesurePort {

    private static final Logger log = LoggerFactory.getLogger(MesureAdapter.class);

    private final List<WebClient> countryWebClients;

    public MesureAdapter(List<WebClient> countryWebClients) {
        this.countryWebClients = countryWebClients;
    }

    @Override
    public MesureResponseDTO findMesureById(UUID id) {
        return Flux.fromIterable(countryWebClients)
                .flatMap(webClient -> webClient.get()
                        .uri("/mesures/getMesure/{id}", id)
                        .exchangeToMono(response -> {
                            if (response.statusCode().is2xxSuccessful()) {
                                return response.bodyToMono(MesureResponseDTO.class)
                                        .doOnError(err -> log.error("Erreur de parsing getMesure", err));
                            }
                            return Mono.empty();
                        })
                        .onErrorResume(ex -> {
                            log.error("Appel getMesure KO pour cet adapter", ex);
                            return Mono.empty();
                        })
                )
                .next()
                .blockOptional()
                .orElse(null);
    }
}
