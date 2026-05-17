-- =============================================================================
-- DB_REPORTES - Base de datos del microservicio de reportes
-- =============================================================================
USE db_reportes;

CREATE TABLE IF NOT EXISTS reportes (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    tipo         VARCHAR(50)    NOT NULL,
    titulo       VARCHAR(200)   NOT NULL,
    contenido    MEDIUMTEXT,
    periodo      VARCHAR(20)    NOT NULL,
    estado       VARCHAR(20)    NOT NULL DEFAULT 'GENERADO',
    generado_por VARCHAR(50),
    created_at   TIMESTAMP      DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_reporte_periodo ON reportes(periodo);
CREATE INDEX idx_reporte_tipo    ON reportes(tipo);

INSERT IGNORE INTO reportes (tipo, titulo, contenido, periodo, estado, generado_por) VALUES
('RESUMEN', 'Reporte Resumen Mayo 2026', 'Resumen ejecutivo generado automáticamente.', '2026-05', 'GENERADO', 'sistema');
