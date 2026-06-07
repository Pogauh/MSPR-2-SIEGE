package com.backendSiege.domain.port;

import com.backendSiege.domain.model.Alerte;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface AlertePort {

    List<Alerte> findAlertesByEntrepotId(String pays, UUID idEntrepot, LocalDateTime startDate, LocalDateTime endDate);
}
