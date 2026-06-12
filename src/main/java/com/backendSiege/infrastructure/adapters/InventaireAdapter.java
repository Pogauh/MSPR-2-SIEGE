package com.backendSiege.infrastructure.adapters;

import com.backendSiege.api.dto.MesureLotResponseDto;
import com.backendSiege.api.dto.MesureResponseDto;
import com.backendSiege.domain.port.InventairePort;
import com.backendSiege.api.dto.LotResponseDto;
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
public class InventaireAdapter implements InventairePort {

    private static final Logger log = LoggerFactory.getLogger(InventaireAdapter.class);

    private final Map<String, WebClient> countryWebClients;

    public InventaireAdapter(Map<String, WebClient> countryWebClients) {
        this.countryWebClients = countryWebClients;
    }

    @Override
    public Integer findQuantiteTotale() {
        return Flux.fromIterable(countryWebClients.values())
                .flatMap(webClient -> webClient.get()
                        .uri("lots/getAllQuantity")
                        .exchangeToMono(response -> {
                            log.debug("getAllQuantity status={}", response.statusCode());
                            if (response.statusCode().is2xxSuccessful()) {
                                return response.bodyToMono(Integer.class)
                                        .doOnError(err -> log.error("Erreur de parsing getAllQuantity", err));
                            }
                            if (response.statusCode().value() == 404) {
                                log.warn("getAllQuantity retourne 404, fallback 0");
                                return Mono.just(0); // Pays sans stock
                            }
                            return response.bodyToMono(String.class)
                                    .defaultIfEmpty("")
                                    .flatMap(body -> response.createException()
                                            .doOnNext(ex -> log.error("getAllQuantity erreur HTTP {} body {}", response.statusCode(), body))
                                            .flatMap(Mono::error));
                        })
                        .onErrorResume(ex -> {
                            log.error("Appel getAllQuantity KO, fallback 0", ex);
                            return Mono.just(0);
                        }) // Pays injoignable ou autre erreur
                )
                .reduce(0, Integer::sum)
                .blockOptional()
                .orElse(0);
    }

    @Override
    public LotResponseDto findLotById(UUID id) {
        return Flux.fromIterable(countryWebClients.values())
                .flatMap(webClient -> webClient.get()
                        .uri("lots/getLot/{idLot}", id)
                        .exchangeToMono(response -> {
                            if (response.statusCode().is2xxSuccessful()) {
                                return response.bodyToMono(LotResponseDto.class)
                                        .doOnError(err -> log.error("Erreur de parsing getLot", err));
                            }
                            // Si ce n'est pas trouvé dans ce pays (404 ou autre)
                            return Mono.empty();
                        })
                        .onErrorResume(ex -> {
                            log.error("Appel getLot KO pour cet adapter", ex);
                            return Mono.empty();
                        })
                )
                .next() // Prend le premier pays qui retourne un lot avec succès
                .blockOptional()
                .orElse(null);
    }

    @Override
    public LotResponseDto findOldestLotById(String pays) {
        if (pays != null && !pays.trim().isEmpty()) {
            WebClient targetClient = countryWebClients.get(pays.trim().toUpperCase());
            if (targetClient == null) {
                log.warn("Aucun WebClient configuré pour le pays fourni : {}", pays);
                return null;
            }
            return executeGetOldestLot(targetClient).block();
        }

        return Flux.fromIterable(countryWebClients.values())
                .flatMap(this::executeGetOldestLot)
                // Le reduce compare les lots reçus deux à deux pour ne garder que le plus ancien
                .reduce((lot1, lot2) -> {
                    if (lot1.getDateStockage() == null) return lot2;
                    if (lot2.getDateStockage() == null) return lot1;
                    return lot1.getDateStockage().isBefore(lot2.getDateStockage()) ? lot1 : lot2;
                })
                .blockOptional()
                .orElse(null);
    }

    private Mono<LotResponseDto> executeGetOldestLot(WebClient webClient) {
        return webClient.get()
                .uri("lots/oldestLot")
                .exchangeToMono(response -> {
                    if (response.statusCode().is2xxSuccessful()) {
                        return response.bodyToMono(LotResponseDto.class)
                                .doOnError(err -> log.error("Erreur de parsing getOldestLot", err));
                    }
                    return Mono.empty();
                })
                .onErrorResume(ex -> {
                    log.error("Appel getLot KO pour cet adapter", ex);
                    return Mono.empty();
                });
    }

    @Override
    public List<LotResponseDto> findAllLots() {
        return Flux.fromIterable(countryWebClients.values())
                .flatMap(webClient -> webClient.get()
                        .uri("lots/allLotsFromAllEntrepots")
                        .exchangeToFlux(response -> {
                            if (response.statusCode().is2xxSuccessful()) {
                                return response.bodyToFlux(LotResponseDto.class)
                                        .doOnError(err -> log.error("Erreur de parsing allLotsFromAllEntrepots", err));
                            }
                            return Flux.empty();
                        })
                        .onErrorResume(ex -> {
                            log.error("Appel allLotsFromAllEntrepots KO pour cet adapter", ex);
                            return Flux.empty();
                        })
                )
                .collectList()
                .blockOptional()
                .orElse(List.of());
    }

    @Override
    public List<MesureLotResponseDto> findAllMesuresByLotId(UUID id) {
        return Flux.fromIterable(countryWebClients.values())
                .flatMap(webClient -> webClient.get()
                        .uri("lots/getAllMesuresByLotId/{idLot}", id)
                        .exchangeToFlux(response -> {
                            if (response.statusCode().is2xxSuccessful()) {
                                return response.bodyToFlux(MesureLotResponseDto.class)
                                        .doOnError(err -> log.error("Erreur de parsing getAllMesuresByLotId", err));
                            }
                            return Flux.empty();
                        })
                        .onErrorResume(ex -> {
                            log.error("Appel getMesuresByLotId KO pour  l'adapter findAllMesuresByLotId", ex);
                            return Flux.empty();
                        })
                )
                .collectList()
                .blockOptional()
                .orElse(List.of());
    }
}