package com.backendSiege.api.security;

import com.backendSiege.domain.models.Role;
import com.backendSiege.domain.models.User;
import com.backendSiege.domain.services.AuthService;
import io.jsonwebtoken.MalformedJwtException;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    private static final User MARIE = new User(UUID.randomUUID(), "marie@siege.fr", "hache",
            Set.of(Role.ROLE_QUALITY, Role.ROLE_WAREHOUSE_MANAGER), "colombie");

    @Mock
    AuthService authService;
    @Mock
    FilterChain chain;

    JwtAuthenticationFilter filtre;
    MockHttpServletRequest requete;
    MockHttpServletResponse reponse;

    @BeforeEach
    void preparer() {
        SecurityContextHolder.clearContext();
        filtre = new JwtAuthenticationFilter(authService);
        requete = new MockHttpServletRequest("GET", "/lots");
        requete.setServletPath("/lots");
        reponse = new MockHttpServletResponse();
    }

    @AfterEach
    void nettoyer() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void un_jeton_valide_authentifie_l_utilisateur_avec_ses_roles() throws Exception {
        requete.addHeader("Authorization", "Bearer jeton-valide");
        when(authService.authenticate("jeton-valide")).thenReturn(Optional.of(MARIE));

        filtre.doFilter(requete, reponse, chain);

        Authentication authentification = SecurityContextHolder.getContext().getAuthentication();
        assertThat(authentification).isNotNull();
        assertThat(authentification.getName()).isEqualTo("marie@siege.fr");
        assertThat(authentification.getAuthorities()).extracting(Object::toString)
                .containsExactlyInAnyOrder("ROLE_QUALITY", "ROLE_WAREHOUSE_MANAGER");
        assertThat(authentification.isAuthenticated()).isTrue();
        verify(chain).doFilter(requete, reponse);
    }

    @Test
    void un_jeton_qui_n_identifie_personne_laisse_la_requete_anonyme() throws Exception {
        requete.addHeader("Authorization", "Bearer jeton-perime");
        when(authService.authenticate("jeton-perime")).thenReturn(Optional.empty());

        filtre.doFilter(requete, reponse, chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(chain).doFilter(requete, reponse);
    }

    @Test
    void sans_en_tete_ou_avec_un_autre_schema_la_requete_reste_anonyme_sans_toucher_au_jeton() throws Exception {
        filtre.doFilter(requete, reponse, chain);
        requete.addHeader("Authorization", "Basic abc");
        filtre.doFilter(requete, reponse, chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verifyNoInteractions(authService);
    }

    @Test
    void un_jeton_illisible_fait_echouer_la_requete() {
        requete.addHeader("Authorization", "Bearer n-importe-quoi");
        when(authService.authenticate("n-importe-quoi")).thenThrow(new MalformedJwtException("illisible"));

        assertThatThrownBy(() -> filtre.doFilter(requete, reponse, chain)).isInstanceOf(MalformedJwtException.class);
    }

    @Test
    void une_requete_deja_authentifiee_n_est_pas_reauthentifiee() throws Exception {
        Authentication existante = org.mockito.Mockito.mock(Authentication.class);
        SecurityContextHolder.getContext().setAuthentication(existante);
        requete.addHeader("Authorization", "Bearer jeton");

        filtre.doFilter(requete, reponse, chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isSameAs(existante);
        verifyNoInteractions(authService);
    }

    @Test
    void les_routes_publiques_ne_sont_pas_filtrees() {
        for (String chemin : new String[]{"/api/auth/login", "/api/auth/register", "/swagger-ui/index.html", "/swagger-ui.html", "/v3/api-docs",
                "/v3/api-docs/swagger-config"}) {
            requete.setServletPath(chemin);
            assertThat(filtre.shouldNotFilter(requete)).as(chemin).isTrue();
        }
    }

    @Test
    void les_autres_routes_sont_filtrees_y_compris_me() {
        for (String chemin : new String[]{"/api/auth/me", "/lots", "/reporting/global", "/api/auth/login/autre"}) {
            requete.setServletPath(chemin);
            assertThat(filtre.shouldNotFilter(requete)).as(chemin).isFalse();
        }
    }
}
