package com.backendSiege.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AlerteDto {
    private String idAlerte;
    private String typeAlerte;
    private String description;
    private LocalDateTime dateEmission;
    private String idZone;
}