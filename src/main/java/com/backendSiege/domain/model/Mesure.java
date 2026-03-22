package com.backendSiege.domain.model;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class Mesure {

    private final UUID id;
    private final Integer temperature;
    private final Integer humidite;
    private final LocalDateTime dateMesure;
    private final Capteur capteur;
}

