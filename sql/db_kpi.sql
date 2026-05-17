-- =============================================================================
-- DB_KPI - Base de datos del microservicio de KPIs
-- =============================================================================
USE db_kpi;

CREATE TABLE IF NOT EXISTS kpis (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre       VARCHAR(100)  NOT NULL,
    tipo_calculo VARCHAR(20)   NOT NULL,
    valor        DECIMAL(15,4) NOT NULL,
    periodo      VARCHAR(20)   NOT NULL,
    unidad       VARCHAR(30)   DEFAULT 'CLP',
    descripcion  VARCHAR(255),
    origen_ids   VARCHAR(500),
    created_at   TIMESTAMP     DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_kpi_periodo ON kpis(periodo);
CREATE INDEX idx_kpi_nombre  ON kpis(nombre);

INSERT IGNORE INTO kpis (nombre, tipo_calculo, valor, periodo, unidad, descripcion) VALUES
('Promedio Ventas',    'PROMEDIO', 1625000.00, '2026-05', 'CLP', 'Promedio ventas abril-mayo'),
('Total Costos',       'SUMA',     1650000.00, '2026-05', 'CLP', 'Suma total costos'),
('Máximo Producción',  'MAXIMO',   10200.00,   '2026-05', 'UND', 'Máximo unidades producidas');
