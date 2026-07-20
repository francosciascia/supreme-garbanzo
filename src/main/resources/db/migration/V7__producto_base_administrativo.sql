CREATE TABLE IF NOT EXISTS perfiles_empleado (
    id BIGSERIAL PRIMARY KEY,
    persona_id BIGINT NOT NULL UNIQUE REFERENCES personas(id) ON DELETE CASCADE,
    legajo VARCHAR(40) UNIQUE,
    puesto VARCHAR(100),
    fecha_contratacion DATE,
    sueldo NUMERIC(14,2),
    telefono_emergencia VARCHAR(80),
    observaciones VARCHAR(500)
);

CREATE TABLE IF NOT EXISTS permisos_usuario (
    persona_id BIGINT NOT NULL REFERENCES personas(id) ON DELETE CASCADE,
    permiso VARCHAR(80) NOT NULL,
    PRIMARY KEY (persona_id, permiso)
);

CREATE TABLE IF NOT EXISTS reglas_operativas (
    id BIGINT PRIMARY KEY CHECK (id = 1),
    caja_obligatoria BOOLEAN NOT NULL DEFAULT TRUE,
    permitir_venta_sin_stock BOOLEAN NOT NULL DEFAULT FALSE,
    descuento_maximo NUMERIC(5,2) NOT NULL DEFAULT 20 CHECK (descuento_maximo BETWEEN 0 AND 100),
    fiado_habilitado BOOLEAN NOT NULL DEFAULT FALSE,
    limite_credito_predeterminado NUMERIC(14,2) NOT NULL DEFAULT 0,
    anulacion_solo_dueno BOOLEAN NOT NULL DEFAULT TRUE,
    dias_alerta_vencimiento INTEGER NOT NULL DEFAULT 30 CHECK (dias_alerta_vencimiento >= 0),
    permitir_precio_manual BOOLEAN NOT NULL DEFAULT FALSE
);
INSERT INTO reglas_operativas (id) VALUES (1) ON CONFLICT (id) DO NOTHING;

ALTER TABLE clientes ADD COLUMN IF NOT EXISTS saldo_cuenta NUMERIC(14,2) NOT NULL DEFAULT 0;
ALTER TABLE clientes ADD COLUMN IF NOT EXISTS limite_credito NUMERIC(14,2) NOT NULL DEFAULT 0;
ALTER TABLE ventas ADD COLUMN IF NOT EXISTS monto_fiado NUMERIC(14,2) NOT NULL DEFAULT 0;

CREATE TABLE IF NOT EXISTS movimientos_cuenta (
    id BIGSERIAL PRIMARY KEY,
    cliente_id BIGINT NOT NULL REFERENCES clientes(id),
    fecha TIMESTAMP NOT NULL,
    tipo VARCHAR(30) NOT NULL,
    monto NUMERIC(14,2) NOT NULL CHECK (monto > 0),
    saldo_resultante NUMERIC(14,2) NOT NULL,
    descripcion VARCHAR(255),
    venta_id BIGINT REFERENCES ventas(id),
    usuario_id BIGINT REFERENCES personas(id)
);

CREATE TABLE IF NOT EXISTS devoluciones (
    id BIGSERIAL PRIMARY KEY,
    venta_id BIGINT NOT NULL REFERENCES ventas(id),
    fecha TIMESTAMP NOT NULL,
    motivo VARCHAR(255) NOT NULL,
    total NUMERIC(14,2) NOT NULL,
    forma_reintegro VARCHAR(30) NOT NULL,
    usuario_id BIGINT REFERENCES personas(id)
);
CREATE TABLE IF NOT EXISTS items_devolucion (
    id BIGSERIAL PRIMARY KEY,
    devolucion_id BIGINT NOT NULL REFERENCES devoluciones(id) ON DELETE CASCADE,
    item_venta_id BIGINT NOT NULL REFERENCES items_venta(id),
    cantidad INTEGER NOT NULL CHECK (cantidad > 0),
    subtotal NUMERIC(14,2) NOT NULL
);

CREATE TABLE IF NOT EXISTS conteos_inventario (
    id BIGSERIAL PRIMARY KEY,
    fecha_inicio TIMESTAMP NOT NULL,
    fecha_cierre TIMESTAMP,
    estado VARCHAR(20) NOT NULL,
    usuario_id BIGINT REFERENCES personas(id),
    observaciones VARCHAR(500)
);
CREATE TABLE IF NOT EXISTS items_conteo (
    id BIGSERIAL PRIMARY KEY,
    conteo_id BIGINT NOT NULL REFERENCES conteos_inventario(id) ON DELETE CASCADE,
    producto_id BIGINT NOT NULL REFERENCES productos(id),
    stock_sistema INTEGER NOT NULL,
    stock_contado INTEGER,
    diferencia INTEGER,
    UNIQUE(conteo_id, producto_id)
);

CREATE TABLE IF NOT EXISTS auditoria (
    id BIGSERIAL PRIMARY KEY,
    fecha TIMESTAMP NOT NULL,
    usuario_id BIGINT REFERENCES personas(id),
    accion VARCHAR(80) NOT NULL,
    entidad VARCHAR(80) NOT NULL,
    entidad_id VARCHAR(80),
    detalle VARCHAR(1000),
    ip VARCHAR(64)
);

CREATE INDEX IF NOT EXISTS idx_movimientos_cuenta_cliente_fecha ON movimientos_cuenta(cliente_id, fecha);
CREATE INDEX IF NOT EXISTS idx_devoluciones_venta ON devoluciones(venta_id);
CREATE INDEX IF NOT EXISTS idx_auditoria_fecha ON auditoria(fecha);
