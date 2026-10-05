package com.backendSiege.infrastructure.persistence.mapper;

import com.backendSiege.domain.models.User;
import com.backendSiege.infrastructure.persistence.entity.UserEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

/** Traduction entre l'entité JPA des utilisateurs et le modèle du domaine. */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface UserEntityMapper {

    User toModel(UserEntity entity);

    UserEntity toEntity(User user);
}
