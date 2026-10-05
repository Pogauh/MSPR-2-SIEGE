package com.backendSiege.domain.exceptions;

/** Le backend d'un pays n'a pas pu répondre (injoignable, erreur HTTP, réponse illisible). */
public class PaysIndisponibleException extends RuntimeException {

    public PaysIndisponibleException(String codePays, Throwable cause) {
        super("Le backend du pays " + codePays + " n'a pas pu répondre", cause);
    }
}
