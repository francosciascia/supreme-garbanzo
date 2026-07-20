ALTER TABLE productos ADD COLUMN IF NOT EXISTS cantidad_minima_promo INTEGER CHECK (cantidad_minima_promo >= 2);
ALTER TABLE productos ADD COLUMN IF NOT EXISTS precio_promocional NUMERIC(14,2) CHECK (precio_promocional > 0);
