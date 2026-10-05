package com.backendSiege.domain.ports;

import com.backendSiege.domain.models.User;

import java.util.Optional;

/** Persistance des utilisateurs du siège. */
public interface UserPort {

    Optional<User> findByEmail(String email);

    /** Enregistre l'utilisateur et le renvoie tel qu'enregistré (avec son identifiant). */
    User save(User user);
}
