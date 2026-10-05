package com.backendSiege.support;

import com.backendSiege.infrastructure.client.PaysClient;
import com.backendSiege.infrastructure.client.PaysClients;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.LinkedHashMap;
import java.util.Map;

/** Construit les clients des pays comme le fait la configuration, mais face à des faux backends. */
public final class PaysTestClients {

    private PaysTestClients() {
    }

    /** @param serveurs les faux backends, par code de pays (BRESIL, COLOMBIE…), dans l'ordre voulu */
    public static PaysClients clients(Map<String, FakePaysServer> serveurs) {
        Map<String, PaysClient> clients = new LinkedHashMap<>();
        serveurs.forEach((code, serveur) -> clients.put(code, client(code, serveur)));
        return new PaysClients(clients);
    }

    public static PaysClient client(String code, FakePaysServer serveur) {
        return new PaysClient(code, WebClient.builder().baseUrl(serveur.baseUrl()).build());
    }

    /** Les trois pays, dans l'ordre BRESIL, COLOMBIE, EQUATEUR. */
    public static Map<String, FakePaysServer> troisServeurs() {
        Map<String, FakePaysServer> serveurs = new LinkedHashMap<>();
        serveurs.put("BRESIL", new FakePaysServer());
        serveurs.put("COLOMBIE", new FakePaysServer());
        serveurs.put("EQUATEUR", new FakePaysServer());
        return serveurs;
    }
}
