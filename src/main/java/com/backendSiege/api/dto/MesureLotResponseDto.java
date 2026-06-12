package com.backendSiege.api.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@JsonPropertyOrder({
    "temperature",
    "humidite",
    "dateMesure"
})
public class MesureLotResponseDto {
    private Double temperature;
    private Double humidite;
    private LocalDateTime dateMesure;
}
