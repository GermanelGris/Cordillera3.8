package com.cordillera.MS_kpi.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "kpis")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Kpi {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(name = "tipo_calculo", nullable = false, length = 20)
    private String tipoCalculo;

    @Column(nullable = false, precision = 15, scale = 4)
    private BigDecimal valor;

    @Column(nullable = false, length = 20)
    private String periodo;

    @Column(length = 30)
    private String unidad;

    @Column(length = 255)
    private String descripcion;

    @Column(name = "origen_ids", length = 500)
    private String origenIds;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (unidad == null) unidad = "CLP";
    }
}
