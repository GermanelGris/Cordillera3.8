-- =============================================================================
-- MS_DATOS — Microservicio de datos operacionales
-- Grupo Cordillera · MySQL 8.x · utf8mb4_unicode_ci
-- =============================================================================

CREATE DATABASE IF NOT EXISTS ms_datos
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE ms_datos;

-- -----------------------------------------------------------------------------
-- SUCURSALES
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS sucursales (
    id         INT           NOT NULL AUTO_INCREMENT,
    nombre     VARCHAR(100)  NOT NULL,
    ciudad     VARCHAR(80)   NOT NULL,
    region     VARCHAR(80)   NOT NULL,
    direccion  VARCHAR(200)  NOT NULL,
    activa     TINYINT(1)    NOT NULL DEFAULT 1,
    created_at DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_sucursales PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='Puntos de venta físicos y canales de la empresa';

-- -----------------------------------------------------------------------------
-- PRODUCTOS
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS productos (
    id          INT           NOT NULL AUTO_INCREMENT,
    sku         VARCHAR(50)   NOT NULL,
    nombre      VARCHAR(150)  NOT NULL,
    categoria   VARCHAR(80)   NOT NULL,
    precio_base DECIMAL(10,2) NOT NULL,
    activo      TINYINT(1)    NOT NULL DEFAULT 1,
    created_at  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_productos     PRIMARY KEY (id),
    CONSTRAINT uq_productos_sku UNIQUE      (sku)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='Catálogo maestro de productos';

-- -----------------------------------------------------------------------------
-- CLIENTES
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS clientes (
    id         INT           NOT NULL AUTO_INCREMENT,
    nombre     VARCHAR(120)  NOT NULL,
    email      VARCHAR(150)  NOT NULL,
    telefono   VARCHAR(20),
    ciudad     VARCHAR(80),
    created_at DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_clientes       PRIMARY KEY (id),
    CONSTRAINT uq_clientes_email UNIQUE      (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='Clientes registrados en la plataforma';

-- -----------------------------------------------------------------------------
-- INVENTARIO_RAW
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS inventario_raw (
    id           INT      NOT NULL AUTO_INCREMENT,
    producto_id  INT      NOT NULL,
    sucursal_id  INT      NOT NULL,
    stock_actual INT      NOT NULL DEFAULT 0  COMMENT 'Unidades disponibles en bodega',
    stock_minimo INT      NOT NULL DEFAULT 0  COMMENT 'Umbral de alerta de quiebre',
    updated_at   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
                          ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT pk_inventario_raw      PRIMARY KEY (id),
    CONSTRAINT uq_inventario_prod_suc UNIQUE      (producto_id, sucursal_id),
    CONSTRAINT fk_inventario_producto FOREIGN KEY (producto_id) REFERENCES productos(id),
    CONSTRAINT fk_inventario_sucursal FOREIGN KEY (sucursal_id) REFERENCES sucursales(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='Stock en tiempo real por producto y sucursal';

-- -----------------------------------------------------------------------------
-- VENTAS_RAW
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS ventas_raw (
    id          INT           NOT NULL AUTO_INCREMENT,
    sucursal_id INT           NOT NULL,
    cliente_id  INT                    DEFAULT NULL  COMMENT 'Nulo si venta anónima',
    fecha       DATETIME      NOT NULL,
    canal       VARCHAR(20)   NOT NULL DEFAULT 'TIENDA'
                              COMMENT 'TIENDA, ECOMMERCE, POS, MAYORISTA',
    total       DECIMAL(12,2) NOT NULL,
    estado      VARCHAR(20)   NOT NULL DEFAULT 'COMPLETADA'
                              COMMENT 'COMPLETADA, ANULADA, DEVUELTA',
    origen_ref  VARCHAR(80)            DEFAULT NULL,
    created_at  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_ventas_raw     PRIMARY KEY (id),
    CONSTRAINT fk_venta_sucursal FOREIGN KEY (sucursal_id) REFERENCES sucursales(id),
    CONSTRAINT fk_venta_cliente  FOREIGN KEY (cliente_id)  REFERENCES clientes(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='Cabecera de transacciones de venta';

-- -----------------------------------------------------------------------------
-- DETALLE_VENTA
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS detalle_venta (
    id              INT           NOT NULL AUTO_INCREMENT,
    venta_id        INT           NOT NULL,
    producto_id     INT           NOT NULL,
    cantidad        INT           NOT NULL,
    precio_unitario DECIMAL(10,2) NOT NULL,
    subtotal        DECIMAL(12,2) NOT NULL,
    CONSTRAINT pk_detalle_venta    PRIMARY KEY (id),
    CONSTRAINT fk_detalle_venta    FOREIGN KEY (venta_id)    REFERENCES ventas_raw(id) ON DELETE CASCADE,
    CONSTRAINT fk_detalle_producto FOREIGN KEY (producto_id) REFERENCES productos(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='Líneas de productos por transacción de venta';

-- -----------------------------------------------------------------------------
-- ÍNDICES
-- -----------------------------------------------------------------------------
CREATE INDEX idx_inventario_sucursal ON inventario_raw(sucursal_id);
CREATE INDEX idx_ventas_fecha        ON ventas_raw(fecha);
CREATE INDEX idx_ventas_sucursal     ON ventas_raw(sucursal_id);
CREATE INDEX idx_ventas_canal        ON ventas_raw(canal);
CREATE INDEX idx_ventas_estado       ON ventas_raw(estado);
CREATE INDEX idx_detalle_venta       ON detalle_venta(venta_id);
CREATE INDEX idx_detalle_producto    ON detalle_venta(producto_id);
CREATE INDEX idx_productos_categoria ON productos(categoria);

-- -----------------------------------------------------------------------------
-- DATOS DE MUESTRA
-- -----------------------------------------------------------------------------
INSERT IGNORE INTO sucursales (nombre, ciudad, region, direccion) VALUES
('Sucursal Santiago Centro', 'Santiago',    'Metropolitana', 'Av. Libertador Bernardo O\'Higgins 1234'),
('Sucursal Providencia',     'Providencia', 'Metropolitana', 'Av. Providencia 2450'),
('Sucursal Viña del Mar',    'Viña del Mar','Valparaíso',    'Av. Libertad 880'),
('Sucursal Concepción',      'Concepción',  'Biobío',        'Barros Arana 560');

INSERT IGNORE INTO productos (sku, nombre, categoria, precio_base) VALUES
('NB-001', 'Notebook Premium 15"', 'Tecnología',     899990.00),
('MN-002', 'Monitor UHD 27"',      'Tecnología',     349990.00),
('TL-003', 'Teclado Mecánico RGB', 'Accesorios',      89990.00),
('MS-004', 'Mouse Inalámbrico',    'Accesorios',      49990.00),
('TB-005', 'Tablet Pro 11"',       'Tecnología',     599990.00),
('AU-006', 'Auriculares BT Pro',   'Audio',          129990.00),
('WC-007', 'Webcam Full HD',       'Accesorios',      69990.00),
('SS-008', 'SSD Externo 1TB',      'Almacenamiento', 119990.00);

INSERT IGNORE INTO inventario_raw (producto_id, sucursal_id, stock_actual, stock_minimo) VALUES
(1,1,50,5),(1,2,30,5),(1,3,20,3),(1,4,15,3),
(2,1,30,5),(2,2,20,5),(2,3,10,3),(2,4, 8,3),
(3,1,100,10),(3,2,80,10),(3,3,60,8),(3,4,40,8),
(4,1,150,15),(4,2,120,15),(4,3,90,10),(4,4,70,10),
(5,1,40,5),(5,2,25,5),(5,3,18,3),(5,4,12,3),
(6,1,75,8),(6,2,60,8),(6,3,45,5),(6,4,35,5),
(7,1,60,8),(7,2,45,8),(7,3,30,5),(7,4,25,5),
(8,1,45,5),(8,2,35,5),(8,3,25,3),(8,4,20,3);
