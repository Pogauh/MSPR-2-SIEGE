package com.backendSiege.domain.exceptions;

/** Le pays demandé comme filtre n'est configuré dans aucun backend du siège : la demande est invalide. */
public class PaysNonSupporteException extends RuntimeException {

    public PaysNonSupporteException(String pays) {
        super("Le pays fourni '" + pays + "' n'est pas supporté ou configuré.");
    }
}
