-- 1. Création de la table principale des utilisateurs
CREATE TABLE IF NOT EXISTS users (
                                     id UUID PRIMARY KEY,
                                     email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL
    );

-- 2. Création de la table des rôles (liée à l'ElementCollection JPA)
CREATE TABLE IF NOT EXISTS user_roles (
                                          user_id UUID NOT NULL,
                                          role VARCHAR(50) NOT NULL,
    CONSTRAINT fk_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
    );

-- 3. Insertion des utilisateurs de test (Mot de passe en clair : password123)
-- Génération de UUID fixes pour les lier proprement aux rôles ci-dessous

-- Utilisateur 1 : Headquarter
INSERT INTO users (id, email, password)
VALUES ('a1b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c6d', 'boss@siege.fr', 'password123')
    ON CONFLICT (email) DO NOTHING;

-- Utilisateur 2 : Warehouse Manager
INSERT INTO users (id, email, password)
VALUES ('b2c3d4e5-f67a-8b9c-0d1e-2f3a4b5c6d7e', 'entrepot@siege.fr', 'password123')
    ON CONFLICT (email) DO NOTHING;

-- Utilisateur 3 : Quality
INSERT INTO users (id, email, password)
VALUES ('c3d4e5f6-7a8b-9c0d-1e2f-3a4b5c6d7e8f', 'qualite@siege.fr', 'password123')
    ON CONFLICT (email) DO NOTHING;

-- Utilisateur 4 : Operation Manager
INSERT INTO users (id, email, password)
VALUES ('d4e5f67a-8b9c-0d1e-2f3a-4b5c6d7e8f9a', 'ops@siege.fr', 'password123')
    ON CONFLICT (email) DO NOTHING;

-- Utilisateur 5 : Admin
INSERT INTO users (id, email, password)
VALUES ('e5f67a8b-9c0d-1e2f-3a4b-5c6d7e8f9a0b', 'admin@siege.fr', 'password123')
    ON CONFLICT (email) DO NOTHING;


-- 4. Association des rôles (Sans le préfixe ROLE_ car UserDetailsServiceImpl l'ajoute dynamiquement)
INSERT INTO user_roles (user_id, role) VALUES
                                           ('a1b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c6d', 'HEADQUARTER'),
                                           ('b2c3d4e5-f67a-8b9c-0d1e-2f3a4b5c6d7e', 'WAREHOUSE_MANAGER'),
                                           ('c3d4e5f6-7a8b-9c0d-1e2f-3a4b5c6d7e8f', 'QUALITY'),
                                           ('d4e5f67a-8b9c-0d1e-2f3a-4b5c6d7e8f9a', 'OPERATION_MANAGER'),
                                           ('e5f67a8b-9c0d-1e2f-3a4b-5c6d7e8f9a0b', 'ADMIN')
    ON CONFLICT DO NOTHING;