-- ==========================================================
-- TABLE PRINCIPALE DES UTILISATEURS
-- ==========================================================
CREATE TABLE IF NOT EXISTS users (
                                     id UUID PRIMARY KEY,
                                     email VARCHAR(255) NOT NULL UNIQUE,

    -- Mot de passe stocké sous forme BCrypt
    password VARCHAR(255) NOT NULL,

    -- Pays associé à l'utilisateur.
    -- NULL = accès global / pas limité à un pays
    country_id VARCHAR(255) NULL
    );


-- ==========================================================
-- TABLE DES ROLES
-- Un utilisateur peut avoir plusieurs rôles
-- Correspond à @ElementCollection dans UserEntity
-- ==========================================================
CREATE TABLE IF NOT EXISTS user_roles (
                                          user_id UUID NOT NULL,
                                          role VARCHAR(50) NOT NULL,

    -- Clé primaire composite requise pour le ON CONFLICT
    PRIMARY KEY (user_id, role),

    CONSTRAINT fk_user
    FOREIGN KEY (user_id)
    REFERENCES users(id)
    ON DELETE CASCADE
    );


-- ==========================================================
-- UTILISATEURS DE TEST
--
-- Mot de passe pour tous :
-- password123
--
-- Hash BCrypt de password123
-- country_id = NULL pour le moment
-- ==========================================================

INSERT INTO users (id, email, password, country_id)
VALUES
    (
        'a1b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c6d',
        'boss@siege.fr',
        '$2a$10$gxHepxcprQtRsiubAFktLudW6tlOdkWfN7a03OjMxog1oVpoVaIIG',
        NULL
    ),
    (
        'b2c3d4e5-f67a-8b9c-0d1e-2f3a4b5c6d7e',
        'entrepot@siege.fr',
        '$2a$10$gxHepxcprQtRsiubAFktLudW6tlOdkWfN7a03OjMxog1oVpoVaIIG',
        'colombie'
    ),
    (
        'c3d4e5f6-7a8b-9c0d-1e2f-3a4b5c6d7e8f',
        'qualite@siege.fr',
        '$2a$10$gxHepxcprQtRsiubAFktLudW6tlOdkWfN7a03OjMxog1oVpoVaIIG',
        'colombie'
    ),
    (
        'd4e5f67a-8b9c-0d1e-2f3a-4b5c6d7e8f9a',
        'ops@siege.fr',
        '$2a$10$gxHepxcprQtRsiubAFktLudW6tlOdkWfN7a03OjMxog1oVpoVaIIG',
        'colombie'
    ),
    (
        'e5f67a8b-9c0d-1e2f-3a4b-5c6d7e8f9a0b',
        'admin@siege.fr',
        '$2a$10$gxHepxcprQtRsiubAFktLudW6tlOdkWfN7a03OjMxog1oVpoVaIIG',
        NULL
    ),
    (
        'f6a7b8c9-0d1e-2f3a-4b5c-6d7e8f9a0b1c',
        'supply@siege.fr',
        '$2a$10$gxHepxcprQtRsiubAFktLudW6tlOdkWfN7a03OjMxog1oVpoVaIIG',
        'colombie'
    )
    ON CONFLICT (email) DO NOTHING;


-- ==========================================================
-- ASSOCIATION UTILISATEUR -> ROLE
--
-- Les noms doivent être identiques à Role.java
-- ==========================================================

INSERT INTO user_roles (user_id, role)
VALUES
    (
        'a1b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c6d',
        'ROLE_HEADQUARTER'
    ),
    (
        'b2c3d4e5-f67a-8b9c-0d1e-2f3a4b5c6d7e',
        'ROLE_WAREHOUSE_MANAGER'
    ),
    (
        'c3d4e5f6-7a8b-9c0d-1e2f-3a4b5c6d7e8f',
        'ROLE_QUALITY'
    ),
    (
        'd4e5f67a-8b9c-0d1e-2f3a-4b5c6d7e8f9a',
        'ROLE_OPERATION_MANAGER'
    ),
    (
        'e5f67a8b-9c0d-1e2f-3a4b-5c6d7e8f9a0b',
        'ROLE_ADMIN'
    ),
    (
        'f6a7b8c9-0d1e-2f3a-4b5c-6d7e8f9a0b1c',
        'ROLE_SUPPLY_CHAIN'
    )
    ON CONFLICT (user_id, role) DO NOTHING;