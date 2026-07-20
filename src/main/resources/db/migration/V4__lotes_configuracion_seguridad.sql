ALTER TABLE items_venta ADD COLUMN IF NOT EXISTS unidad_venta VARCHAR(20) NOT NULL DEFAULT 'UNIDAD';

CREATE TABLE IF NOT EXISTS lotes_producto (
    id BIGSERIAL PRIMARY KEY,
    producto_id BIGINT NOT NULL REFERENCES productos(id),
    codigo_lote VARCHAR(80),
    fecha_ingreso DATE NOT NULL,
    fecha_vencimiento DATE,
    cantidad_inicial INTEGER NOT NULL CHECK (cantidad_inicial > 0),
    cantidad_disponible INTEGER NOT NULL CHECK (cantidad_disponible >= 0),
    costo_unitario NUMERIC(14,2) NOT NULL CHECK (costo_unitario >= 0),
    compra_id BIGINT REFERENCES compras(id),
    activo BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS items_venta_lotes (
    id BIGSERIAL PRIMARY KEY,
    item_venta_id BIGINT NOT NULL REFERENCES items_venta(id) ON DELETE CASCADE,
    lote_id BIGINT NOT NULL REFERENCES lotes_producto(id),
    cantidad INTEGER NOT NULL CHECK (cantidad > 0),
    UNIQUE(item_venta_id, lote_id)
);

CREATE TABLE IF NOT EXISTS configuracion_comercio (
    id BIGINT PRIMARY KEY CHECK (id = 1),
    nombre VARCHAR(150) NOT NULL,
    razon_social VARCHAR(180),
    cuit VARCHAR(20),
    direccion VARCHAR(255),
    telefono VARCHAR(80),
    mensaje_ticket VARCHAR(255),
    ancho_ticket INTEGER NOT NULL DEFAULT 80 CHECK (ancho_ticket IN (58, 80)),
    moneda VARCHAR(10) NOT NULL DEFAULT 'ARS',
    zona_horaria VARCHAR(60) NOT NULL DEFAULT 'America/Argentina/Buenos_Aires'
);

INSERT INTO configuracion_comercio (id, nombre, mensaje_ticket)
VALUES (1, 'Franco', 'Gracias por su compra') ON CONFLICT (id) DO NOTHING;

ALTER TABLE personas ADD COLUMN IF NOT EXISTS intentos_fallidos INTEGER NOT NULL DEFAULT 0;
ALTER TABLE personas ADD COLUMN IF NOT EXISTS bloqueado_hasta TIMESTAMP;
ALTER TABLE personas ADD COLUMN IF NOT EXISTS ultimo_acceso TIMESTAMP;

CREATE INDEX IF NOT EXISTS idx_lotes_producto_fefo ON lotes_producto(producto_id, fecha_vencimiento, fecha_ingreso)
    WHERE activo = TRUE AND cantidad_disponible > 0;
CREATE INDEX IF NOT EXISTS idx_lotes_vencimiento ON lotes_producto(fecha_vencimiento)
    WHERE activo = TRUE AND cantidad_disponible > 0;
