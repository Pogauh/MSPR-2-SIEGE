package com.backendSiege.infrastructure.persistence;

import com.backendSiege.domain.models.Role;
import com.backendSiege.domain.models.User;
import com.backendSiege.infrastructure.persistence.adapter.UserAdapter;
import com.backendSiege.infrastructure.persistence.entity.UserEntity;
import com.backendSiege.infrastructure.persistence.mapper.UserEntityMapperImpl;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/** Traduction entre l'entité JPA et le modèle du domaine, sans base de données. */
class UserPersistenceTest {

    private static final UUID ID = UUID.randomUUID();

    private final UserEntityMapperImpl mapper = new UserEntityMapperImpl();

    @Test
    void l_entite_est_traduite_en_modele_du_domaine() {
        UserEntity entite = new UserEntity();
        entite.setId(ID);
        entite.setEmail("marie@siege.fr");
        entite.setPassword("hache");
        entite.setCountryId("colombie");
        entite.setRoles(Set.of(Role.ROLE_QUALITY, Role.ROLE_HQ));

        assertThat(mapper.toModel(entite)).isEqualTo(new User(ID, "marie@siege.fr", "hache", Set.of(Role.ROLE_QUALITY, Role.ROLE_HQ), "colombie"));
    }

    @Test
    void le_modele_est_traduit_en_entite() {
        UserEntity entite = mapper.toEntity(new User(null, "neo@siege.fr", "hache", Set.of(Role.ROLE_ADMIN), null));

        assertThat(entite.getId()).isNull();
        assertThat(entite.getEmail()).isEqualTo("neo@siege.fr");
        assertThat(entite.getPassword()).isEqualTo("hache");
        assertThat(entite.getCountryId()).isNull();
        assertThat(entite.getRoles()).containsExactly(Role.ROLE_ADMIN);
    }

    @Test
    void l_adaptateur_enregistre_l_entite_et_renvoie_le_modele_enregistre() {
        var repository = Mockito.mock(com.backendSiege.infrastructure.persistence.repository.JpaUserRepository.class);
        Mockito.when(repository.save(Mockito.any(UserEntity.class))).thenAnswer(appel -> {
            UserEntity entite = appel.getArgument(0);
            entite.setId(ID);
            return entite;
        });
        UserAdapter adapter = new UserAdapter(repository, mapper);

        User enregistre = adapter.save(User.nouveau("neo@siege.fr", "hache", Role.ROLE_QUALITY));

        assertThat(enregistre.id()).isEqualTo(ID);
        assertThat(enregistre.roles()).containsExactly(Role.ROLE_QUALITY);
    }

    @Test
    void l_adaptateur_traduit_le_resultat_de_la_recherche_par_email() {
        var repository = Mockito.mock(com.backendSiege.infrastructure.persistence.repository.JpaUserRepository.class);
        UserEntity entite = new UserEntity();
        entite.setId(ID);
        entite.setEmail("marie@siege.fr");
        entite.setPassword("hache");
        entite.setRoles(Set.of(Role.ROLE_HQ));
        Mockito.when(repository.findByEmail("marie@siege.fr")).thenReturn(java.util.Optional.of(entite));
        Mockito.when(repository.findByEmail("inconnu@siege.fr")).thenReturn(java.util.Optional.empty());
        UserAdapter adapter = new UserAdapter(repository, mapper);

        assertThat(adapter.findByEmail("marie@siege.fr")).hasValueSatisfying(user -> assertThat(user.id()).isEqualTo(ID));
        assertThat(adapter.findByEmail("inconnu@siege.fr")).isEmpty();
    }
}
