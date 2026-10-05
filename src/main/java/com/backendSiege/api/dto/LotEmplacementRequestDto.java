package com.backendSiege.api.dto;

import java.util.UUID;

/** Corps de {@code PUT /lot/{idLot}/zones} : la zone de destination. */
public record LotEmplacementRequestDto(UUID idZone) {
}
