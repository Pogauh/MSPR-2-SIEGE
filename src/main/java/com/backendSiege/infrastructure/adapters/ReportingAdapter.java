package com.backendSiege.infrastructure.adapters;

import com.backendSiege.api.dto.ReportingGlobalDto;
import com.backendSiege.api.dto.ReportingQualityDto;
import com.backendSiege.domain.port.ReportingPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
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

    @Override
    public ReportingQualityDto findReportingQuality(LocalDateTime startDate, LocalDateTime endDate) {
        return Flux.fromIterable(countryWebClients.values())
                .flatMap(webClient -> webClient.get()
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

                            // Gestion des autres erreurs HTTP (5xx, 403, etc.)
                            return response.createException().flatMap(Mono::error);
                        })
                        .onErrorResume(ex -> {
                            log.error("Appel pays KO, fallback objet vide", ex);
                            return Mono.just(new ReportingQualityDto());
                        })
                )
                .reduce(new ReportingQualityDto(), (accumulateur, dtoPays) -> {
                    // 1. Accumulation des compteurs simples
                    accumulateur.setAlertCount(
                            accumulateur.getAlertCount() + dtoPays.getAlertCount()
                    );
                    accumulateur.setNonComplianceCount(
                            accumulateur.getNonComplianceCount() + dtoPays.getNonComplianceCount()
                    );

                    // 2. Moyenne du taux de conformité par rapport aux pays qui répondent
                    if (dtoPays.getComplianceRate() > 0) {
                        // Si c'est le premier pays qui a de la donnée, on initialise le taux
                        if (accumulateur.getComplianceRate() == 0) {
                            accumulateur.setComplianceRate(dtoPays.getComplianceRate());
                        } else {
                            // Sinon, on fait la moyenne glissante entre l'existant cumulé et le nouveau pays
                            accumulateur.setComplianceRate((accumulateur.getComplianceRate() + dtoPays.getComplianceRate()) / 2.0);
                        }
                    }

                    return accumulateur;
                })
                .block();
    }
}
