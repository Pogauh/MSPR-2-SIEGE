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
@Table(name = "mesure")
public class MesureEntity {

    @Id
    @GeneratedValue
    @JdbcTypeCode(SqlTypes.UUID)
    @Column(name = "id_mesure")
    private UUID id;

    @Column(name = "temperature")
    private Integer temperature;

    @Column(name = "humidite")
    private Integer humidite;

    @Column(name = "date_mesure")
    private LocalDateTime dateMesure;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_capteur", nullable = false)
    private CapteurEntity capteur;
}

