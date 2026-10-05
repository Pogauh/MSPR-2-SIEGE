package com.backendSiege.domain.services;

import com.backendSiege.domain.models.Pays;
import com.backendSiege.domain.ports.PaysPort;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * Connaissance des pays configurés. Un pays est désigné par son nom sans tenir compte de la casse ni des espaces autour ;
 * son <em>code normalisé</em> (en majuscules) est celui que les ports utilisent pour atteindre son backend.
 */
public class PaysService {

    private final PaysPort paysPort;

    public PaysService(PaysPort paysPort) {
        this.paysPort = paysPort;
    }

    public List<Pays> findAll() {
        return paysPort.findAll();
    }

    /** Les codes normalisés de tous les pays, dans l'ordre de la configuration. */
    public List<String> codes() {
        return paysPort.findAll().stream().map(pays -> normaliser(pays.code())).toList();
    }

    /** @return le code normalisé du pays désigné, vide si la saisie est absente, vide ou ne correspond à aucun pays configuré */
    public Optional<String> resoudre(String saisie) {
        if (saisie == null || saisie.isBlank()) {
            return Optional.empty();
        }
        String code = normaliser(saisie);
        return codes().stream().filter(code::equals).findFirst();
    }

    /** Les pays à interroger : celui qui est désigné, ou tous si la saisie est absente, vide ou inconnue. */
    public List<String> codesOuTous(String saisie) {
        return resoudre(saisie).map(List::of).orElseGet(this::codes);
    }

    private static String normaliser(String nom) {
        return nom.trim().toUpperCase(Locale.ROOT);
    }
}
