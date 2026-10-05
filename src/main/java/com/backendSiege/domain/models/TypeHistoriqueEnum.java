package com.backendSiege.domain.models;

import java.util.Locale;

/** Types d'événements de l'historique d'un lot que l'on peut demander au pays. */
public enum TypeHistoriqueEnum {
    TRANSFERT, ALERTE, SHIPPED;

    /**
     * Interprète une saisie : la casse et les espaces autour sont ignorés et {@code ALERT} est accepté comme alias d'{@code ALERTE}.
     *
     * @throws IllegalArgumentException si la saisie ne désigne aucun type
     */
    public static TypeHistoriqueEnum depuisSaisie(String saisie) {
        String normalise = saisie.trim().toUpperCase(Locale.ROOT);
        if ("ALERT".equals(normalise)) {
            normalise = ALERTE.name();
        }
        return valueOf(normalise);
    }
}
