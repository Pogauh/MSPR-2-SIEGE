package com.backendSiege.infrastructure.persistence.adapter;

import com.backendSiege.domain.models.User;
import com.backendSiege.domain.ports.UserPort;
import com.backendSiege.infrastructure.persistence.mapper.UserEntityMapper;
import com.backendSiege.infrastructure.persistence.repository.JpaUserRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

/** Utilisateurs du siège enregistrés en base. */
@Component
public class UserAdapter implements UserPort {

    private final JpaUserRepository repository;
    private final UserEntityMapper mapper;

    public UserAdapter(JpaUserRepository repository, UserEntityMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return repository.findByEmail(email).map(mapper::toModel);
    }

    @Override
    public User save(User user) {
        return mapper.toModel(repository.save(mapper.toEntity(user)));
    }
}
