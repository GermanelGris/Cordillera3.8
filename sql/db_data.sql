-- =============================================================================
-- DB_DATA - Base de datos del microservicio de datos
-- =============================================================================
USE db_data;

CREATE TABLE IF NOT EXISTS datos_ingresados (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    fuente      VARCHAR(100)   NOT NULL,
    tipo        VARCHAR(50)    NOT NULL,
    valor       DECIMAL(15,4)  NOT NULL,
    periodo     VARCHAR(20)    NOT NULL,
    descripcion VARCHAR(255),
    procesado   BOOLEAN        NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMP      DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_datos_periodo ON datos_ingresados(periodo);
CREATE INDEX idx_datos_tipo    ON datos_ingresados(tipo);
CREATE INDEX idx_datos_fuente  ON datos_ingresados(fuente);

CREATE TABLE IF NOT EXISTS inventario (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    producto_id INT           NOT NULL UNIQUE,
    nombre      VARCHAR(200)  NOT NULL,
    stock       INT           NOT NULL DEFAULT 0,
    updated_at  TIMESTAMP     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

INSERT IGNORE INTO inventario (producto_id, nombre, stock) VALUES
(1, 'Notebook Premium 15"', 50),
(2, 'Monitor UHD 27"',      30),
(3, 'Teclado Mecánico',    100),
(4, 'Mouse Inalámbrico',   150),
(5, 'Tablet Pro 11"',       40),
(6, 'Auriculares BT Pro',   75),
(7, 'Webcam Full HD',       60),
(8, 'SSD Externo 1TB',      45),
(9, 'Hub USB-C 7 en 1',     80);

INSERT IGNORE INTO datos_ingresados (fuente, tipo, valor, periodo, descripcion) VALUES
('Sistema ERP', 'VENTAS',      1500000.00, '2026-04', 'Ventas abril 2026'),
('Sistema ERP', 'VENTAS',      1750000.00, '2026-05', 'Ventas mayo 2026'),
('Sistema ERP', 'COSTOS',       800000.00, '2026-04', 'Costos operacionales abril'),
('Sistema ERP', 'COSTOS',       850000.00, '2026-05', 'Costos operacionales mayo'),
('Sistema ERP', 'PRODUCCION',     9500.00, '2026-04', 'Unidades producidas abril'),
('Sistema ERP', 'PRODUCCION',    10200.00, '2026-05', 'Unidades producidas mayo'),
('Bodega',      'INVENTARIO',  2300000.00, '2026-04', 'Valorización inventario abril'),
('Bodega',      'INVENTARIO',  2150000.00, '2026-05', 'Valorización inventario mayo');
