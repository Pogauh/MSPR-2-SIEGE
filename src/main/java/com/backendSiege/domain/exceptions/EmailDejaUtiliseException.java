package com.backendSiege.domain.exceptions;

/** Un utilisateur est déjà inscrit avec cette adresse e-mail. */
public class EmailDejaUtiliseException extends RuntimeException {

    public EmailDejaUtiliseException() {
        super("Email déjà utilisé");
    }
}
