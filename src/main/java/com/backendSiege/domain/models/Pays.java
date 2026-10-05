package com.backendSiege.domain.models;

/**
 * Pays producteur dont le siège agrège le backend.
 *
 * @param code       nom du pays tel que configuré (BRESIL, COLOMBIE…)
 * @param url        adresse de l'API REST du backend du pays
 * @param tolerances plage climatique idéale du pays
 */
public record Pays(String code, String url, Tolerances tolerances) {
}
