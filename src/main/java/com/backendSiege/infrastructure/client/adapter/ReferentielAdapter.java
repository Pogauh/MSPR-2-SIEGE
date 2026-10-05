package com.backendSiege.infrastructure.client.adapter;

import com.backendSiege.domain.models.Entrepot;
import com.backendSiege.domain.models.Exploitation;
import com.backendSiege.domain.models.Zone;
import com.backendSiege.domain.ports.ReferentielPort;
import com.backendSiege.infrastructure.client.PaysClients;
import com.backendSiege.infrastructure.client.dto.EntrepotPaysDto;
import com.backendSiege.infrastructure.client.dto.ExploitationPaysDto;
import com.backendSiege.infrastructure.client.dto.ZonePaysDto;
import com.backendSiege.infrastructure.client.mapper.PaysMapper;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/** Référentiel (exploitations, entrepôts, zones) lu dans les backends des pays par leur API REST. */
@Component
public class ReferentielAdapter implements ReferentielPort {

    private final PaysClients paysClients;
    private final PaysMapper mapper;

    public ReferentielAdapter(PaysClients paysClients, PaysMapper mapper) {
        this.paysClients = paysClients;
        this.mapper = mapper;
    }

    @Override
    public List<Exploitation> findExploitations(String codePays) {
        return paysClients.get(codePays)
                .listerOuEchouer(uri -> uri.path("exploitation/allExploitations").build(), ExploitationPaysDto.class)
                .stream().map(mapper::toExploitation).toList();
    }

    @Override
    public List<Entrepot> findEntrepots(String codePays) {
        return paysClients.get(codePays)
                .listerOuEchouer(uri -> uri.path("entrepots").build(), EntrepotPaysDto.class)
                .stream().map(mapper::toEntrepot).toList();
    }

    @Override
    public List<Zone> findZones(String codePays, UUID idEntrepot) {
        return paysClients.get(codePays)
                .listerOuEchouer(uri -> uri.path("entrepots/{idEntrepot}/zones").build(idEntrepot), ZonePaysDto.class)
                .stream().map(mapper::toZone).toList();
    }
}
