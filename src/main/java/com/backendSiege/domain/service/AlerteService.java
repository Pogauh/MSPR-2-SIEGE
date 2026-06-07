package com.backendSiege.domain.service;

import com.backendSiege.domain.model.Alerte;
import com.backendSiege.domain.port.AlertePort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class AlerteService {

    private final AlertePort alertePort;

    public AlerteService(AlertePort alertePort) {
        this.alertePort = alertePort;
    }

    public List<Alerte> getAllAlertesByEntrepotId(String pays, UUID entrepotId, LocalDateTime startDate, LocalDateTime endDate) {
        return alertePort.findAlertesByEntrepotId(pays , entrepotId, startDate, endDate);
    }
}
