package com.backendSiege.infrastructure.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@Table(name = "inventaire")
public class InventaireEntity {

    @Id
    @GeneratedValue
    @JdbcTypeCode(SqlTypes.UUID)
    @Column(name = "id_inventaire")
    private UUID id;

    @Column(name = "status", length = 20)
    private String status;

    @Column(name = "quantite")
    private Integer quantite;

    @Column(name = "caracteristique")
    private Integer caracteristique;

    @Column(name = "date_sortie")
    private LocalDateTime dateSortie;

    @Column(name = "date_stockage")
    private LocalDateTime dateStockage;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_entrepot", nullable = false)
    private EntrepotEntity entrepot;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_exploitation", nullable = false)
    private ExploitationEntity exploitation;
}