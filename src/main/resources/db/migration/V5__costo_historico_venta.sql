ALTER TABLE items_venta ADD COLUMN IF NOT EXISTS costo_unitario NUMERIC(14,2) NOT NULL DEFAULT 0;
UPDATE items_venta i SET costo_unitario = p.costo FROM productos p WHERE p.id = i.producto_id AND i.costo_unitario = 0;
