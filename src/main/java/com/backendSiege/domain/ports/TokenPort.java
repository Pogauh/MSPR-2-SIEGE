package com.backendSiege.domain.ports;

import com.backendSiege.domain.models.User;

/** Jetons d'authentification des utilisateurs. */
public interface TokenPort {

    String generate(User user);

    /**
     * @return l'e-mail de l'utilisateur à qui le jeton a été délivré
     * @throws RuntimeException si le jeton est illisible, falsifié ou expiré
     */
    String extractEmail(String token);

    /** Le jeton a été délivré à cet utilisateur et n'est pas expiré. */
    boolean isValid(String token, User user);
}
