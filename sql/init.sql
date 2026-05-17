-- =============================================================================
-- CORDILLERA - Script de inicialización de bases de datos
-- =============================================================================

-- ─── DB_LOGIN ────────────────────────────────────────────────────────────────
CREATE DATABASE IF NOT EXISTS db_login CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE db_login;

CREATE TABLE IF NOT EXISTS usuarios (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    username    VARCHAR(50)  NOT NULL UNIQUE,
    password    VARCHAR(255) NOT NULL,
    email       VARCHAR(100) NOT NULL UNIQUE,
    rol         VARCHAR(20)  NOT NULL DEFAULT 'USUARIO',
    activo      BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Usuario administrador inicial (password: Admin123!)
INSERT IGNORE INTO usuarios (username, password, email, rol) VALUES
('admin', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'admin@cordillera.cl', 'ADMIN'),
('usuario1', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'usuario1@cordillera.cl', 'USUARIO');

-- ─── DB_DATA ─────────────────────────────────────────────────────────────────
CREATE DATABASE IF NOT EXISTS db_data CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE db_data;

CREATE TABLE IF NOT EXISTS datos_ingresados (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    fuente      VARCHAR(100)   NOT NULL COMMENT 'Sistema origen del dato',
    tipo        VARCHAR(50)    NOT NULL COMMENT 'VENTAS, PRODUCCION, COSTOS, INVENTARIO',
    valor       DECIMAL(15,4)  NOT NULL,
    periodo     VARCHAR(20)    NOT NULL COMMENT 'Formato YYYY-MM',
    descripcion VARCHAR(255),
    procesado   BOOLEAN        NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMP      DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_datos_periodo ON datos_ingresados(periodo);
CREATE INDEX IF NOT EXISTS idx_datos_tipo    ON datos_ingresados(tipo);
CREATE INDEX IF NOT EXISTS idx_datos_fuente  ON datos_ingresados(fuente);

INSERT IGNORE INTO datos_ingresados (fuente, tipo, valor, periodo, descripcion) VALUES
('Sistema ERP', 'VENTAS',     1500000.00, '2026-04', 'Ventas abril 2026'),
('Sistema ERP', 'VENTAS',     1750000.00, '2026-05', 'Ventas mayo 2026'),
('Sistema ERP', 'COSTOS',      800000.00, '2026-04', 'Costos operacionales abril'),
('Sistema ERP', 'COSTOS',      850000.00, '2026-05', 'Costos operacionales mayo'),
('Sistema ERP', 'PRODUCCION',    9500.00, '2026-04', 'Unidades producidas abril'),
('Sistema ERP', 'PRODUCCION',   10200.00, '2026-05', 'Unidades producidas mayo'),
('Bodega',      'INVENTARIO', 2300000.00, '2026-04', 'Valorización inventario abril'),
('Bodega',      'INVENTARIO', 2150000.00, '2026-05', 'Valorización inventario mayo');

-- ─── DB_KPI ──────────────────────────────────────────────────────────────────
CREATE DATABASE IF NOT EXISTS db_kpi CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE db_kpi;

CREATE TABLE IF NOT EXISTS kpis (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre      VARCHAR(100)  NOT NULL,
    tipo_calculo VARCHAR(20)  NOT NULL COMMENT 'PROMEDIO, SUMA, MAXIMO, MINIMO',
    valor       DECIMAL(15,4) NOT NULL,
    periodo     VARCHAR(20)   NOT NULL,
    unidad      VARCHAR(30)   DEFAULT 'CLP',
    descripcion VARCHAR(255),
    origen_ids  VARCHAR(500)  COMMENT 'IDs de datos de origen (JSON array)',
    created_at  TIMESTAMP     DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_kpi_periodo ON kpis(periodo);
CREATE INDEX IF NOT EXISTS idx_kpi_nombre  ON kpis(nombre);

INSERT IGNORE INTO kpis (nombre, tipo_calculo, valor, periodo, unidad, descripcion) VALUES
('Promedio Ventas Q1', 'PROMEDIO', 1625000.00, '2026-05', 'CLP', 'Promedio de ventas abril-mayo'),
('Total Costos',       'SUMA',     1650000.00, '2026-05', 'CLP', 'Suma total de costos'),
('Máximo Producción',  'MAXIMO',   10200.00,   '2026-05', 'UND', 'Máximo de unidades producidas');

-- ─── DB_REPORTES ─────────────────────────────────────────────────────────────
CREATE DATABASE IF NOT EXISTS db_reportes CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE db_reportes;

CREATE TABLE IF NOT EXISTS reportes (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    tipo         VARCHAR(50)    NOT NULL COMMENT 'KPI, MENSUAL, RESUMEN',
    titulo       VARCHAR(200)   NOT NULL,
    contenido    MEDIUMTEXT,
    periodo      VARCHAR(20)    NOT NULL,
    estado       VARCHAR(20)    NOT NULL DEFAULT 'GENERADO',
    generado_por VARCHAR(50),
    created_at   TIMESTAMP      DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_reporte_periodo ON reportes(periodo);
CREATE INDEX IF NOT EXISTS idx_reporte_tipo    ON reportes(tipo);

INSERT IGNORE INTO reportes (tipo, titulo, contenido, periodo, estado, generado_por) VALUES
('RESUMEN', 'Reporte Resumen Mayo 2026', 'Reporte de ejemplo generado automáticamente', '2026-05', 'GENERADO', 'sistema');
