DROP TABLE IF EXISTS items_conteo;
DROP TABLE IF EXISTS conteos_inventario;

ALTER TABLE configuracion_comercio ADD COLUMN IF NOT EXISTS rubro VARCHAR(100);
ALTER TABLE configuracion_comercio ADD COLUMN IF NOT EXISTS slogan VARCHAR(180);
ALTER TABLE configuracion_comercio ADD COLUMN IF NOT EXISTS condicion_iva VARCHAR(80);
ALTER TABLE configuracion_comercio ADD COLUMN IF NOT EXISTS email VARCHAR(150);
ALTER TABLE configuracion_comercio ADD COLUMN IF NOT EXISTS whatsapp VARCHAR(80);
ALTER TABLE configuracion_comercio ADD COLUMN IF NOT EXISTS sitio_web VARCHAR(255);
ALTER TABLE configuracion_comercio ADD COLUMN IF NOT EXISTS logo_url VARCHAR(1000);
ALTER TABLE configuracion_comercio ADD COLUMN IF NOT EXISTS color_primario VARCHAR(7) NOT NULL DEFAULT '#2563EB';
ALTER TABLE configuracion_comercio ADD COLUMN IF NOT EXISTS color_secundario VARCHAR(7) NOT NULL DEFAULT '#0F172A';
ALTER TABLE configuracion_comercio ADD COLUMN IF NOT EXISTS encabezado_ticket VARCHAR(255);
ALTER TABLE configuracion_comercio ADD COLUMN IF NOT EXISTS mostrar_datos_fiscales_ticket BOOLEAN NOT NULL DEFAULT TRUE;

ALTER TABLE reglas_operativas ADD COLUMN IF NOT EXISTS requerir_cliente_venta BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE reglas_operativas ADD COLUMN IF NOT EXISTS medio_pago_predeterminado VARCHAR(30) NOT NULL DEFAULT 'EFECTIVO';
ALTER TABLE reglas_operativas ADD COLUMN IF NOT EXISTS devoluciones_habilitadas BOOLEAN NOT NULL DEFAULT TRUE;
ALTER TABLE reglas_operativas ADD COLUMN IF NOT EXISTS dias_maximos_devolucion INTEGER NOT NULL DEFAULT 30;
ALTER TABLE reglas_operativas ADD COLUMN IF NOT EXISTS controlar_vencimientos BOOLEAN NOT NULL DEFAULT TRUE;
ALTER TABLE reglas_operativas ADD COLUMN IF NOT EXISTS bloquear_venta_vencidos BOOLEAN NOT NULL DEFAULT TRUE;
