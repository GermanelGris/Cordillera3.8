-- =============================================================================
-- MS_LOGIN — Microservicio de autenticación y auditoría
-- Grupo Cordillera · MySQL 8.x · utf8mb4_unicode_ci
-- =============================================================================

CREATE DATABASE IF NOT EXISTS ms_login
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE ms_login;

-- -----------------------------------------------------------------------------
-- ROLES
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS roles (
    id          INT          NOT NULL AUTO_INCREMENT,
    nombre      VARCHAR(50)  NOT NULL,
    descripcion VARCHAR(255)          DEFAULT NULL,
    activo      TINYINT(1)   NOT NULL DEFAULT 1,
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_roles       PRIMARY KEY (id),
    CONSTRAINT uq_roles_nombre UNIQUE     (nombre)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='Roles del sistema con sus permisos asociados';

-- -----------------------------------------------------------------------------
-- USUARIOS
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS usuarios (
    id            BIGINT       NOT NULL AUTO_INCREMENT,
    nombre        VARCHAR(150) NOT NULL,
    email         VARCHAR(120) NOT NULL,
    password_hash VARCHAR(255) NOT NULL  COMMENT 'Hash BCrypt con cost ≥ 12',
    rol_id        INT          NOT NULL,
    activo        TINYINT(1)   NOT NULL DEFAULT 1,
    ultimo_login  DATETIME              DEFAULT NULL,
    created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP
                               ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT pk_login_usuarios       PRIMARY KEY (id),
    CONSTRAINT uq_login_usuarios_email UNIQUE      (email),
    CONSTRAINT fk_usuario_rol          FOREIGN KEY (rol_id) REFERENCES roles(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='Usuarios del sistema con credenciales de acceso';

-- -----------------------------------------------------------------------------
-- REFRESH_TOKENS
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS refresh_tokens (
    id         INT          NOT NULL AUTO_INCREMENT,
    usuario_id INT          NOT NULL,
    token      VARCHAR(500) NOT NULL,
    expira_en  DATETIME     NOT NULL,
    revocado   TINYINT(1)   NOT NULL DEFAULT 0,
    created_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_refresh_tokens   PRIMARY KEY (id),
    CONSTRAINT uq_refresh_token    UNIQUE      (token),
    CONSTRAINT fk_token_usuario    FOREIGN KEY (usuario_id)
        REFERENCES usuarios(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='Tokens de renovación de sesión JWT';

-- -----------------------------------------------------------------------------
-- LOGIN_AUDIT
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS login_audit (
    id         INT          NOT NULL AUTO_INCREMENT,
    email      VARCHAR(120) NOT NULL  COMMENT 'Email intentado, exista o no el usuario',
    exitoso    TINYINT(1)   NOT NULL,
    ip_origen  VARCHAR(45)           DEFAULT NULL COMMENT 'IPv4 o IPv6',
    detalle    VARCHAR(255)          DEFAULT NULL COMMENT 'Motivo de fallo o info adicional',
    created_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_login_audit PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='Auditoría de intentos de inicio de sesión';

-- -----------------------------------------------------------------------------
-- ÍNDICES
-- -----------------------------------------------------------------------------
CREATE INDEX idx_usuarios_rol         ON usuarios(rol_id);
CREATE INDEX idx_usuarios_activo      ON usuarios(activo);
CREATE INDEX idx_refresh_usuario      ON refresh_tokens(usuario_id);
CREATE INDEX idx_refresh_expira       ON refresh_tokens(expira_en);
CREATE INDEX idx_refresh_revocado     ON refresh_tokens(revocado);
CREATE INDEX idx_audit_email          ON login_audit(email);
CREATE INDEX idx_audit_fecha          ON login_audit(created_at);
CREATE INDEX idx_audit_exitoso        ON login_audit(exitoso);

-- -----------------------------------------------------------------------------
-- SEED — Roles del sistema
-- -----------------------------------------------------------------------------
INSERT IGNORE INTO roles (nombre, descripcion) VALUES
('ADMIN',   'Administrador con acceso total al sistema y configuración'),
('VENDEDOR','Operador de punto de venta y gestión de stock'),
('USUARIO', 'Cliente registrado con acceso a la tienda online');

-- -----------------------------------------------------------------------------
-- SEED — Usuario administrador inicial
--
-- IMPORTANTE: El password_hash de abajo es un placeholder.
-- Reemplázalo ejecutando en tu aplicación:
--   BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);
--   System.out.println(encoder.encode("TuPasswordSeguro!"));
-- O usando CLI: htpasswd -bnBC 12 "" TuPasswordSeguro! | tr -d ':\n'
-- El hash de ejemplo corresponde a: Admin123!
-- -----------------------------------------------------------------------------
-- BCrypt cost=10 de 'Admin123!'
INSERT IGNORE INTO usuarios (nombre, email, password_hash, rol_id)
SELECT 'Administrador del Sistema', 'admin@cordillera.cl',
       '$2b$10$b42F38Imlu3N56zVMILuIuUiXbuMJmiiLguPg0TIPwA3XQ0nhNgi2',
       id FROM roles WHERE nombre = 'ADMIN';

INSERT IGNORE INTO usuarios (nombre, email, password_hash, rol_id)
SELECT 'Vendedor POS', 'vendedor1@cordillera.cl',
       '$2b$10$b42F38Imlu3N56zVMILuIuUiXbuMJmiiLguPg0TIPwA3XQ0nhNgi2',
       id FROM roles WHERE nombre = 'VENDEDOR';

INSERT IGNORE INTO usuarios (nombre, email, password_hash, rol_id)
SELECT 'Cliente Demo', 'cliente1@cordillera.cl',
       '$2b$10$b42F38Imlu3N56zVMILuIuUiXbuMJmiiLguPg0TIPwA3XQ0nhNgi2',
       id FROM roles WHERE nombre = 'USUARIO';
