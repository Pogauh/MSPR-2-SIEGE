package com.backendSiege.infrastructure.client.adapter;

import com.backendSiege.domain.models.Historique;
import com.backendSiege.domain.models.TypeHistoriqueEnum;
import com.backendSiege.domain.ports.HistoriquePort;
import com.backendSiege.infrastructure.client.PaysClients;
import com.backendSiege.infrastructure.client.dto.HistoriquePaysDto;
import com.backendSiege.infrastructure.client.mapper.PaysMapper;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Historique des lots lu dans les backends des pays par leur API REST. */
@Component
public class HistoriqueAdapter implements HistoriquePort {

    private final PaysClients paysClients;
    private final PaysMapper mapper;

    public HistoriqueAdapter(PaysClients paysClients, PaysMapper mapper) {
        this.paysClients = paysClients;
        this.mapper = mapper;
    }

    @Override
    public List<Historique> findHistorique(Collection<String> codesPays, UUID idLot, TypeHistoriqueEnum type) {
        return Flux.fromIterable(paysClients.clients(codesPays))
                .flatMapSequential(client -> client
                        .lister(uri -> uri.path("historique/allHistoriquesForLot/{idLot}")
                                .queryParamIfPresent("type", Optional.ofNullable(type).map(Enum::name))
                                .build(idLot), HistoriquePaysDto.class, "allHistoriquesForLot"))
                .map(mapper::toHistorique)
                .collectList()
                .blockOptional()
                .orElse(List.of());
    }
}
