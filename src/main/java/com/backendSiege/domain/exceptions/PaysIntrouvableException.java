package com.backendSiege.domain.exceptions;

/** Le pays dont on demande les données n'est configuré dans aucun backend du siège : la ressource n'existe pas. */
public class PaysIntrouvableException extends RuntimeException {

    public PaysIntrouvableException(String pays) {
        super("Le pays '" + pays + "' n'est pas supporté ou configuré.");
    }
}
