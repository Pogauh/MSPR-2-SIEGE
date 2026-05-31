package com.backendSiege.api.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@Data
@JsonPropertyOrder({
    "idHistorique",
    "type",
    "dateEmission",
    "zonePrecedente",
    "zoneActuelle",
    "detail"
})
public class HistoriqueDto {
    private UUID idHistorique;
    private String type;
    private LocalDateTime dateEmission;
    private UUID zonePrecedente;
    private UUID zoneActuelle;
    private String detail;
}
