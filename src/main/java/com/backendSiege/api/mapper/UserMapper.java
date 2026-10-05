package com.backendSiege.api.mapper;

import com.backendSiege.api.dto.auth.MeResponseDTO;
import com.backendSiege.domain.models.User;
import org.mapstruct.Mapper;

@Mapper(config = ApiMapperConfig.class)
public interface UserMapper {

    MeResponseDTO toMeResponse(User user);
}
