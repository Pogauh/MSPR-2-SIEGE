package com.backendSiege.api.dto;

import java.util.UUID;

public class LotStatusResponseDto {
    public UUID idLot;
    public String status;

    public LotStatusResponseDto() {
    }

    public LotStatusResponseDto(UUID idLot, String status) {
        this.idLot = idLot;
        this.status = status;
    }
}
