package com.backendSiege.domain.services;

import com.backendSiege.domain.exceptions.EmailDejaUtiliseException;
import com.backendSiege.domain.exceptions.IdentifiantsInvalidesException;
import com.backendSiege.domain.exceptions.RoleInterditALInscriptionException;
import com.backendSiege.domain.exceptions.UtilisateurIntrouvableException;
import com.backendSiege.domain.models.Role;
import com.backendSiege.domain.models.User;
import com.backendSiege.domain.ports.PasswordHasherPort;
import com.backendSiege.domain.ports.TokenPort;
import com.backendSiege.domain.ports.UserPort;

import java.util.Optional;
import java.util.Set;

/** Inscription, connexion et identification des utilisateurs du siège. */
public class AuthService {

    /** Rôle d'un utilisateur qui s'inscrit sans en préciser un. */
    static final Role ROLE_PAR_DEFAUT = Role.ROLE_HQ;

    /** Rôles qu'on ne peut jamais demander en s'inscrivant : l'inscription est publique, l'administration ne doit pas pouvoir s'y obtenir. */
    static final Set<Role> ROLES_INTERDITS_A_L_INSCRIPTION = Set.of(Role.ROLE_ADMIN);

    private final UserPort userPort;
    private final PasswordHasherPort passwordHasher;
    private final TokenPort tokenPort;

    public AuthService(UserPort userPort, PasswordHasherPort passwordHasher, TokenPort tokenPort) {
        this.userPort = userPort;
        this.passwordHasher = passwordHasher;
        this.tokenPort = tokenPort;
    }

    /**
     * Inscrit un utilisateur.
     *
     * @param role son rôle, {@link #ROLE_PAR_DEFAUT} s'il est {@code null}
     * @return le jeton d'authentification du nouvel utilisateur
     * @throws RoleInterditALInscriptionException si le rôle demandé est {@link #ROLES_INTERDITS_A_L_INSCRIPTION} (rien n'est enregistré)
     * @throws EmailDejaUtiliseException          si l'e-mail est déjà inscrit
     */
    public String register(String email, String motDePasse, Role role) {
        if (role != null && ROLES_INTERDITS_A_L_INSCRIPTION.contains(role)) {
            throw new RoleInterditALInscriptionException(role);
        }
        if (userPort.findByEmail(email).isPresent()) {
            throw new EmailDejaUtiliseException();
        }
        User user = userPort.save(User.nouveau(email, passwordHasher.hash(motDePasse), role != null ? role : ROLE_PAR_DEFAUT));
        return tokenPort.generate(user);
    }

    /**
     * @return le jeton d'authentification de l'utilisateur
     * @throws IdentifiantsInvalidesException si l'e-mail est inconnu ou le mot de passe faux
     */
    public String login(String email, String motDePasse) {
        User user = userPort.findByEmail(email).orElseThrow(IdentifiantsInvalidesException::new);
        if (!passwordHasher.matches(motDePasse, user.password())) {
            throw new IdentifiantsInvalidesException();
        }
        return tokenPort.generate(user);
    }

    public Optional<User> findByEmail(String email) {
        return userPort.findByEmail(email);
    }

    /** @throws UtilisateurIntrouvableException si aucun utilisateur n'est inscrit avec cet e-mail */
    public User getCurrentUser(String email) {
        return userPort.findByEmail(email).orElseThrow(UtilisateurIntrouvableException::new);
    }

    /**
     * Identifie l'utilisateur à qui un jeton a été délivré.
     *
     * @return l'utilisateur, vide s'il n'existe plus ou si le jeton ne lui appartient pas ou est expiré
     * @throws RuntimeException si le jeton est illisible ou falsifié
     */
    public Optional<User> authenticate(String token) {
        String email = tokenPort.extractEmail(token);
        if (email == null) {
            return Optional.empty();
        }
        return userPort.findByEmail(email).filter(user -> tokenPort.isValid(token, user));
    }
}
