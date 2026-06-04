package com.backendSiege.infrastructure.configuration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.HashMap;
import java.util.Map;

@Configuration
@ConfigurationProperties(prefix = "app.country-adapters")
public class WebClientConfig {

    private Map<String, String> mapping;

    public void setMapping(Map<String, String> mapping) { this.mapping = mapping; }

    @Bean
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder();
    }

    @Bean
    public Map<String, WebClient> countryWebClients(WebClient.Builder builder) {
        Map<String, WebClient> clients = new HashMap<>();
        mapping.forEach((country, url) ->
                clients.put(country.toUpperCase(), builder.clone().baseUrl(url).build())
        );
        return clients;
    }
}