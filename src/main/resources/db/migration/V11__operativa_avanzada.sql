-- Reglas operativas extra
ALTER TABLE reglas_operativas
    ADD COLUMN IF NOT EXISTS redondeo_efectivo BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN IF NOT EXISTS motivo_anulacion_obligatorio BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN IF NOT EXISTS margen_minimo_pct NUMERIC(5, 2) NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS imprimir_ticket_auto BOOLEAN NOT NULL DEFAULT TRUE;

-- Setup wizard + IVA en ticket
ALTER TABLE configuracion_comercio
    ADD COLUMN IF NOT EXISTS setup_completado BOOLEAN NOT NULL DEFAULT TRUE,
    ADD COLUMN IF NOT EXISTS mostrar_iva_ticket BOOLEAN NOT NULL DEFAULT FALSE;

-- IVA por producto (precios con IVA incluido)
ALTER TABLE productos
    ADD COLUMN IF NOT EXISTS alicuota_iva NUMERIC(5, 2) NOT NULL DEFAULT 21;

-- Motivo de anulación de venta
ALTER TABLE ventas
    ADD COLUMN IF NOT EXISTS motivo_anulacion VARCHAR(500);

-- Historial de costos
CREATE TABLE IF NOT EXISTS historial_costo_producto (
    id BIGSERIAL PRIMARY KEY,
    producto_id BIGINT NOT NULL REFERENCES productos(id),
    fecha TIMESTAMP NOT NULL DEFAULT NOW(),
    costo_anterior NUMERIC(14, 2),
    costo_nuevo NUMERIC(14, 2) NOT NULL,
    origen VARCHAR(40) NOT NULL,
    referencia VARCHAR(120),
    usuario_id BIGINT REFERENCES personas(id)
);

CREATE INDEX IF NOT EXISTS idx_historial_costo_producto ON historial_costo_producto(producto_id, fecha DESC);
