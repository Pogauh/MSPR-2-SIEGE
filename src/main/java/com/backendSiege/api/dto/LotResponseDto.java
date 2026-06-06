package com.backendSiege.api.dto;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@Data
@JsonPropertyOrder({
    "idLot",
    "status",
    "quantite",
    "caracteristique",
    "dateSortie",
    "dateStockage",
    "dateProduction",
    "emplacement",
    "idExploitation",
    "idEntrepot",
    "historique"
})
public class LotResponseDto {
    public UUID idLot;
    public String status;
    public Integer quantite;
    public Integer caracteristique;
    public LocalDateTime dateSortie;
    public LocalDateTime dateStockage;
    public LocalDate dateProduction;
    public String emplacement;
    public UUID idExploitation;
    public UUID idEntrepot;
    public List<HistoriqueResponseDto> historique;
}
