CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE pays (
                      id_pays UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                      nom VARCHAR(50) NOT NULL,
                      temp_ideale DECIMAL(5,2),
                      tolerance_temp DECIMAL(3,2),
                      humi_ideale DECIMAL(5,2),
                      tolerance_humi DECIMAL(3,2),
                      email_responsable VARCHAR(100)
);

CREATE TABLE exploitation (
                              id_exploitation UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                              nom VARCHAR(50) NOT NULL,
                              adresse VARCHAR(150),
                              id_pays UUID REFERENCES pays(id_pays)
);

CREATE TABLE entrepot (
                          id_entrepot UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                          nom VARCHAR(50) NOT NULL,
                          localisation VARCHAR(150),
                          id_pays UUID REFERENCES pays(id_pays)
);

CREATE TABLE lot (
                     id_lot UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                     status VARCHAR(20) DEFAULT 'CONFORME',
                     quantite INT,
                     caracteristique INT,
                     date_stockage TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                     date_sortie TIMESTAMP,
                     emplacement VARCHAR(150),
                     id_exploitation UUID REFERENCES exploitation(id_exploitation),
                     id_entrepot UUID REFERENCES entrepot(id_entrepot)
);

CREATE TABLE capteur (
                         id_capteur UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                         code_serie VARCHAR(50) UNIQUE NOT NULL,
                         nom VARCHAR(50),
                         emplacement_precis VARCHAR(150),
                         id_entrepot UUID NOT NULL REFERENCES entrepot(id_entrepot) ON DELETE CASCADE
);

CREATE TABLE mesure (
                        id_mesure UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                        temperature DECIMAL(5,2),
                        humidite DECIMAL(5,2),
                        date_mesure TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        id_capteur UUID NOT NULL REFERENCES capteur(id_capteur) ON DELETE CASCADE);

CREATE TABLE alerte (
                        id_alerte UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                        type_alerte VARCHAR(50),
                        description TEXT,
                        date_emission TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE genere_alerte (
                               id_lot UUID REFERENCES lot(id_lot),
                               id_alerte UUID REFERENCES alerte(id_alerte),
                               PRIMARY KEY (id_lot, id_alerte)
);

INSERT INTO pays (id_pays, nom, temp_ideale, tolerance_temp, humi_ideale, tolerance_humi, email_responsable)
VALUES ('550e8400-e29b-41d4-a716-446655440000', 'Brésil', 29.0, 3.0, 55.0, 2.0, 'resp.bresil@futurekawa.com');

INSERT INTO exploitation (id_exploitation, nom, adresse, id_pays)
VALUES ('aa0e8400-e29b-41d4-a716-446655440022', 'Fazenda Santa Maria', 'Minas Gerais, Brésil', '550e8400-e29b-41d4-a716-446655440000');

INSERT INTO entrepot (id_entrepot, nom, localisation, id_pays)
VALUES ('770e8400-e29b-41d4-a716-446655440011', 'Entrepôt Principal Santos', 'Port de Santos, Brésil', '550e8400-e29b-41d4-a716-446655440000');

-- Seed de capteurs uniquement si la table est vide
WITH capteur_seed (idx, emplacement) AS (
    VALUES
        (1, 'A'), (2, 'B'), (3, 'C'), (4, 'D'), (5, 'E'), (6, 'F'), (7, 'G'), (8, 'H'),
        (9, 'I'), (10, 'J'), (11, 'K'), (12, 'L'), (13, 'M'), (14, 'N'), (15, 'O'), (16, 'P'),
        (17, 'Q'), (18, 'R'), (19, 'S'), (20, 'T'), (21, 'U'), (22, 'V'), (23, 'W'), (24, 'X'),
        (25, 'Y'), (26, 'Z'), (27, 'AA'), (28, 'AB'), (29, 'AC'), (30, 'AD')
)
INSERT INTO capteur (id_capteur, code_serie, nom, emplacement_precis, id_entrepot)
SELECT
    uuid_generate_v4(),
    format('02:AA:BB:CC:DD:%s', lpad(upper(to_hex(idx)), 2, '0')),
    'TEMP_HUMI',
    emplacement,
    '770e8400-e29b-41d4-a716-446655440011'
FROM capteur_seed
WHERE NOT EXISTS (SELECT 1 FROM capteur);
