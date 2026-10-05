package com.backendSiege.infrastructure.persistence;

import com.backendSiege.domain.models.Role;
import com.backendSiege.domain.models.User;
import com.backendSiege.infrastructure.persistence.adapter.UserAdapter;
import com.backendSiege.infrastructure.persistence.mapper.UserEntityMapperImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Les utilisateurs sont enregistrés pour de vrai (schéma Hibernate sur H2) : identifiant généré, rôles, unicité de l'e-mail. */
@DataJpaTest
@ActiveProfiles("test")
@Import({UserAdapter.class, UserEntityMapperImpl.class})
class UserAdapterIT {

    @Autowired
    UserAdapter adapter;

    @Test
    void un_utilisateur_enregistre_recoit_un_identifiant_et_se_retrouve_par_son_email_avec_ses_roles() {
        User enregistre = adapter.save(new User(null, "marie@siege.fr", "hache", Set.of(Role.ROLE_QUALITY, Role.ROLE_WAREHOUSE_MANAGER), "colombie"));

        assertThat(enregistre.id()).isNotNull();
        assertThat(adapter.findByEmail("marie@siege.fr")).hasValueSatisfying(user -> {
            assertThat(user.id()).isEqualTo(enregistre.id());
            assertThat(user.password()).isEqualTo("hache");
            assertThat(user.countryId()).isEqualTo("colombie");
            assertThat(user.roles()).containsExactlyInAnyOrder(Role.ROLE_QUALITY, Role.ROLE_WAREHOUSE_MANAGER);
        });
    }

    @Test
    void un_utilisateur_sans_pays_est_enregistre_sans_pays() {
        adapter.save(User.nouveau("neo@siege.fr", "hache", Role.ROLE_HQ));

        assertThat(adapter.findByEmail("neo@siege.fr")).hasValueSatisfying(user -> {
            assertThat(user.countryId()).isNull();
            assertThat(user.roles()).containsExactly(Role.ROLE_HQ);
        });
    }

    @Test
    void un_email_inconnu_ne_donne_rien() {
        assertThat(adapter.findByEmail("inconnu@siege.fr")).isEmpty();
    }

    @Test
    void l_email_est_unique() {
        adapter.save(User.nouveau("doublon@siege.fr", "hache", Role.ROLE_HQ));

        assertThatThrownBy(() -> {
            adapter.save(User.nouveau("doublon@siege.fr", "autre", Role.ROLE_ADMIN));
            // l'écriture est différée jusqu'à la synchronisation de la session
            adapter.findByEmail("doublon@siege.fr");
        }).isInstanceOf(DataIntegrityViolationException.class);
    }
}
