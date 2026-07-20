CREATE SEQUENCE IF NOT EXISTS persona_sequence START WITH 1 INCREMENT BY 1;

CREATE TABLE IF NOT EXISTS categorias (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(255) NOT NULL UNIQUE,
    descripcion TEXT
);

CREATE TABLE IF NOT EXISTS productos (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(255) NOT NULL,
    descripcion VARCHAR(255),
    stock INTEGER NOT NULL CHECK (stock >= 0),
    vencimiento BOOLEAN NOT NULL,
    costo NUMERIC(14,2) NOT NULL CHECK (costo >= 0),
    precio_venta NUMERIC(14,2) NOT NULL CHECK (precio_venta >= 0),
    categoria_id BIGINT REFERENCES categorias(id)
);

CREATE TABLE IF NOT EXISTS clientes (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(255) NOT NULL,
    apellido VARCHAR(255) NOT NULL,
    dni INTEGER NOT NULL UNIQUE,
    email VARCHAR(255) UNIQUE,
    telefono VARCHAR(255),
    direccion VARCHAR(255),
    fecha_registro DATE NOT NULL,
    activo BOOLEAN NOT NULL
);

CREATE TABLE IF NOT EXISTS ventas (
    id BIGSERIAL PRIMARY KEY,
    fecha DATE NOT NULL,
    total NUMERIC(14,2) NOT NULL,
    cliente_id BIGINT REFERENCES clientes(id)
);

CREATE TABLE IF NOT EXISTS items_venta (
    id BIGSERIAL PRIMARY KEY,
    venta_id BIGINT NOT NULL REFERENCES ventas(id) ON DELETE CASCADE,
    producto_id BIGINT NOT NULL REFERENCES productos(id),
    cantidad INTEGER NOT NULL CHECK (cantidad >= 1),
    precio_unitario NUMERIC(14,2) NOT NULL
);

CREATE TABLE IF NOT EXISTS personas (
    id BIGINT PRIMARY KEY DEFAULT nextval('persona_sequence'),
    nombre VARCHAR(255) NOT NULL,
    apellido VARCHAR(255) NOT NULL,
    edad INTEGER NOT NULL,
    dni INTEGER NOT NULL UNIQUE,
    direccion VARCHAR(255),
    fecha_inicio DATE,
    email VARCHAR(255) NOT NULL UNIQUE,
    contraseña VARCHAR(255) NOT NULL,
    rol VARCHAR(32) NOT NULL,
    activo BOOLEAN NOT NULL
);

CREATE TABLE IF NOT EXISTS administradores (
    id BIGINT PRIMARY KEY DEFAULT nextval('persona_sequence'),
    nombre VARCHAR(255) NOT NULL, apellido VARCHAR(255) NOT NULL, edad INTEGER NOT NULL,
    dni INTEGER NOT NULL UNIQUE, direccion VARCHAR(255), fecha_inicio DATE,
    email VARCHAR(255) NOT NULL UNIQUE, contraseña VARCHAR(255) NOT NULL, rol VARCHAR(32) NOT NULL,
    activo BOOLEAN NOT NULL, nivel_acceso VARCHAR(32) NOT NULL, departamento VARCHAR(255) NOT NULL,
    permisos_especiales VARCHAR(255), fecha_nombramiento DATE
);

CREATE TABLE IF NOT EXISTS empleados (
    id BIGINT PRIMARY KEY DEFAULT nextval('persona_sequence'),
    nombre VARCHAR(255) NOT NULL, apellido VARCHAR(255) NOT NULL, edad INTEGER NOT NULL,
    dni INTEGER NOT NULL UNIQUE, direccion VARCHAR(255), fecha_inicio DATE,
    email VARCHAR(255) NOT NULL UNIQUE, contraseña VARCHAR(255) NOT NULL, rol VARCHAR(32) NOT NULL,
    activo BOOLEAN NOT NULL, salario NUMERIC(10,2) NOT NULL, cargo VARCHAR(255) NOT NULL,
    fecha_contratacion DATE, horas_trabajo_semanal INTEGER NOT NULL, departamento VARCHAR(255) NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_productos_categoria ON productos(categoria_id);
CREATE INDEX IF NOT EXISTS idx_ventas_fecha ON ventas(fecha);
CREATE INDEX IF NOT EXISTS idx_ventas_cliente ON ventas(cliente_id);
CREATE INDEX IF NOT EXISTS idx_items_venta_venta ON items_venta(venta_id);
