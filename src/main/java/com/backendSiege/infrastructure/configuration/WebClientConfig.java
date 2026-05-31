package com.backendSiege.infrastructure.configuration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@Configuration
public class WebClientConfig {

    private final List<String> countryApiBaseUrls;

    public WebClientConfig(@Value("${country.api.base-urls}") List<String> countryApiBaseUrls) {
        this.countryApiBaseUrls = countryApiBaseUrls;
    }

    @Bean
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder();
    }

    @Bean
    public List<WebClient> countryWebClients(WebClient.Builder builder) {
        return countryApiBaseUrls.stream()
                .map(url -> builder.clone().baseUrl(url).build())
                .toList();
    }
}