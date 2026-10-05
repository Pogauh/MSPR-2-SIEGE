package com.backendSiege.support;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;

/**
 * Faux backend d'un pays (serveur HTTP du JDK) : répond avec des réponses préparées et mémorise les requêtes reçues.
 * Une route non préparée répond {@code 404} sans corps, comme le ferait un backend pays pour une ressource inconnue.
 * Le serveur peut être coupé puis relancé sur le même port pour simuler un pays injoignable.
 */
public final class FakePaysServer {

    /** Requête reçue par le faux backend. */
    public record RequestRecue(String methode, String chemin, String query, String corps) {
    }

    private record Reponse(int status, String corps) {
    }

    private final int port;
    private final Map<String, Reponse> routes = new ConcurrentHashMap<>();
    private final List<RequestRecue> requetes = new CopyOnWriteArrayList<>();
    private HttpServer server;

    public FakePaysServer() {
        this.port = portLibre();
        demarrer();
    }

    /** URL de base telle que configurée pour un pays dans le siège (se termine par {@code /api/}). */
    public String baseUrl() {
        return "http://localhost:" + port + "/api/";
    }

    public synchronized void demarrer() {
        if (server != null) {
            return;
        }
        try {
            server = HttpServer.create(new InetSocketAddress("localhost", port), 0);
        } catch (IOException e) {
            throw new IllegalStateException("Impossible de démarrer le faux pays sur le port " + port, e);
        }
        server.setExecutor(Executors.newCachedThreadPool(runnable -> {
            Thread thread = new Thread(runnable, "fake-pays-" + port);
            thread.setDaemon(true);
            return thread;
        }));
        server.createContext("/", this::traiter);
        server.start();
    }

    /** Le pays devient injoignable (connexion refusée). */
    public synchronized void couper() {
        if (server != null) {
            server.stop(0);
            server = null;
        }
    }

    /** Prépare la réponse à {@code methode chemin} (le chemin est celui reçu par le pays, par exemple {@code /api/lots/oldestLot}). */
    public void repond(String methode, String chemin, int status, String corps) {
        routes.put(cle(methode, chemin), new Reponse(status, corps));
    }

    public void repondJson(String methode, String chemin, String corps) {
        repond(methode, chemin, 200, corps);
    }

    public void get(String chemin, String corps) {
        repond("GET", chemin, 200, corps);
    }

    /** Efface les réponses préparées et les requêtes mémorisées, et rallume le pays s'il avait été coupé. */
    public void reinitialiser() {
        routes.clear();
        requetes.clear();
        demarrer();
    }

    public List<RequestRecue> requetes() {
        return List.copyOf(requetes);
    }

    /** Requêtes reçues pour un chemin donné, sans distinction de méthode. */
    public List<RequestRecue> requetes(String chemin) {
        return requetes.stream().filter(r -> r.chemin().equals(chemin)).toList();
    }

    private void traiter(HttpExchange exchange) throws IOException {
        String corpsRecu = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        String chemin = exchange.getRequestURI().getRawPath();
        requetes.add(new RequestRecue(exchange.getRequestMethod(), chemin, exchange.getRequestURI().getRawQuery(), corpsRecu));

        Reponse reponse = routes.getOrDefault(cle(exchange.getRequestMethod(), chemin), new Reponse(404, null));
        if (reponse.corps() == null) {
            exchange.sendResponseHeaders(reponse.status(), -1);
        } else {
            byte[] octets = reponse.corps().getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(reponse.status(), octets.length);
            try (OutputStream sortie = exchange.getResponseBody()) {
                sortie.write(octets);
            }
        }
        exchange.close();
    }

    private static String cle(String methode, String chemin) {
        return methode + " " + chemin;
    }

    private static int portLibre() {
        try (ServerSocket socket = new ServerSocket(0)) {
            return socket.getLocalPort();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
}
