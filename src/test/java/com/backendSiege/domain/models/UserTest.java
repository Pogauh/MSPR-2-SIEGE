package com.backendSiege.domain.models;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UserTest {

    @Test
    void nouveau_est_un_utilisateur_sans_identifiant_ni_pays_avec_un_seul_role() {
        User user = User.nouveau("neo@siege.fr", "hache", Role.ROLE_QUALITY);

        assertThat(user.id()).isNull();
        assertThat(user.email()).isEqualTo("neo@siege.fr");
        assertThat(user.password()).isEqualTo("hache");
        assertThat(user.roles()).containsExactly(Role.ROLE_QUALITY);
        assertThat(user.countryId()).isNull();
    }

    @Test
    void les_roles_portent_le_prefixe_ROLE_attendu_par_spring_security() {
        for (Role role : Role.values()) {
            assertThat(role.name()).startsWith("ROLE_");
        }
    }
}
