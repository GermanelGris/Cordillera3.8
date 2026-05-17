package com.cordillera.MS_data.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "datos_ingresados")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DatoIngresado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String fuente;

    @Column(nullable = false, length = 50)
    private String tipo;

    @Column(nullable = false, precision = 15, scale = 4)
    private BigDecimal valor;

    @Column(nullable = false, length = 20)
    private String periodo;

    @Column(length = 255)
    private String descripcion;

    @Column(nullable = false)
    private Boolean procesado;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (procesado == null) procesado = false;
    }
}
