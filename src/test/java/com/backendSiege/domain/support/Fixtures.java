package com.backendSiege.domain.support;

import com.backendSiege.domain.models.Historique;
import com.backendSiege.domain.models.Lot;
import com.backendSiege.domain.models.Pays;
import com.backendSiege.domain.models.Tolerances;
import com.backendSiege.domain.services.PaysService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/** Objets du domaine prêts à l'emploi pour les tests unitaires. */
public final class Fixtures {

    public static final UUID ID_LOT = UUID.fromString("11111111-1111-1111-1111-111111111111");
    public static final UUID ID_ZONE = UUID.fromString("22222222-2222-2222-2222-222222222222");
    public static final UUID ID_ENTREPOT = UUID.fromString("770e8400-e29b-41d4-a716-446655440011");
    public static final UUID ID_EXPLOITATION = UUID.fromString("aa0e8400-e29b-41d4-a716-446655440022");

    private Fixtures() {
    }

    /** Les trois pays configurés, dans l'ordre BRESIL, COLOMBIE, EQUATEUR. */
    public static List<Pays> troisPays() {
        return List.of(
                new Pays("BRESIL", "http://bresil/api/", new Tolerances(29, 55, 3, 2)),
                new Pays("COLOMBIE", "http://colombie/api/", new Tolerances(26, 80, 3, 2)),
                new Pays("EQUATEUR", "http://equateur/api/", new Tolerances(31, 60, 3, 2)));
    }

    public static PaysService paysService() {
        return new PaysService(Fixtures::troisPays);
    }

    public static Lot lot(LocalDateTime dateStockage) {
        return lot(ID_LOT, dateStockage);
    }

    public static Lot lot(UUID idLot, LocalDateTime dateStockage) {
        return new Lot(idLot, "COMPLIANT", 120, 2, null, dateStockage, LocalDate.of(2025, 12, 1), ID_ZONE.toString(),
                ID_EXPLOITATION, ID_ENTREPOT, List.of(
                        new Historique(UUID.randomUUID(), "TRANSFERT", LocalDateTime.of(2026, 1, 11, 9, 0), null, ID_ZONE, "Transfert")));
    }
}
