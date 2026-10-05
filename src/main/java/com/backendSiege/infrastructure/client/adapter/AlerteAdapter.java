package com.backendSiege.infrastructure.client.adapter;

import com.backendSiege.domain.models.Alerte;
import com.backendSiege.domain.ports.AlertePort;
import com.backendSiege.infrastructure.client.PaysClients;
import com.backendSiege.infrastructure.client.dto.AlertePaysDto;
import com.backendSiege.infrastructure.client.mapper.PaysMapper;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Alertes lues dans les backends des pays par leur API REST ({@code /api/alertes}). */
@Component
public class AlerteAdapter implements AlertePort {

    private final PaysClients paysClients;
    private final PaysMapper mapper;

    public AlerteAdapter(PaysClients paysClients, PaysMapper mapper) {
        this.paysClients = paysClients;
        this.mapper = mapper;
    }

    @Override
    public List<Alerte> findAlertes(Collection<String> codesPays, UUID idEntrepot, LocalDateTime debut, LocalDateTime fin) {
        return Flux.fromIterable(paysClients.clients(codesPays))
                .flatMapSequential(client -> client
                        .lister(uri -> uri.path("alertes")
                                .queryParam("idEntrepot", idEntrepot.toString())
                                .queryParamIfPresent("startDate", Optional.ofNullable(debut).map(LocalDateTime::toString))
                                .queryParamIfPresent("endDate", Optional.ofNullable(fin).map(LocalDateTime::toString))
                                .build(), AlertePaysDto.class, "alertes"))
                .map(mapper::toAlerte)
                .collectList()
                .blockOptional()
                .orElse(List.of());
    }
}
