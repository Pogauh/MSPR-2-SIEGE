package com.backendSiege.infrastructure.configuration;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.HashMap;
import java.util.Map;

@Configuration
@ConfigurationProperties(prefix = "app.country-adapters")
@Data
public class WebClientConfig {

    private Map<String, CountrySettings> mapping;

    @Data
    public static class CountrySettings {
        private String url;
        private Tolerances tolerences;
    }

    @Data
    public static class Tolerances {
        private int temperatureIdeale;
        private int humiditeIdeale;
        private int toleranceTemperature;
        private int toleranceHumidite;
    }

    @Bean
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder();
    }

    @Bean
    public Map<String, WebClient> countryWebClients(WebClient.Builder builder) {
        Map<String, WebClient> clients = new HashMap<>();
        mapping.forEach((country, settings) ->
                clients.put(country.toUpperCase(), builder.clone().baseUrl(settings.getUrl()).build())
        );
        return clients;
    }
}