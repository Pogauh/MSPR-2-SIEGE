package com.backendSiege.support;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/** Petits utilitaires JSON pour les tests (JSON écrit avec des apostrophes pour rester lisible dans le code). */
public final class Json {

    private static final JsonMapper MAPPER = JsonMapper.builder().build();

    private Json() {
    }

    /** {@code "{'a':1}"} devient {@code {"a":1}}. */
    public static String json(String avecApostrophes) {
        return avecApostrophes.replace('\'', '"');
    }

    public static JsonNode lire(String json) {
        return MAPPER.readTree(json);
    }

    /** {@code true} si les deux documents ont le même contenu, quel que soit l'ordre des champs d'un objet. */
    public static boolean equivalents(String a, String b) {
        return lire(a).equals(lire(b));
    }
}
