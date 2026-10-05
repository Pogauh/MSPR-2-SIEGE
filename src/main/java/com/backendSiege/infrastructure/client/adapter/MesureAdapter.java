package com.backendSiege.infrastructure.client.adapter;

import com.backendSiege.domain.models.CapteurSummary;
import com.backendSiege.domain.models.Mesure;
import com.backendSiege.domain.ports.MesurePort;
import com.backendSiege.infrastructure.client.PaysClients;
import com.backendSiege.infrastructure.client.dto.CapteurSummaryPaysDto;
import com.backendSiege.infrastructure.client.dto.MesurePaysDto;
import com.backendSiege.infrastructure.client.mapper.PaysMapper;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Mesures des capteurs lues dans les backends des pays par leur API REST. */
@Component
public class MesureAdapter implements MesurePort {

    private final PaysClients paysClients;
    private final PaysMapper mapper;

    public MesureAdapter(PaysClients paysClients, PaysMapper mapper) {
        this.paysClients = paysClients;
        this.mapper = mapper;
    }

    @Override
    public Optional<Mesure> findMesure(String codePays, UUID idMesure) {
        return paysClients.get(codePays)
                .lire(uri -> uri.path("mesures/getMesure/{idMesure}").build(idMesure), MesurePaysDto.class, "getMesure")
                .map(mapper::toMesure)
                .blockOptional();
    }

    @Override
    public List<Mesure> findMesuresDeLaZone(String codePays, UUID idZone, LocalDateTime debut, LocalDateTime fin) {
        return paysClients.get(codePays)
                .lister(uri -> uri.path("zone/readings/{idZone}")
                        .queryParamIfPresent("startDate", Optional.ofNullable(debut).map(LocalDateTime::toString))
                        .queryParamIfPresent("endDate", Optional.ofNullable(fin).map(LocalDateTime::toString))
                        .build(idZone), MesurePaysDto.class, "readings")
                .map(mapper::toMesure)
                .collectList()
                .blockOptional()
                .orElse(List.of());
    }

    @Override
    public Optional<Mesure> findDerniereMesureDeLaZone(String codePays, UUID idZone) {
        return paysClients.get(codePays)
                .lire(uri -> uri.path("zone/{idZone}/readings/latest").build(idZone), MesurePaysDto.class, "readings/latest")
                .map(mapper::toMesure)
                .blockOptional();
    }

    @Override
    public List<CapteurSummary> findResumeCapteurs(String codePays, UUID idEntrepot) {
        return paysClients.get(codePays)
                .lister(uri -> uri.path("entrepots/{idEntrepot}/capteurSummary").build(idEntrepot), CapteurSummaryPaysDto.class,
                        "capteurSummary")
                .map(mapper::toCapteurSummary)
                .collectList()
                .blockOptional()
                .orElse(List.of());
    }
}
