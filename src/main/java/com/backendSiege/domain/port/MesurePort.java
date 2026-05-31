package com.backendSiege.domain.port;

import com.backendSiege.api.dto.MesureResponseDTO;
import java.util.UUID;

public interface MesurePort {
    MesureResponseDTO findMesureById(UUID id);
}
