package com.backendSiege.infrastructure.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** Les clients HTTP des backends des pays configurés, indexés par code normalisé (en majuscules). */
public class PaysClients {

    private static final Logger log = LoggerFactory.getLogger(PaysClients.class);

    private final Map<String, PaysClient> clients;

    public PaysClients(Map<String, PaysClient> clients) {
        this.clients = Map.copyOf(clients);
    }

    /** @throws IllegalArgumentException si aucun pays n'est configuré sous ce code (le domaine ne transmet que des codes valides) */
    public PaysClient get(String code) {
        return Optional.ofNullable(clients.get(code))
                .orElseThrow(() -> new IllegalArgumentException("Aucun client configuré pour le pays : " + code));
    }

    /** Les clients des pays demandés, dans l'ordre demandé ; un code inconnu est ignoré. */
    public List<PaysClient> clients(Collection<String> codes) {
        return codes.stream()
                .map(code -> {
                    PaysClient client = clients.get(code);
                    if (client == null) {
                        log.warn("Aucun client configuré pour le pays : {}", code);
                    }
                    return Optional.ofNullable(client);
                })
                .flatMap(Optional::stream)
                .toList();
    }
}
