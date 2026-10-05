package com.backendSiege.domain.models;

import java.util.Set;
import java.util.UUID;

/**
 * Utilisateur du siège.
 *
 * @param password  mot de passe haché
 * @param countryId pays auquel l'utilisateur est rattaché ; {@code null} pour un accès non limité à un pays
 */
public record User(UUID id, String email, String password, Set<Role> roles, String countryId) {

    /** Un nouvel utilisateur, pas encore enregistré (sans identifiant) et sans pays de rattachement. */
    public static User nouveau(String email, String motDePasseHache, Role role) {
        return new User(null, email, motDePasseHache, Set.of(role), null);
    }
}
