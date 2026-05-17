-- =============================================================================
-- MS_KPI — Microservicio de indicadores clave de rendimiento
-- Grupo Cordillera · MySQL 8.x · utf8mb4_unicode_ci
-- =============================================================================

CREATE DATABASE IF NOT EXISTS ms_kpi
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE ms_kpi;

-- -----------------------------------------------------------------------------
-- PERIODOS
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS periodos (
    id           INT         NOT NULL AUTO_INCREMENT,
    tipo         VARCHAR(20) NOT NULL COMMENT 'MENSUAL, TRIMESTRAL, SEMESTRAL, ANUAL',
    fecha_inicio DATE        NOT NULL,
    fecha_fin    DATE        NOT NULL,
    created_at   DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_periodos PRIMARY KEY (id),
    CONSTRAINT chk_periodos_fechas CHECK (fecha_fin >= fecha_inicio)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='Ventanas temporales para el cálculo de KPIs';

-- -----------------------------------------------------------------------------
-- KPI_VENTAS
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS kpi_ventas (
    id                INT           NOT NULL AUTO_INCREMENT,
    periodo_id        INT           NOT NULL,
    sucursal_ref      INT           NOT NULL  COMMENT 'ID de sucursal en ms_datos (sin FK cross-BD)',
    total_ventas      DECIMAL(14,2) NOT NULL DEFAULT 0.00,
    num_transacciones INT           NOT NULL DEFAULT 0,
    ticket_promedio   DECIMAL(10,2) NOT NULL DEFAULT 0.00
                      COMMENT 'total_ventas / num_transacciones',
    variacion_pct     DECIMAL(6,2)           DEFAULT NULL
                      COMMENT 'Variación % respecto al período anterior',
    calculado_at      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP
                      ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT pk_kpi_ventas          PRIMARY KEY (id),
    CONSTRAINT fk_kpi_ventas_periodo  FOREIGN KEY (periodo_id) REFERENCES periodos(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='KPIs de ventas agregados por período y sucursal';

-- -----------------------------------------------------------------------------
-- KPI_INVENTARIO
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS kpi_inventario (
    id            INT          NOT NULL AUTO_INCREMENT,
    periodo_id    INT          NOT NULL,
    sucursal_ref  INT          NOT NULL  COMMENT 'ID de sucursal en ms_datos (sin FK cross-BD)',
    rotacion      DECIMAL(8,4) NOT NULL DEFAULT 0.0000
                  COMMENT 'Veces que rota el inventario en el período',
    quiebre_stock INT          NOT NULL DEFAULT 0
                  COMMENT 'Productos con stock_actual < stock_minimo',
    sobrestock    INT          NOT NULL DEFAULT 0
                  COMMENT 'Productos con stock_actual > 3 × stock_minimo',
    calculado_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP
                  ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT pk_kpi_inventario         PRIMARY KEY (id),
    CONSTRAINT fk_kpi_inventario_periodo FOREIGN KEY (periodo_id) REFERENCES periodos(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='KPIs de inventario por período y sucursal';

-- -----------------------------------------------------------------------------
-- KPI_RENTABILIDAD
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS kpi_rentabilidad (
    id            INT           NOT NULL AUTO_INCREMENT,
    periodo_id    INT           NOT NULL,
    sucursal_ref  INT           NOT NULL  COMMENT 'ID de sucursal en ms_datos (sin FK cross-BD)',
    margen_bruto  DECIMAL(6,2)  NOT NULL DEFAULT 0.00
                  COMMENT 'Porcentaje: (ventas - costo) / ventas × 100',
    costo_total   DECIMAL(14,2) NOT NULL DEFAULT 0.00,
    utilidad_neta DECIMAL(14,2) NOT NULL DEFAULT 0.00
                  COMMENT 'total_ventas - costo_total - gastos_operativos',
    calculado_at  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP
                  ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT pk_kpi_rentabilidad         PRIMARY KEY (id),
    CONSTRAINT fk_kpi_rentabilidad_periodo FOREIGN KEY (periodo_id) REFERENCES periodos(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='KPIs de rentabilidad por período y sucursal';

-- -----------------------------------------------------------------------------
-- ÍNDICES
-- -----------------------------------------------------------------------------
CREATE INDEX idx_kpi_ventas_periodo       ON kpi_ventas(periodo_id);
CREATE INDEX idx_kpi_ventas_sucursal      ON kpi_ventas(sucursal_ref);
CREATE INDEX idx_kpi_inventario_periodo   ON kpi_inventario(periodo_id);
CREATE INDEX idx_kpi_inventario_sucursal  ON kpi_inventario(sucursal_ref);
CREATE INDEX idx_kpi_rentab_periodo       ON kpi_rentabilidad(periodo_id);
CREATE INDEX idx_kpi_rentab_sucursal      ON kpi_rentabilidad(sucursal_ref);
CREATE INDEX idx_periodos_tipo            ON periodos(tipo);
CREATE INDEX idx_periodos_rango           ON periodos(fecha_inicio, fecha_fin);

-- -----------------------------------------------------------------------------
-- DATOS DE MUESTRA
-- -----------------------------------------------------------------------------
INSERT IGNORE INTO periodos (tipo, fecha_inicio, fecha_fin) VALUES
('MENSUAL',     '2026-04-01', '2026-04-30'),
('MENSUAL',     '2026-05-01', '2026-05-31'),
('TRIMESTRAL',  '2026-01-01', '2026-03-31'),
('SEMESTRAL',   '2026-01-01', '2026-06-30');

INSERT IGNORE INTO kpi_ventas (periodo_id, sucursal_ref, total_ventas, num_transacciones, ticket_promedio, variacion_pct) VALUES
(1, 1, 1500000.00, 94,  15957.45,  NULL),
(2, 1, 1750000.00, 102, 17156.86,  16.67),
(1, 2,  980000.00, 65,  15076.92,  NULL),
(2, 2, 1120000.00, 71,  15774.65,  14.29),
(1, 3,  730000.00, 50,  14600.00,  NULL),
(2, 3,  860000.00, 57,  15087.72,  17.81);

INSERT IGNORE INTO kpi_inventario (periodo_id, sucursal_ref, rotacion, quiebre_stock, sobrestock) VALUES
(1, 1, 4.2500, 2, 1),
(2, 1, 4.8000, 1, 0),
(1, 2, 3.9100, 3, 2),
(2, 2, 4.1200, 2, 1);

INSERT IGNORE INTO kpi_rentabilidad (periodo_id, sucursal_ref, margen_bruto, costo_total, utilidad_neta) VALUES
(1, 1, 38.50,  923000.00, 577000.00),
(2, 1, 40.20, 1045500.00, 704500.00),
(1, 2, 36.80,  619160.00, 360840.00),
(2, 2, 37.50,  700000.00, 420000.00);
