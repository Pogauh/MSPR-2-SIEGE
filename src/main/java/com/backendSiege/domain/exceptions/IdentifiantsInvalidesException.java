package com.backendSiege.domain.exceptions;

/** L'e-mail est inconnu ou le mot de passe est faux (les deux cas ne sont volontairement pas distingués). */
public class IdentifiantsInvalidesException extends RuntimeException {

    public IdentifiantsInvalidesException() {
        super("Email ou mot de passe incorrect");
    }
}
