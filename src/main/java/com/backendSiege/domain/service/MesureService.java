package com.backendSiege.domain.service;

import com.backendSiege.domain.port.MesurePort;
import com.backendSiege.api.dto.MesureResponseDTO;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@AllArgsConstructor
@Service
public class MesureService {

    private final MesurePort mesurePort;

    public MesureResponseDTO getMesureById(UUID id) {
        return mesurePort.findMesureById(id);
    }
}

