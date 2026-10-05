package com.backendSiege.infrastructure.client.adapter;

import com.backendSiege.domain.models.ReportingGlobal;
import com.backendSiege.domain.models.ReportingQuality;
import com.backendSiege.domain.models.ReportingStock;
import com.backendSiege.domain.ports.ReportingPort;
import com.backendSiege.infrastructure.client.PaysClients;
import com.backendSiege.infrastructure.client.dto.ReportingGlobalPaysDto;
import com.backendSiege.infrastructure.client.dto.ReportingQualityPaysDto;
import com.backendSiege.infrastructure.client.dto.ReportingStockPaysDto;
import com.backendSiege.infrastructure.client.mapper.PaysMapper;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/** Rapports lus dans les backends des pays par leur API REST ({@code /api/reporting/...}) ; un pays en erreur donne un rapport vide. */
@Component
public class ReportingAdapter implements ReportingPort {

    private final PaysClients paysClients;
    private final PaysMapper mapper;

    public ReportingAdapter(PaysClients paysClients, PaysMapper mapper) {
        this.paysClients = paysClients;
        this.mapper = mapper;
    }

    @Override
    public List<ReportingGlobal> findReportingGlobal(Collection<String> codesPays) {
        return Flux.fromIterable(paysClients.clients(codesPays))
                .flatMapSequential(client -> client
                        .lire(uri -> uri.path("reporting/global").build(), ReportingGlobalPaysDto.class, "reporting/global")
                        .map(mapper::toReportingGlobal)
                        .defaultIfEmpty(ReportingGlobal.vide()))
                .collectList()
                .blockOptional()
                .orElse(List.of());
    }

    @Override
    public List<ReportingQuality> findReportingQuality(Collection<String> codesPays, LocalDateTime debut, LocalDateTime fin) {
        return Flux.fromIterable(paysClients.clients(codesPays))
                .flatMapSequential(client -> client
                        .lire(uri -> uri.path("reporting/quality")
                                .queryParamIfPresent("startDate", Optional.ofNullable(debut).map(LocalDateTime::toString))
                                .queryParamIfPresent("endDate", Optional.ofNullable(fin).map(LocalDateTime::toString))
                                .build(), ReportingQualityPaysDto.class, "reporting/quality")
                        .map(mapper::toReportingQuality)
                        .defaultIfEmpty(ReportingQuality.vide()))
                .collectList()
                .blockOptional()
                .orElse(List.of());
    }

    @Override
    public ReportingStock findReportingStock(String codePays, LocalDateTime debut, LocalDateTime fin) {
        return paysClients.get(codePays)
                .lire(uri -> uri.path("reporting/stock")
                        .queryParamIfPresent("startDate", Optional.ofNullable(debut).map(LocalDateTime::toString))
                        .queryParamIfPresent("endDate", Optional.ofNullable(fin).map(LocalDateTime::toString))
                        .build(), ReportingStockPaysDto.class, "reporting/stock")
                .map(mapper::toReportingStock)
                .blockOptional()
                .orElseGet(ReportingStock::vide);
    }
}
