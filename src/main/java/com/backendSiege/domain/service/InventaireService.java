package com.backendSiege.domain.service;

import com.backendSiege.domain.model.Inventaire;
import com.backendSiege.domain.port.InventairePort;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@AllArgsConstructor
@Service
public class InventaireService {

    private final InventairePort inventairePort;

    public List<Inventaire> getInventaireMondial() {
        return inventairePort.findInventaireMondial();
    }
}

