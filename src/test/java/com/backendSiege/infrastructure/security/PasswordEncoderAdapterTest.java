package com.backendSiege.infrastructure.security;

import com.backendSiege.infrastructure.config.SecurityBeansConfig;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;

class PasswordEncoderAdapterTest {

    private final PasswordEncoderAdapter adapter = new PasswordEncoderAdapter(new SecurityBeansConfig().passwordEncoder());

    @Test
    void le_mot_de_passe_est_hache_en_bcrypt_avec_un_sel_different_a_chaque_fois() {
        String premier = adapter.hash("secret");
        String second = adapter.hash("secret");

        assertThat(premier).startsWith("$2a$").isNotEqualTo("secret").isNotEqualTo(second);
    }

    @Test
    void matches_compare_un_mot_de_passe_a_son_hache() {
        String hache = adapter.hash("secret");

        assertThat(adapter.matches("secret", hache)).isTrue();
        assertThat(adapter.matches("faux", hache)).isFalse();
    }

    @Test
    void les_hashs_bcrypt_des_donnees_initiales_du_siege_restent_valides() {
        // utilisateurs de postgres/init_siege.sql : mot de passe « password123 »
        String hacheInitial = "$2a$10$gxHepxcprQtRsiubAFktLudW6tlOdkWfN7a03OjMxog1oVpoVaIIG";

        assertThat(adapter.matches("password123", hacheInitial)).isTrue();
        assertThat(new BCryptPasswordEncoder().matches("password123", hacheInitial)).isTrue();
    }
}
