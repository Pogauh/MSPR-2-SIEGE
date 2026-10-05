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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    private static final User MARIE = new User(UUID.randomUUID(), "marie@siege.fr", "hache", Set.of(Role.ROLE_QUALITY), "colombie");

    @Mock
    UserPort userPort;
    @Mock
    PasswordHasherPort hasher;
    @Mock
    TokenPort tokenPort;

    AuthService service;

    @BeforeEach
    void creerLeService() {
        service = new AuthService(userPort, hasher, tokenPort);
    }

    // ------------------------------------------------------------------ inscription

    @Test
    void register_enregistre_un_utilisateur_au_mot_de_passe_hache_et_renvoie_son_jeton() {
        when(userPort.findByEmail("neo@siege.fr")).thenReturn(Optional.empty());
        when(hasher.hash("secret")).thenReturn("hache");
        when(userPort.save(any())).thenAnswer(appel -> {
            User recu = appel.getArgument(0);
            return new User(UUID.randomUUID(), recu.email(), recu.password(), recu.roles(), recu.countryId());
        });
        when(tokenPort.generate(any())).thenReturn("jeton");

        String jeton = service.register("neo@siege.fr", "secret", Role.ROLE_QUALITY);

        assertThat(jeton).isEqualTo("jeton");
        ArgumentCaptor<User> enregistre = ArgumentCaptor.forClass(User.class);
        verify(userPort).save(enregistre.capture());
        assertThat(enregistre.getValue()).satisfies(user -> {
            assertThat(user.id()).isNull();
            assertThat(user.email()).isEqualTo("neo@siege.fr");
            assertThat(user.password()).isEqualTo("hache");
            assertThat(user.roles()).containsExactly(Role.ROLE_QUALITY);
            assertThat(user.countryId()).isNull();
        });
        ArgumentCaptor<User> pourLeJeton = ArgumentCaptor.forClass(User.class);
        verify(tokenPort).generate(pourLeJeton.capture());
        assertThat(pourLeJeton.getValue().id()).as("le jeton est généré pour l'utilisateur enregistré").isNotNull();
    }

    @Test
    void register_sans_role_donne_le_role_HQ() {
        when(userPort.findByEmail("neo@siege.fr")).thenReturn(Optional.empty());
        when(hasher.hash("secret")).thenReturn("hache");
        when(userPort.save(any())).thenAnswer(appel -> appel.getArgument(0));

        service.register("neo@siege.fr", "secret", null);

        ArgumentCaptor<User> enregistre = ArgumentCaptor.forClass(User.class);
        verify(userPort).save(enregistre.capture());
        assertThat(enregistre.getValue().roles()).containsExactly(Role.ROLE_HQ).containsExactly(AuthService.ROLE_PAR_DEFAUT);
    }

    @Test
    void register_refuse_le_role_administrateur_sans_rien_verifier_ni_enregistrer() {
        assertThatThrownBy(() -> service.register("neo@siege.fr", "secret", Role.ROLE_ADMIN))
                .isInstanceOf(RoleInterditALInscriptionException.class)
                .hasMessageContaining("ROLE_ADMIN");

        verifyNoInteractions(userPort, hasher, tokenPort);
    }

    @Test
    void register_refuse_le_role_administrateur_meme_si_l_email_est_deja_utilise() {
        assertThatThrownBy(() -> service.register("marie@siege.fr", "secret", Role.ROLE_ADMIN))
                .isInstanceOf(RoleInterditALInscriptionException.class);

        verifyNoInteractions(userPort);
    }

    @Test
    void register_accepte_tous_les_autres_roles() {
        when(userPort.findByEmail(any())).thenReturn(Optional.empty());
        when(hasher.hash(any())).thenReturn("hache");
        when(userPort.save(any())).thenAnswer(appel -> appel.getArgument(0));
        when(tokenPort.generate(any())).thenReturn("jeton");

        for (Role role : Role.values()) {
            if (role != Role.ROLE_ADMIN) {
                assertThat(service.register("neo@siege.fr", "secret", role)).as(role.name()).isEqualTo("jeton");
            }
        }
    }

    @Test
    void seul_le_role_administrateur_est_interdit_a_l_inscription() {
        assertThat(AuthService.ROLES_INTERDITS_A_L_INSCRIPTION).containsExactly(Role.ROLE_ADMIN);
    }

    @Test
    void register_refuse_un_email_deja_utilise_sans_rien_enregistrer() {
        when(userPort.findByEmail("marie@siege.fr")).thenReturn(Optional.of(MARIE));

        assertThatThrownBy(() -> service.register("marie@siege.fr", "secret", null)).isInstanceOf(EmailDejaUtiliseException.class);

        verify(userPort, never()).save(any());
        verify(tokenPort, never()).generate(any());
    }

    // ------------------------------------------------------------------ connexion

    @Test
    void login_renvoie_le_jeton_si_le_mot_de_passe_est_bon() {
        when(userPort.findByEmail("marie@siege.fr")).thenReturn(Optional.of(MARIE));
        when(hasher.matches("secret", "hache")).thenReturn(true);
        when(tokenPort.generate(MARIE)).thenReturn("jeton");

        assertThat(service.login("marie@siege.fr", "secret")).isEqualTo("jeton");
    }

    @Test
    void login_refuse_un_mot_de_passe_faux() {
        when(userPort.findByEmail("marie@siege.fr")).thenReturn(Optional.of(MARIE));
        when(hasher.matches("faux", "hache")).thenReturn(false);

        assertThatThrownBy(() -> service.login("marie@siege.fr", "faux")).isInstanceOf(IdentifiantsInvalidesException.class);

        verify(tokenPort, never()).generate(any());
    }

    @Test
    void login_refuse_un_email_inconnu_sans_tester_de_mot_de_passe() {
        when(userPort.findByEmail("inconnu@siege.fr")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.login("inconnu@siege.fr", "secret")).isInstanceOf(IdentifiantsInvalidesException.class);

        verify(hasher, never()).matches(any(), any());
    }

    // ------------------------------------------------------------------ utilisateur courant

    @Test
    void getCurrentUser_renvoie_l_utilisateur() {
        when(userPort.findByEmail("marie@siege.fr")).thenReturn(Optional.of(MARIE));

        assertThat(service.getCurrentUser("marie@siege.fr")).isEqualTo(MARIE);
    }

    @Test
    void getCurrentUser_echoue_si_l_utilisateur_n_existe_pas() {
        when(userPort.findByEmail("inconnu@siege.fr")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getCurrentUser("inconnu@siege.fr")).isInstanceOf(UtilisateurIntrouvableException.class);
    }

    @Test
    void findByEmail_renvoie_l_utilisateur_ou_rien() {
        when(userPort.findByEmail("marie@siege.fr")).thenReturn(Optional.of(MARIE));
        when(userPort.findByEmail("inconnu@siege.fr")).thenReturn(Optional.empty());

        assertThat(service.findByEmail("marie@siege.fr")).contains(MARIE);
        assertThat(service.findByEmail("inconnu@siege.fr")).isEmpty();
    }

    // ------------------------------------------------------------------ identification par jeton

    @Test
    void authenticate_identifie_l_utilisateur_a_qui_le_jeton_a_ete_delivre() {
        when(tokenPort.extractEmail("jeton")).thenReturn("marie@siege.fr");
        when(userPort.findByEmail("marie@siege.fr")).thenReturn(Optional.of(MARIE));
        when(tokenPort.isValid("jeton", MARIE)).thenReturn(true);

        assertThat(service.authenticate("jeton")).contains(MARIE);
    }

    @Test
    void authenticate_ne_reconnait_personne_si_le_jeton_n_est_plus_valide() {
        when(tokenPort.extractEmail("jeton")).thenReturn("marie@siege.fr");
        when(userPort.findByEmail("marie@siege.fr")).thenReturn(Optional.of(MARIE));
        when(tokenPort.isValid("jeton", MARIE)).thenReturn(false);

        assertThat(service.authenticate("jeton")).isEmpty();
    }

    @Test
    void authenticate_ne_reconnait_personne_si_l_utilisateur_n_existe_plus_ou_si_le_jeton_n_a_pas_de_sujet() {
        when(tokenPort.extractEmail("fantome")).thenReturn("parti@siege.fr");
        when(userPort.findByEmail("parti@siege.fr")).thenReturn(Optional.empty());
        when(tokenPort.extractEmail("anonyme")).thenReturn(null);

        assertThat(service.authenticate("fantome")).isEmpty();
        assertThat(service.authenticate("anonyme")).isEmpty();
    }

    @Test
    void authenticate_laisse_remonter_l_erreur_d_un_jeton_illisible() {
        when(tokenPort.extractEmail("pasunjeton")).thenThrow(new IllegalArgumentException("jeton illisible"));

        assertThatThrownBy(() -> service.authenticate("pasunjeton")).isInstanceOf(IllegalArgumentException.class);
    }
}
