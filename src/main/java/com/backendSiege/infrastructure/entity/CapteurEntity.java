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

import java.util.UUID;

@Data
@Entity
@Table(name = "capteur")
public class CapteurEntity {

    @Id
    @GeneratedValue
    @JdbcTypeCode(SqlTypes.UUID)
    @Column(name = "id_capteur")
    private UUID id;

    @Column(name = "humi_avg", nullable = false)
    private Integer humiAvg;

    @Column(name = "nom", length = 50)
    private String nom;

    @Column(name = "emplacement_precis", length = 150)
    private String emplacementPrecis;

    @Column(name = "temp_avg", length = 50)
    private String tempAvg;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_entrepot", nullable = false)
    private EntrepotEntity entrepot;
}

