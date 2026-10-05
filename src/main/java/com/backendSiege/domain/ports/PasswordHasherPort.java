package com.backendSiege.domain.ports;

/** Hachage des mots de passe. */
public interface PasswordHasherPort {

    String hash(String motDePasse);

    boolean matches(String motDePasse, String motDePasseHache);
}
