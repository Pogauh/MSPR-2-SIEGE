package com.backendSiege.api.dto;

import java.time.LocalDateTime;

public record AlerteDto(String idAlerte, String typeAlerte, String description, LocalDateTime dateEmission, String idZone) {
}
