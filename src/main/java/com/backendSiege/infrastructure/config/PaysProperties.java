package com.backendSiege.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

/**
 * Pays configurés ({@code app.country-adapters.mapping.<PAYS>}) : l'adresse de l'API REST de leur backend
 * et leur plage climatique idéale.
 *
 * @param mapping les pays, indexés par nom (BRESIL, COLOMBIE…), dans l'ordre de la configuration
 */
@ConfigurationProperties(prefix = "app.country-adapters")
public record PaysProperties(Map<String, Pays> mapping) {

    public PaysProperties {
        mapping = mapping == null ? Map.of() : mapping;
    }

    public record Pays(String url, Tolerances tolerences) {
    }

    public record Tolerances(int temperatureIdeale, int humiditeIdeale, int toleranceTemperature, int toleranceHumidite) {
    }
}
