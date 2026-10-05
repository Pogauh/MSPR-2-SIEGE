package com.backendSiege.api.exception;

import com.backendSiege.domain.exceptions.PaysIntrouvableException;
import com.backendSiege.domain.exceptions.PaysNonSupporteException;
import com.backendSiege.domain.exceptions.PaysObligatoireException;
import com.backendSiege.domain.exceptions.RoleInterditALInscriptionException;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.io.IOException;

/**
 * Traduit en réponses HTTP les erreurs métier du domaine.
 * <p>
 * Les erreurs sont signalées par {@code sendError} : Spring Security refuse ensuite la redirection interne vers {@code /error}
 * à un contexte anonyme, si bien que le client reçoit un {@code 403} sans corps, comme pour toute autre erreur non traitée.
 */
@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler({PaysNonSupporteException.class, PaysObligatoireException.class})
    public void handleRequeteInvalide(RuntimeException e, HttpServletResponse response) throws IOException {
        response.sendError(HttpStatus.BAD_REQUEST.value(), e.getMessage());
    }

    /** Inscription avec un rôle réservé (administrateur) : interdite. */
    @ExceptionHandler(RoleInterditALInscriptionException.class)
    public void handleRoleInterdit(RoleInterditALInscriptionException e, HttpServletResponse response) throws IOException {
        response.sendError(HttpStatus.FORBIDDEN.value(), e.getMessage());
    }

    @ExceptionHandler(PaysIntrouvableException.class)
    public void handlePaysIntrouvable(PaysIntrouvableException e, HttpServletResponse response) throws IOException {
        response.sendError(HttpStatus.NOT_FOUND.value(), e.getMessage());
    }
}
