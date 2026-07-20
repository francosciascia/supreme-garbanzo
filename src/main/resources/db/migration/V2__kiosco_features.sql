ALTER TABLE productos ADD COLUMN IF NOT EXISTS codigo_barras VARCHAR(64);
ALTER TABLE productos ADD COLUMN IF NOT EXISTS marca VARCHAR(120);
ALTER TABLE productos ADD COLUMN IF NOT EXISTS stock_minimo INTEGER NOT NULL DEFAULT 5 CHECK (stock_minimo >= 0);
ALTER TABLE productos ADD COLUMN IF NOT EXISTS unidad_venta VARCHAR(20) NOT NULL DEFAULT 'UNIDAD';
ALTER TABLE productos ADD COLUMN IF NOT EXISTS fecha_vencimiento DATE;
CREATE UNIQUE INDEX IF NOT EXISTS uk_productos_codigo_barras ON productos(codigo_barras) WHERE codigo_barras IS NOT NULL;

CREATE TABLE IF NOT EXISTS proveedores (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(150) NOT NULL,
    cuit VARCHAR(20) UNIQUE,
    telefono VARCHAR(60),
    email VARCHAR(255),
    direccion VARCHAR(255),
    activo BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS cajas (
    id BIGSERIAL PRIMARY KEY,
    usuario_id BIGINT NOT NULL REFERENCES personas(id),
    fecha_apertura TIMESTAMP NOT NULL,
    fecha_cierre TIMESTAMP,
    monto_inicial NUMERIC(14,2) NOT NULL CHECK (monto_inicial >= 0),
    monto_final_esperado NUMERIC(14,2),
    monto_final_real NUMERIC(14,2),
    diferencia NUMERIC(14,2),
    estado VARCHAR(20) NOT NULL
);

CREATE TABLE IF NOT EXISTS movimientos_caja (
    id BIGSERIAL PRIMARY KEY,
    caja_id BIGINT NOT NULL REFERENCES cajas(id),
    fecha TIMESTAMP NOT NULL,
    tipo VARCHAR(30) NOT NULL,
    monto NUMERIC(14,2) NOT NULL CHECK (monto > 0),
    descripcion VARCHAR(255),
    venta_id BIGINT REFERENCES ventas(id)
);

ALTER TABLE ventas ADD COLUMN IF NOT EXISTS estado VARCHAR(20) NOT NULL DEFAULT 'CONFIRMADA';
ALTER TABLE ventas ADD COLUMN IF NOT EXISTS medio_pago VARCHAR(30) NOT NULL DEFAULT 'EFECTIVO';
ALTER TABLE ventas ADD COLUMN IF NOT EXISTS monto_recibido NUMERIC(14,2);
ALTER TABLE ventas ADD COLUMN IF NOT EXISTS vuelto NUMERIC(14,2) NOT NULL DEFAULT 0;
ALTER TABLE ventas ADD COLUMN IF NOT EXISTS descuento NUMERIC(14,2) NOT NULL DEFAULT 0;
ALTER TABLE ventas ADD COLUMN IF NOT EXISTS numero_comprobante VARCHAR(40);
ALTER TABLE ventas ADD COLUMN IF NOT EXISTS caja_id BIGINT REFERENCES cajas(id);
ALTER TABLE ventas ADD COLUMN IF NOT EXISTS usuario_id BIGINT REFERENCES personas(id);
CREATE UNIQUE INDEX IF NOT EXISTS uk_ventas_numero_comprobante ON ventas(numero_comprobante) WHERE numero_comprobante IS NOT NULL;

CREATE TABLE IF NOT EXISTS compras (
    id BIGSERIAL PRIMARY KEY,
    proveedor_id BIGINT NOT NULL REFERENCES proveedores(id),
    fecha TIMESTAMP NOT NULL,
    numero_comprobante VARCHAR(60),
    total NUMERIC(14,2) NOT NULL CHECK (total >= 0),
    estado VARCHAR(20) NOT NULL,
    usuario_id BIGINT REFERENCES personas(id)
);

CREATE TABLE IF NOT EXISTS items_compra (
    id BIGSERIAL PRIMARY KEY,
    compra_id BIGINT NOT NULL REFERENCES compras(id) ON DELETE CASCADE,
    producto_id BIGINT NOT NULL REFERENCES productos(id),
    cantidad INTEGER NOT NULL CHECK (cantidad > 0),
    costo_unitario NUMERIC(14,2) NOT NULL CHECK (costo_unitario >= 0),
    fecha_vencimiento DATE
);

CREATE TABLE IF NOT EXISTS movimientos_stock (
    id BIGSERIAL PRIMARY KEY,
    producto_id BIGINT NOT NULL REFERENCES productos(id),
    fecha TIMESTAMP NOT NULL,
    tipo VARCHAR(30) NOT NULL,
    cantidad INTEGER NOT NULL,
    stock_anterior INTEGER NOT NULL,
    stock_nuevo INTEGER NOT NULL,
    referencia VARCHAR(80),
    descripcion VARCHAR(255),
    usuario_id BIGINT REFERENCES personas(id)
);

CREATE INDEX IF NOT EXISTS idx_cajas_usuario_estado ON cajas(usuario_id, estado);
CREATE INDEX IF NOT EXISTS idx_movimientos_caja_caja ON movimientos_caja(caja_id);
CREATE INDEX IF NOT EXISTS idx_compras_proveedor ON compras(proveedor_id);
CREATE INDEX IF NOT EXISTS idx_movimientos_stock_producto_fecha ON movimientos_stock(producto_id, fecha);
