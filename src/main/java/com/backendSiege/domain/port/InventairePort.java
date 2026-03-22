package com.backendSiege.domain.port;

import com.backendSiege.domain.model.Inventaire;

import java.util.List;

public interface InventairePort {

    List<Inventaire> findInventaireMondial();
}

