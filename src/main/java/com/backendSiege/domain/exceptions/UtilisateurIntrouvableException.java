package com.backendSiege.domain.exceptions;

/** Aucun utilisateur n'est inscrit avec cette adresse e-mail. */
public class UtilisateurIntrouvableException extends RuntimeException {

    public UtilisateurIntrouvableException() {
        super("Utilisateur introuvable");
    }
}
