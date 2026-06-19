CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE exploitation (
                              id_exploitation UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                              nom VARCHAR(50),
                              adresse VARCHAR(70)
);

CREATE TABLE entrepot (
                          id_entrepot UUID PRIMARY KEY,
                          nom VARCHAR(50),
                          localisation VARCHAR(150)
);

CREATE TABLE zone (
                      id_zone UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                      nom VARCHAR(50) NOT NULL,
                      id_entrepot UUID NOT NULL REFERENCES entrepot(id_entrepot) ON DELETE CASCADE,
                      UNIQUE (nom, id_entrepot)
);

CREATE TABLE capteur (
                         id_capteur UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                         code_serie VARCHAR(50) NOT NULL UNIQUE,
                         id_zone UUID NOT NULL REFERENCES zone(id_zone) ON DELETE CASCADE,
                         UNIQUE (id_zone)
);

CREATE TABLE lot (
                     id_lot UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                     status VARCHAR(20),
                     quantite INT,
                     caracteristique INT,
                     date_sortie TIMESTAMP,
                     date_stockage TIMESTAMP,
                     date_production DATE,
                     id_exploitation UUID NOT NULL REFERENCES exploitation(id_exploitation),
                     id_zone UUID NOT NULL REFERENCES zone(id_zone)
);

CREATE TABLE mesure (
                        id_mesure UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                        temperature INT,
                        humidite INT,
                        date_mesure TIMESTAMP,
                        id_capteur UUID NOT NULL REFERENCES capteur(id_capteur)
);

CREATE TABLE alerte (
                        id_alerte UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                        type_alerte VARCHAR(50),
                        description VARCHAR(255),
                        date_emission TIMESTAMP,
                        id_capteur UUID REFERENCES capteur(id_capteur)
);

CREATE TABLE historique (
                            id_historique UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                            type VARCHAR(50),
                            date_emission TIMESTAMP,
                            zone_precedente UUID,
                            zone_actuelle UUID,
                            detail VARCHAR(255),
                            id_lot UUID NOT NULL REFERENCES lot(id_lot)
);

INSERT INTO entrepot (id_entrepot, nom, localisation)
VALUES
    ('7a5e94dc-1432-4bba-934e-fd80f0ecdd94', 'Entrepot A', 'Colombie'),
    ('8a607c44-08a6-403a-b536-7853452b7772', 'Entrepot B', 'Colombie')
    ON CONFLICT (id_entrepot) DO NOTHING;

WITH entrepot_seed (id_entrepot) AS (
    VALUES
        ('7a5e94dc-1432-4bba-934e-fd80f0ecdd94'::uuid),
        ('8a607c44-08a6-403a-b536-7853452b7772'::uuid)
),
     zone_seed (nom_zone) AS (
         VALUES
             ('A'), ('B'), ('C'), ('D'), ('E'), ('F'), ('G'), ('H'),
             ('I'), ('J'), ('K'), ('L'), ('M'), ('N'), ('O'), ('P'),
             ('Q'), ('R'), ('S'), ('T'), ('U'), ('V'), ('W'), ('X'),
             ('Y'), ('Z'), ('AA'), ('AB'), ('AC'), ('AD')
     )
INSERT INTO zone (id_zone, nom, id_entrepot)
SELECT
    uuid_generate_v4(),
    zone_seed.nom_zone,
    entrepot_seed.id_entrepot
FROM zone_seed
         CROSS JOIN entrepot_seed
    ON CONFLICT (nom, id_entrepot) DO NOTHING;

WITH capteur_seed AS (
    SELECT
        z.id_zone,
        row_number() OVER (ORDER BY z.id_zone) AS idx
    FROM zone z
    WHERE z.id_entrepot IN (
                            '7a5e94dc-1432-4bba-934e-fd80f0ecdd94'::uuid,
                            '8a607c44-08a6-403a-b536-7853452b7772'::uuid
        )
)
INSERT INTO capteur (id_capteur, code_serie, id_zone)
SELECT
    uuid_generate_v4(),
    format('02:AA:BB:CC:DD:%s', lpad(upper(to_hex(capteur_seed.idx)), 2, '0')),
    capteur_seed.id_zone
FROM capteur_seed
    ON CONFLICT (id_zone) DO NOTHING;

INSERT INTO exploitation (id_exploitation, nom, adresse)
VALUES ('c5333186-716c-4198-93b1-42d7bb23519e', 'Exploitation Pepper', '12 allée du salut')
    ON CONFLICT (id_exploitation) DO NOTHING;


WITH lot_multiplier AS (
    -- Génère deux lignes pour multiplier chaque zone par 2
    VALUES (1), (2)
)
INSERT INTO lot (
    id_lot,
    status,
    quantite,
    caracteristique,
    date_sortie,
    date_stockage,
    date_production,
    id_exploitation,
    id_zone
)
SELECT
    uuid_generate_v4(),
    'COMPLIANT',
    (floor(random() * 5)::int + 1) * 100,
    floor(random() * 5 + 1)::int,
        NULL,
    CURRENT_TIMESTAMP - (random() * interval '10 months'),
    CURRENT_DATE - floor(random() * 330 + 1)::int,
        (SELECT id_exploitation FROM exploitation LIMIT 1),
    z.id_zone
FROM zone z
    CROSS JOIN lot_multiplier;