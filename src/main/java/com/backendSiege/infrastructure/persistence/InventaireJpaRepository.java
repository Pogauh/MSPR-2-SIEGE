package com.backendSiege.infrastructure.persistence;

import com.backendSiege.infrastructure.entity.InventaireEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface InventaireJpaRepository extends JpaRepository<InventaireEntity, UUID> {
}

