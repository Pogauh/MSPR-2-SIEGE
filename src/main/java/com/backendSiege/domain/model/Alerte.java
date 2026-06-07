package com.backendSiege.domain.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class Alerte {
    private String idAlerte;
    private String typeAlerte;
    private String description;
    private LocalDateTime dateEmission;
    private String idZone;
}
