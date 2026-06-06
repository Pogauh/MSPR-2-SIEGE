package com.backendSiege.domain.model;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class Historique {

    private UUID idHistorique;
    private TypeHistoriqueEnum type;
    private LocalDateTime dateEmission;
    private UUID zonePrecedente;
    private UUID zoneActuelle;
    private String detail;
}
