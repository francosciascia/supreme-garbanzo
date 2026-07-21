ALTER TABLE ventas ALTER COLUMN fecha TYPE TIMESTAMP USING fecha::timestamp;
INSERT INTO personas (id,nombre,apellido,edad,dni,direccion,fecha_inicio,email,contraseña,rol,activo,intentos_fallidos,bloqueado_hasta,ultimo_acceso)
SELECT id,nombre,apellido,edad,dni,direccion,fecha_inicio,email,contraseña,rol,activo,intentos_fallidos,bloqueado_hasta,ultimo_acceso FROM empleados ON CONFLICT DO NOTHING;
INSERT INTO perfiles_empleado (persona_id,puesto,fecha_contratacion,sueldo)
SELECT p.id,e.cargo,e.fecha_contratacion,e.salario FROM empleados e JOIN personas p ON p.email=e.email ON CONFLICT (persona_id) DO NOTHING;
SELECT setval('persona_sequence',GREATEST((SELECT COALESCE(MAX(id),1) FROM personas),1));
CREATE INDEX IF NOT EXISTS idx_ventas_fecha_estado ON ventas(fecha,estado);
