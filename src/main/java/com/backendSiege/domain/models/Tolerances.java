package com.backendSiege.domain.models;

/** Plage climatique idéale d'un pays : valeurs idéales de température (°C) et d'humidité (%), avec leur tolérance. */
public record Tolerances(int temperatureIdeale, int humiditeIdeale, int toleranceTemperature, int toleranceHumidite) {
}
