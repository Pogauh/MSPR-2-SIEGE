package com.backendSiege.infrastructure.config;

import com.backendSiege.infrastructure.client.PaysClient;
import com.backendSiege.infrastructure.client.PaysClients;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/** Crée un client HTTP par pays configuré. */
@Configuration
@EnableConfigurationProperties(PaysProperties.class)
public class PaysClientsConfig {

    @Bean
    public PaysClients paysClients(PaysProperties properties) {
        Map<String, PaysClient> clients = new LinkedHashMap<>();
        properties.mapping().forEach((nom, pays) -> {
            String code = nom.trim().toUpperCase(Locale.ROOT);
            clients.put(code, new PaysClient(code, WebClient.builder().baseUrl(pays.url()).build()));
        });
        return new PaysClients(clients);
    }
}
