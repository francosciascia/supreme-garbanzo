# Franco — Sistema de gestión comercial

Aplicación monolítica construida con Java 21, Spring Boot 3.5 y Vaadin Flow. Permite administrar productos, categorías, clientes y ventas con control de stock, autenticación JWT y permisos por rol.

## Funcionalidades

- Dashboard con métricas de productos, ventas, ingresos, clientes y stock bajo.
- CRUD de productos, categorías y clientes.
- Registro de ventas con múltiples ítems y actualización atómica de stock.
- Filtros, ordenamiento y paginación en Vaadin.
- Endpoints paginados para productos y ventas.
- Roles `USUARIO`, `ADMIN` y `SUPER_ADMIN`.
- Respuestas de error uniformes y validación global.
- Migraciones de base de datos con Flyway.
- Entorno reproducible con Docker Compose.

## Estructura

```text
src/main/java/com/example/demo/
├── config/       Configuración y datos iniciales
├── controller/   API REST
├── dto/          Contratos de entrada y salida
├── exceptions/   Excepciones y manejo global
├── mapper/       Conversión entidad/DTO
├── models/       Entidades JPA
├── repository/   Repositorios Spring Data
├── security/     JWT, filtros y autorización
├── services/     Casos de uso y transacciones
└── ui/           Vistas Vaadin Flow

src/main/resources/
├── application.properties
└── db/migration/ Migraciones Flyway
```

## Ejecución local

Requisitos: Java 21, PostgreSQL y Docker opcional.

La configuración predeterminada usa PostgreSQL en `localhost:5432`, base `postgres`, usuario `postgres` y contraseña `1234`. Puede reemplazarse con variables de entorno:

```powershell
$env:DB_URL = "jdbc:postgresql://localhost:5432/postgres"
$env:DB_USERNAME = "postgres"
$env:DB_PASSWORD = "1234"
$env:JWT_SECRET = "una-clave-segura-de-al-menos-32-caracteres"
./mvnw.cmd spring-boot:run
```

Aplicación: <http://localhost:8083>

Swagger: <http://localhost:8083/swagger-ui.html>

## Docker Compose

```bash
docker compose up --build
```

Esto inicia PostgreSQL y la aplicación. Para producción, definí `JWT_SECRET` fuera del repositorio.

## Seguridad

El login se realiza mediante `POST /api/auth/login`. Para endpoints protegidos, enviar:

```http
Authorization: Bearer <token>
```

Permisos principales:

| Recurso | Lectura | Crear/editar | Eliminar |
|---|---|---|---|
| Productos y categorías | Público | ADMIN, SUPER_ADMIN | SUPER_ADMIN |
| Clientes | Autenticado | ADMIN, SUPER_ADMIN | SUPER_ADMIN |
| Ventas | Autenticado | Autenticado | SUPER_ADMIN |

## Paginación y filtros

```text
GET /api/productos/page?search=leche&categoriaId=3&page=0&size=20&sort=nombre,asc
GET /api/ventas/page?clienteId=1&desde=2026-01-01&hasta=2026-12-31&page=0&size=20
```

Los endpoints históricos sin paginación se mantienen por compatibilidad.

## Base de datos

Flyway administra el esquema desde `V1__initial_schema.sql`. Hibernate usa `ddl-auto=validate`, por lo que detecta diferencias sin modificar tablas automáticamente. `baseline-on-migrate` permite incorporar una base preexistente.

## Pruebas

```powershell
./mvnw.cmd test
```

La suite cubre ventas, stock, productos, validaciones de negocio y generación/validación JWT.
