package com.backendSiege.infrastructure.client;

import com.backendSiege.domain.exceptions.PaysIndisponibleException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

/**
 * Client HTTP de l'API REST du backend d'un pays. Les chemins sont relatifs à l'adresse configurée du pays
 * (qui se termine par {@code /api/}).
 * <p>
 * Deux familles d'appels :
 * <ul>
 *   <li><b>tolérants</b> ({@link #lire}, {@link #lister}, {@link #envoyer}) : une réponse en erreur, une réponse illisible ou
 *   un pays injoignable donnent un résultat vide, l'erreur est seulement journalisée ;</li>
 *   <li><b>stricts</b> ({@link #listerOuEchouer}, {@link #envoyerOuEchouer}) : toute erreur devient une
 *   {@link PaysIndisponibleException}.</li>
 * </ul>
 */
public class PaysClient {

    private static final Logger log = LoggerFactory.getLogger(PaysClient.class);

    private final String code;
    private final WebClient webClient;

    public PaysClient(String code, WebClient webClient) {
        this.code = code;
        this.webClient = webClient;
    }

    public String code() {
        return code;
    }

    /** GET tolérant d'une ressource : son contenu si le pays répond en 2xx, rien sinon. */
    public <T> Mono<T> lire(Function<UriBuilder, URI> uri, Class<T> type, String operation) {
        return webClient.get().uri(uri)
                .exchangeToMono(reponse -> reponse.statusCode().is2xxSuccessful()
                        ? reponse.bodyToMono(type).doOnError(erreur -> log.error("Erreur de parsing {} ({})", operation, code, erreur))
                        : Mono.<T>empty())
                .onErrorResume(erreur -> {
                    log.error("Appel {} KO pour le pays {}", operation, code, erreur);
                    return Mono.empty();
                });
    }

    /** GET tolérant d'une liste : ses éléments si le pays répond en 2xx, aucun sinon. */
    public <T> Flux<T> lister(Function<UriBuilder, URI> uri, Class<T> type, String operation) {
        return webClient.get().uri(uri)
                .exchangeToFlux(reponse -> reponse.statusCode().is2xxSuccessful()
                        ? reponse.bodyToFlux(type).doOnError(erreur -> log.error("Erreur de parsing {} ({})", operation, code, erreur))
                        : Flux.<T>empty())
                .onErrorResume(erreur -> {
                    log.error("Appel {} KO pour le pays {}", operation, code, erreur);
                    return Flux.empty();
                });
    }

    /** Envoi tolérant ({@code POST}, {@code PUT}…) : la réponse si le pays répond en 2xx, rien sinon. */
    public <T> Mono<T> envoyer(HttpMethod methode, Function<UriBuilder, URI> uri, Object corps, Class<T> type, String operation) {
        return requete(methode, uri, corps)
                .exchangeToMono(reponse -> reponse.statusCode().is2xxSuccessful()
                        ? reponse.bodyToMono(type).doOnError(erreur -> log.error("Erreur de parsing {} ({})", operation, code, erreur))
                        : Mono.<T>empty())
                .onErrorResume(erreur -> {
                    log.error("Appel {} KO pour le pays {}", operation, code, erreur);
                    return Mono.empty();
                });
    }

    /** GET strict d'une liste. @throws PaysIndisponibleException si le pays répond en erreur ou est injoignable */
    public <T> List<T> listerOuEchouer(Function<UriBuilder, URI> uri, Class<T> type) {
        return bloquer(webClient.get().uri(uri).retrieve().bodyToFlux(type).collectList())
                .orElseGet(List::of);
    }

    /** Envoi strict. @throws PaysIndisponibleException si le pays répond en erreur ou est injoignable */
    public <T> Optional<T> envoyerOuEchouer(HttpMethod methode, Function<UriBuilder, URI> uri, Object corps, Class<T> type) {
        return bloquer(requete(methode, uri, corps).retrieve().bodyToMono(type));
    }

    private WebClient.RequestHeadersSpec<?> requete(HttpMethod methode, Function<UriBuilder, URI> uri, Object corps) {
        WebClient.RequestBodySpec requete = webClient.method(methode).uri(uri);
        return corps == null ? requete : requete.bodyValue(corps);
    }

    private <T> Optional<T> bloquer(Mono<T> appel) {
        try {
            return appel.blockOptional();
        } catch (RuntimeException erreur) {
            log.error("Appel strict KO pour le pays {}", code, erreur);
            throw new PaysIndisponibleException(code, erreur);
        }
    }
}
