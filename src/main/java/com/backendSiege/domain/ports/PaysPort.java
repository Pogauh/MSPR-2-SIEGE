package com.backendSiege.domain.ports;

import com.backendSiege.domain.models.Pays;

import java.util.List;

/** Les pays dont le siège agrège le backend. */
public interface PaysPort {

    /** Tous les pays configurés, dans l'ordre de la configuration. */
    List<Pays> findAll();
}
