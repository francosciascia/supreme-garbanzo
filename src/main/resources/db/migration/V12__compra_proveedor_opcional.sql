-- Proveedor opcional en compras (ingreso de stock sin proveedor cargado)
ALTER TABLE compras ALTER COLUMN proveedor_id DROP NOT NULL;
