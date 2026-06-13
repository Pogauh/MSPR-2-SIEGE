package com.backendSiege.infrastructure.adapters;

import com.backendSiege.api.dto.ReportingGlobalDto;
import com.backendSiege.api.dto.ReportingQualityDto;
import com.backendSiege.api.dto.ReportingStockDto;
import com.backendSiege.domain.port.ReportingPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Map;

@Component
public class ReportingAdapter implements ReportingPort {

    private static final Logger log = LoggerFactory.getLogger(ReportingAdapter.class);

    private final Map<String, WebClient> countryWebClients;

    public ReportingAdapter(Map<String, WebClient> countryWebClients) {
        this.countryWebClients = countryWebClients;
    }


    @Override
    public ReportingGlobalDto findReportingGlobal() {
        return Flux.fromIterable(countryWebClients.values())
                .flatMap(webClient -> webClient.get()
                        .uri("reporting/global")
                        .exchangeToMono(response -> {
                            log.debug("findReportingGlobal status={}", response.statusCode());

                            if (response.statusCode().is2xxSuccessful()) {
                                return response.bodyToMono(ReportingGlobalDto.class)
                                        .doOnError(err -> log.error("Erreur de parsing findReportingGlobal", err));
                            }

                            if (response.statusCode().value() == 404) {
                                log.warn("Reporting non trouvé (404), fallback objet vide");
                                return Mono.just(new ReportingGlobalDto());
                            }

                            // Gestion des autres erreurs HTTP (5xx, 403, etc.)
                            return response.createException().flatMap(Mono::error);
                        })
                        .onErrorResume(ex -> {
                            log.error("Appel pays KO, fallback objet vide", ex);
                            return Mono.just(new ReportingGlobalDto());
                        })
                )
                .reduce(new ReportingGlobalDto(), (accumulateur, dtoPays) -> {
                    int ancienTotalLots = accumulateur.getTotalLots();
                    int nouveauxLotsPays = dtoPays.getTotalLots();
                    int totalLotsFutur = ancienTotalLots + nouveauxLotsPays;

                    // 1. Accumulation des compteurs simples
                    accumulateur.setTotalLots(totalLotsFutur);
                    accumulateur.setCountMouvementLast30Days(
                            accumulateur.getCountMouvementLast30Days() + dtoPays.getCountMouvementLast30Days()
                    );

                    // 2. Moyenne d'âge pondérée globale
                    if (totalLotsFutur > 0) {
                        double sommeAgesExistants = accumulateur.getAvgAgeDays() * ancienTotalLots;
                        double sommeAgesNouveauPays = dtoPays.getAvgAgeDays() * nouveauxLotsPays;
                        accumulateur.setAvgAgeDays((sommeAgesExistants + sommeAgesNouveauPays) / totalLotsFutur);
                    }

                    // 3. Taux de conformité pondéré global
                    if (totalLotsFutur > 0) {
                        double conformiteExistante = accumulateur.getComplianceRate() * ancienTotalLots;
                        double conformiteNouveauPays = dtoPays.getComplianceRate() * nouveauxLotsPays;
                        accumulateur.setComplianceRate((conformiteExistante + conformiteNouveauPays) / totalLotsFutur);
                    }

                    return accumulateur;
                })
                .block();
    }

    public ReportingQualityDto findReportingQuality(String pays, LocalDateTime startDate, LocalDateTime endDate) {

        // 1. Traitement si un pays spécifique est fourni
        if (pays != null && !pays.trim().isEmpty()) {
            WebClient targetClient = countryWebClients.get(pays.trim().toUpperCase());
            if (targetClient == null) {
                log.warn("Aucun WebClient configuré pour le pays fourni : {}", pays);
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Le pays fourni '" + pays + "' n'est pas supporté ou configuré."
                );
            }
            // On exécute l'appel uniquement sur le client du pays cible
            return executeQualityReporting(targetClient, startDate, endDate).block();
        }

        // 2. Traitement global si aucun pays n'est fourni (on boucle sur tous les pays)
        return Flux.fromIterable(countryWebClients.values())
                .flatMap(webClient -> executeQualityReporting(webClient, startDate, endDate))
                .reduce(new ReportingQualityDto(), (accumulateur, dtoPays) -> {
                    // Accumulation des compteurs simples
                    accumulateur.setAlertCount(
                            accumulateur.getAlertCount() + dtoPays.getAlertCount()
                    );
                    accumulateur.setNonComplianceCount(
                            accumulateur.getNonComplianceCount() + dtoPays.getNonComplianceCount()
                    );

                    // Moyenne du taux de conformité
                    if (dtoPays.getComplianceRate() > 0) {
                        if (accumulateur.getComplianceRate() == 0) {
                            accumulateur.setComplianceRate(dtoPays.getComplianceRate());
                        } else {
                            accumulateur.setComplianceRate((accumulateur.getComplianceRate() + dtoPays.getComplianceRate()) / 2.0);
                        }
                    }
                    return accumulateur;
                })
                .block();
    }


    public ReportingStockDto findReportingStock(String pays, LocalDateTime startDate, LocalDateTime endDate) {

        // 1. Validation de la présence du pays
        if (pays == null || pays.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Le paramètre 'pays' est obligatoire.");
        }

        // 2. Récupération du client correspondant
        WebClient targetClient = countryWebClients.get(pays.trim().toUpperCase());

        if (targetClient == null) {
            log.warn("Aucun WebClient configuré pour le pays : {}", pays);
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Le pays '" + pays + "' n'est pas supporté ou configuré."
            );
        }

        // 3. Appel direct et retour du résultat
        return executeStockReporting(targetClient, startDate, endDate).block();
    }

    private Mono<ReportingQualityDto> executeQualityReporting(WebClient webClient, LocalDateTime startDate, LocalDateTime endDate) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("reporting/quality")
                        .queryParam("startDate", startDate)
                        .queryParam("endDate", endDate)
                        .build())
                .exchangeToMono(response -> {
                    log.debug("findReportingQuality status={}", response.statusCode());

                    if (response.statusCode().is2xxSuccessful()) {
                        return response.bodyToMono(ReportingQualityDto.class)
                                .doOnError(err -> log.error("Erreur de parsing findReportingQuality", err));
                    }

                    if (response.statusCode().value() == 404) {
                        log.warn("Reporting non trouvé (404), fallback objet vide");
                        return Mono.just(new ReportingQualityDto());
                    }

                    return response.createException().flatMap(Mono::error);
                })
                .onErrorResume(ex -> {
                    log.error("Appel pays KO, fallback objet vide", ex);
                    return Mono.just(new ReportingQualityDto());
                });
    }

    private Mono<ReportingStockDto> executeStockReporting(WebClient webClient, LocalDateTime startDate, LocalDateTime endDate) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("reporting/stock")
                        .queryParam("startDate", startDate)
                        .queryParam("endDate", endDate)
                        .build())
                .exchangeToMono(response -> {
                    log.debug("findReportingStock status={}", response.statusCode());

                    if (response.statusCode().is2xxSuccessful()) {
                        return response.bodyToMono(ReportingStockDto.class)
                                .doOnError(err -> log.error("Erreur de parsing findReportingStock", err));
                    }

                    if (response.statusCode().value() == 404) {
                        log.warn("Reporting non trouvé (404), fallback objet vide");
                        return Mono.just(new ReportingStockDto());
                    }

                    return response.createException().flatMap(Mono::error);
                })
                .onErrorResume(ex -> {
                    log.error("Appel pays KO, fallback objet vide", ex);
                    return Mono.just(new ReportingStockDto());
                });
    }
}
