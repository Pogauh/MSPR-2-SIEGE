package com.backendSiege.domain.model;

import java.util.UUID;

public class ZoneResponse {
    public UUID idZone;
    public String nomZone;
    public UUID idEntrepot;
    public UUID idCapteur;
    public Long lotCount;

    public ZoneResponse(UUID idZone, String nomZone, UUID idEntrepot, UUID idCapteur, Long lotCount) {
        this.idZone = idZone;
        this.nomZone = nomZone;
        this.idEntrepot = idEntrepot;
        this.idCapteur = idCapteur;
        this.lotCount = lotCount;
    }
}
