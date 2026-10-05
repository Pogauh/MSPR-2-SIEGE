package com.backendSiege.api.security;

import com.backendSiege.domain.models.Role;
import com.backendSiege.domain.models.User;
import com.backendSiege.domain.services.AuthService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    AuthService authService;

    @Test
    void charge_l_utilisateur_avec_son_mot_de_passe_hache_et_ses_roles() {
        when(authService.findByEmail("marie@siege.fr"))
                .thenReturn(Optional.of(new User(UUID.randomUUID(), "marie@siege.fr", "hache", Set.of(Role.ROLE_ADMIN), null)));

        UserDetails details = new CustomUserDetailsService(authService).loadUserByUsername("marie@siege.fr");

        assertThat(details.getUsername()).isEqualTo("marie@siege.fr");
        assertThat(details.getPassword()).isEqualTo("hache");
        assertThat(details.getAuthorities()).extracting(Object::toString).containsExactly("ROLE_ADMIN");
    }

    @Test
    void refuse_un_utilisateur_inconnu() {
        when(authService.findByEmail("inconnu@siege.fr")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> new CustomUserDetailsService(authService).loadUserByUsername("inconnu@siege.fr"))
                .isInstanceOf(UsernameNotFoundException.class);
    }
}
