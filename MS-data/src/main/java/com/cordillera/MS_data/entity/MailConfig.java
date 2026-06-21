package com.cordillera.MS_data.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Configuración SMTP editable por el administrador.
 * Se persiste como una única fila (id = 1).
 */
@Entity
@Table(name = "mail_config")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MailConfig {

    @Id
    private Long id;

    @Column(nullable = false, length = 150)
    private String host;

    @Column(nullable = false)
    private Integer port;

    @Column(length = 200)
    private String username;

    @Column(length = 300)
    private String password;

    /** Dirección que aparece como remitente. */
    @Column(name = "from_address", length = 200)
    private String fromAddress;

    private Boolean auth;

    private Boolean starttls;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
