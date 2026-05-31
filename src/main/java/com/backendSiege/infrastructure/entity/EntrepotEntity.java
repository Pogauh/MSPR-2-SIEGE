package com.backendSiege.infrastructure.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.UUID;

@Data
@Entity
@Table(name = "entrepot")
public class EntrepotEntity {

    @Id
    @GeneratedValue
    @JdbcTypeCode(SqlTypes.UUID)
    @Column(name = "id_entrepot")
    private UUID id;

    @Column(name = "localisation", length = 150)
    private String localisation;

    @Column(name = "nom", length = 50)
    private String nom;

    @Column(name = "pays", length = 50)
    private String pays;
}

