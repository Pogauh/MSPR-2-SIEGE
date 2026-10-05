package com.backendSiege.domain.services;

import com.backendSiege.domain.models.Pays;
import com.backendSiege.domain.support.Fixtures;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PaysServiceTest {

    private final PaysService service = Fixtures.paysService();

    @Test
    void findAll_renvoie_les_pays_dans_l_ordre_de_la_configuration() {
        assertThat(service.findAll()).extracting(Pays::code).containsExactly("BRESIL", "COLOMBIE", "EQUATEUR");
    }

    @Test
    void codes_sont_normalises_en_majuscules() {
        PaysService minuscules = new PaysService(() -> List.of(new Pays("bresil", "u", null), new Pays(" Colombie ", "u", null)));

        assertThat(minuscules.codes()).containsExactly("BRESIL", "COLOMBIE");
    }

    @ParameterizedTest
    @ValueSource(strings = {"BRESIL", "bresil", "Bresil", "  bresil  ", "\tBRESIL\n"})
    void resoudre_ignore_la_casse_et_les_espaces(String saisie) {
        assertThat(service.resoudre(saisie)).contains("BRESIL");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t", "atlantide", "bre sil"})
    void resoudre_ne_trouve_rien_pour_une_saisie_absente_vide_ou_inconnue(String saisie) {
        assertThat(service.resoudre(saisie)).isEmpty();
    }

    @Test
    void codesOuTous_renvoie_le_pays_designe() {
        assertThat(service.codesOuTous("colombie")).containsExactly("COLOMBIE");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"  ", "atlantide"})
    void codesOuTous_renvoie_tous_les_pays_si_la_saisie_est_absente_vide_ou_inconnue(String saisie) {
        assertThat(service.codesOuTous(saisie)).containsExactly("BRESIL", "COLOMBIE", "EQUATEUR");
    }

    @Test
    void la_normalisation_ne_depend_pas_de_la_langue_du_systeme() {
        java.util.Locale defaut = java.util.Locale.getDefault();
        try {
            java.util.Locale.setDefault(java.util.Locale.forLanguageTag("tr-TR"));
            PaysService turc = new PaysService(() -> List.of(new Pays("ISRAEL", "u", null)));

            assertThat(turc.resoudre("israel")).contains("ISRAEL");
        } finally {
            java.util.Locale.setDefault(defaut);
        }
    }
}
