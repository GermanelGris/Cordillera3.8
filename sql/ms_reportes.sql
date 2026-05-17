-- =============================================================================
-- MS_REPORTES — Microservicio de reportes ejecutivos y alertas
-- Grupo Cordillera · MySQL 8.x · utf8mb4_unicode_ci
-- =============================================================================

CREATE DATABASE IF NOT EXISTS ms_reportes
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE ms_reportes;

-- -----------------------------------------------------------------------------
-- USUARIOS  (referencia local — sin FK cross-servicio a ms_login)
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS usuarios (
    id            INT          NOT NULL AUTO_INCREMENT,
    nombre        VARCHAR(120) NOT NULL,
    email         VARCHAR(150) NOT NULL,
    rol           VARCHAR(20)  NOT NULL DEFAULT 'ANALISTA'
                  COMMENT 'ADMIN, GERENTE, ANALISTA',
    password_hash VARCHAR(255) NOT NULL,
    activo        TINYINT(1)   NOT NULL DEFAULT 1,
    created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_rep_usuarios       PRIMARY KEY (id),
    CONSTRAINT uq_rep_usuarios_email UNIQUE      (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='Copia local de usuarios con permisos de reportería';

-- -----------------------------------------------------------------------------
-- REPORTES
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS reportes (
    id          INT          NOT NULL AUTO_INCREMENT,
    titulo      VARCHAR(200) NOT NULL,
    tipo        VARCHAR(30)  NOT NULL COMMENT 'KPI, MENSUAL, TRIMESTRAL, RESUMEN',
    formato     VARCHAR(10)  NOT NULL DEFAULT 'PDF'
                COMMENT 'PDF, XLSX, JSON',
    usuario_id  INT          NOT NULL,
    fecha_gen   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    estado      VARCHAR(20)  NOT NULL DEFAULT 'PENDIENTE'
                COMMENT 'PENDIENTE, GENERANDO, GENERADO, ERROR',
    url_archivo VARCHAR(500)          DEFAULT NULL,
    CONSTRAINT pk_reportes         PRIMARY KEY (id),
    CONSTRAINT fk_reporte_usuario  FOREIGN KEY (usuario_id) REFERENCES usuarios(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='Reportes ejecutivos generados por los usuarios';

-- -----------------------------------------------------------------------------
-- REPORTE_PARAMS
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS reporte_params (
    id         INT          NOT NULL AUTO_INCREMENT,
    reporte_id INT          NOT NULL,
    clave      VARCHAR(80)  NOT NULL  COMMENT 'Ej: periodo, sucursal_id, formato',
    valor      VARCHAR(200) NOT NULL,
    CONSTRAINT pk_reporte_params   PRIMARY KEY (id),
    CONSTRAINT fk_params_reporte   FOREIGN KEY (reporte_id)
        REFERENCES reportes(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='Parámetros de configuración de cada reporte';

-- -----------------------------------------------------------------------------
-- ALERTAS
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS alertas (
    id         INT          NOT NULL AUTO_INCREMENT,
    tipo       VARCHAR(60)  NOT NULL  COMMENT 'QUIEBRE_STOCK, KPI_BAJO, VENTA_ANOMALA, etc.',
    mensaje    TEXT         NOT NULL,
    nivel      VARCHAR(10)  NOT NULL DEFAULT 'INFO'
               COMMENT 'INFO, WARN, ERROR, CRITICAL',
    usuario_id INT          NOT NULL,
    leida      TINYINT(1)   NOT NULL DEFAULT 0,
    created_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_alertas        PRIMARY KEY (id),
    CONSTRAINT fk_alerta_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios(id),
    CONSTRAINT chk_alerta_nivel  CHECK (nivel IN ('INFO','WARN','ERROR','CRITICAL'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='Alertas operacionales dirigidas a usuarios';

-- -----------------------------------------------------------------------------
-- ÍNDICES
-- -----------------------------------------------------------------------------
CREATE INDEX idx_reportes_usuario ON reportes(usuario_id);
CREATE INDEX idx_reportes_estado  ON reportes(estado);
CREATE INDEX idx_reportes_tipo    ON reportes(tipo);
CREATE INDEX idx_reportes_fecha   ON reportes(fecha_gen);
CREATE INDEX idx_params_reporte   ON reporte_params(reporte_id);
CREATE INDEX idx_alertas_usuario  ON alertas(usuario_id);
CREATE INDEX idx_alertas_leida    ON alertas(leida);
CREATE INDEX idx_alertas_nivel    ON alertas(nivel);
CREATE INDEX idx_alertas_fecha    ON alertas(created_at);

-- -----------------------------------------------------------------------------
-- DATOS DE MUESTRA
-- -----------------------------------------------------------------------------
INSERT IGNORE INTO usuarios (nombre, email, rol, password_hash) VALUES
('Administrador',  'admin@cordillera.cl',    'ADMIN',    '$2a$12$placeholder_reemplazar'),
('Gerente General','gerente@cordillera.cl',  'GERENTE',  '$2a$12$placeholder_reemplazar'),
('Analista BI',    'analista@cordillera.cl', 'ANALISTA', '$2a$12$placeholder_reemplazar');

INSERT IGNORE INTO reportes (titulo, tipo, formato, usuario_id, estado, url_archivo) VALUES
('Reporte KPI Mayo 2026',         'KPI',      'PDF',  1, 'GENERADO', '/reportes/kpi-mayo-2026.pdf'),
('Resumen Mensual Mayo 2026',     'MENSUAL',  'PDF',  1, 'GENERADO', '/reportes/mensual-mayo-2026.pdf'),
('Análisis Trimestral Q1 2026',   'TRIMESTRAL','XLSX',2, 'GENERADO', '/reportes/trimestral-q1-2026.xlsx');

INSERT IGNORE INTO reporte_params (reporte_id, clave, valor) VALUES
(1, 'periodo',      '2026-05'),
(1, 'sucursal_id',  '0'),
(1, 'incluir_graf', 'true'),
(2, 'periodo',      '2026-05'),
(2, 'sucursal_id',  '0'),
(3, 'fecha_inicio', '2026-01-01'),
(3, 'fecha_fin',    '2026-03-31');

INSERT IGNORE INTO alertas (tipo, mensaje, nivel, usuario_id, leida) VALUES
('QUIEBRE_STOCK',  'Notebook Premium 15" con stock crítico en Sucursal Concepción (3 unidades)', 'WARN',     1, 0),
('KPI_BAJO',       'Ticket promedio de Sucursal Viña del Mar cayó 8% respecto al mes anterior',  'WARN',     2, 0),
('VENTA_ANOMALA',  'Volumen de ventas inusualmente alto en canal ECOMMERCE el 2026-05-10',       'INFO',     1, 1),
('SOBRESTOCK',     'Monitor UHD 27" con sobrestock en Sucursal Santiago Centro (95 unidades)',   'INFO',     3, 0);
