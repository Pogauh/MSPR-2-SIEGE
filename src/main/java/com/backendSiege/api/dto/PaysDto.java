package com.backendSiege.api.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Un pays de {@code GET /countries} : l'adresse de son backend et sa plage climatique idéale
 * (la clé {@code tolerences} est historique, elle est conservée).
 */
@JsonPropertyOrder({"tolerences", "url"})
public record PaysDto(String url, TolerancesDto tolerences) {

    @JsonPropertyOrder({"humiditeIdeale", "temperatureIdeale", "toleranceHumidite", "toleranceTemperature"})
    public record TolerancesDto(int temperatureIdeale, int humiditeIdeale, int toleranceTemperature, int toleranceHumidite) {
    }
}
