package com.backendSiege.api.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@Data
@JsonPropertyOrder({
    "idMesure",
    "temperature",
    "humidite",
    "dateMesure",
    "idCapteur"
})
public class MesureResponseDTO {
    private UUID idMesure;
    private Double temperature;
    private Double humidite;
    private LocalDateTime dateMesure;
    private UUID idCapteur;
}
