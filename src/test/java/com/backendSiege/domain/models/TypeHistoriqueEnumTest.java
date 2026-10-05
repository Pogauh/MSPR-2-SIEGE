package com.backendSiege.domain.models;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TypeHistoriqueEnumTest {

    @ParameterizedTest
    @CsvSource({
            "TRANSFERT, TRANSFERT",
            "transfert, TRANSFERT",
            "'  Shipped  ', SHIPPED",
            "ALERTE, ALERTE",
            "alerte, ALERTE",
            "ALERT, ALERTE",
            "alert, ALERTE",
            "' Alert ', ALERTE"})
    void depuisSaisie_ignore_la_casse_et_les_espaces_et_accepte_alert_pour_alerte(String saisie, TypeHistoriqueEnum attendu) {
        assertThat(TypeHistoriqueEnum.depuisSaisie(saisie)).isEqualTo(attendu);
    }

    @ParameterizedTest
    @ValueSource(strings = {"inconnu", "ALERTES", "TRANSFER", "expired"})
    void depuisSaisie_refuse_un_type_inconnu(String saisie) {
        assertThatThrownBy(() -> TypeHistoriqueEnum.depuisSaisie(saisie)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void depuisSaisie_refuse_une_saisie_vide() {
        assertThatThrownBy(() -> TypeHistoriqueEnum.depuisSaisie("")).isInstanceOf(IllegalArgumentException.class);
    }
}
