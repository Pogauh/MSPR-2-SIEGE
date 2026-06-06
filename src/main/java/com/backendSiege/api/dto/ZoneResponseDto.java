package com.backendSiege.api.dto;


import java.util.UUID;

public class ZoneResponseDto {
    public UUID idZone;
    public String nomZone;
    public UUID idEntrepot;
    public UUID idCapteur;
    public Long lotCount;

    public ZoneResponseDto(UUID idZone, String nomZone, UUID idEntrepot, UUID idCapteur, Long lotCount) {
        this.idZone = idZone;
        this.nomZone = nomZone;
        this.idEntrepot = idEntrepot;
        this.idCapteur = idCapteur;
        this.lotCount = lotCount;
    }
}
