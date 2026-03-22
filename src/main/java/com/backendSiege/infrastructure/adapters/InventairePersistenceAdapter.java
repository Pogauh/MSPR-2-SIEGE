package com.backendSiege.infrastructure.adapters;

import com.backendSiege.infrastructure.persistence.InventaireJpaRepository;
import com.backendSiege.infrastructure.entity.EntrepotEntity;
import com.backendSiege.infrastructure.entity.ExploitationEntity;
import com.backendSiege.infrastructure.entity.InventaireEntity;
import com.backendSiege.infrastructure.entity.PaysEntity;
import com.backendSiege.domain.model.Entrepot;
import com.backendSiege.domain.model.Exploitation;
import com.backendSiege.domain.model.Inventaire;
import com.backendSiege.domain.model.Pays;
import com.backendSiege.domain.port.InventairePort;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class InventairePersistenceAdapter implements InventairePort {

    private final InventaireJpaRepository inventaireJpaRepository;

    public InventairePersistenceAdapter(InventaireJpaRepository inventaireJpaRepository) {
        this.inventaireJpaRepository = inventaireJpaRepository;
    }

    @Override
    public List<Inventaire> findInventaireMondial() {
        return inventaireJpaRepository.findAll()
                .stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    private Inventaire toDomain(InventaireEntity entity) {
        EntrepotEntity entrepotEntity = entity.getEntrepot();
        ExploitationEntity exploitationEntity = entity.getExploitation();
        PaysEntity paysEntity = entity.getPays();

        Entrepot entrepot = new Entrepot(
                entrepotEntity.getId(),
                entrepotEntity.getLocalisation(),
                entrepotEntity.getNom()
        );

        Exploitation exploitation = new Exploitation(
                exploitationEntity.getId(),
                exploitationEntity.getNom(),
                exploitationEntity.getAdresse()
        );

        Pays pays = new Pays(
                paysEntity.getId(),
                paysEntity.getNom()
        );

        return new Inventaire(
                entity.getId(),
                entity.getStatus(),
                entity.getQuantite(),
                entity.getCaracteristique(),
                entity.getDateSortie(),
                entity.getDateStockage(),
                entrepot,
                exploitation,
                pays
        );
    }
}

