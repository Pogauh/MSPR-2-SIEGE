package com.backendSiege.domain.exceptions;

/** Le pays est obligatoire pour cette demande et n'a pas été précisé. */
public class PaysObligatoireException extends RuntimeException {

    public PaysObligatoireException() {
        super("Le paramètre 'pays' est obligatoire.");
    }
}
