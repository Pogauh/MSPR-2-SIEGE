package com.backendSiege.domain.exceptions;

import com.backendSiege.domain.models.Role;

/** Le rôle demandé à l'inscription ne peut pas s'obtenir en s'inscrivant (il est attribué par un administrateur, pas par l'API publique). */
public class RoleInterditALInscriptionException extends RuntimeException {

    public RoleInterditALInscriptionException(Role role) {
        super("Le rôle " + role + " ne peut pas être obtenu par inscription");
    }
}
