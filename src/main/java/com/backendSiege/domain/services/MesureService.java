package com.backendSiege.domain.services;

import com.backendSiege.domain.models.CapteurSummary;
import com.backendSiege.domain.models.Mesure;
import com.backendSiege.domain.ports.MesurePort;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Mesures des capteurs d'un pays. Un pays inconnu n'a ni mesure ni capteur. */
public class MesureService {

    private final MesurePort mesurePort;
    private final PaysService paysService;

    public MesureService(MesurePort mesurePort, PaysService paysService) {
        this.mesurePort = mesurePort;
        this.paysService = paysService;
    }

    public Optional<Mesure> getMesureById(String pays, UUID idMesure) {
        return paysService.resoudre(pays).flatMap(code -> mesurePort.findMesure(code, idMesure));
    }

    public List<Mesure> getMesuresByZoneId(String pays, UUID idZone, LocalDateTime debut, LocalDateTime fin) {
        return paysService.resoudre(pays).map(code -> mesurePort.findMesuresDeLaZone(code, idZone, debut, fin)).orElseGet(List::of);
    }

    public Optional<Mesure> getLastMesureByZoneId(String pays, UUID idZone) {
        return paysService.resoudre(pays).flatMap(code -> mesurePort.findDerniereMesureDeLaZone(code, idZone));
    }

    public List<CapteurSummary> getCapteurSummaryByEntrepotId(String pays, UUID idEntrepot) {
        return paysService.resoudre(pays).map(code -> mesurePort.findResumeCapteurs(code, idEntrepot)).orElseGet(List::of);
    }
}
