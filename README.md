# Franco — Sistema de gestión comercial

Aplicación monolítica construida con Java 21, Spring Boot 3.5 y Vaadin Flow. Permite administrar productos, categorías, clientes y ventas con control de stock, autenticación JWT y permisos por rol.

## Funcionalidades

- Dashboard con métricas de productos, ventas, ingresos, clientes y stock bajo.
- CRUD de productos, categorías y clientes.
- Registro de ventas con múltiples ítems y actualización atómica de stock.
- Punto de venta con lector de códigos de barras, promociones por cantidad, descuentos, medios de pago y cálculo de vuelto.
- Apertura y cierre de caja, ingresos, retiros y control de diferencias por turno.
- Proveedores y compras con reposición automática de stock.
- Historial de movimientos y ajustes manuales de inventario.
- Anulación trazable de ventas y compras con reversión de stock.
- Tickets imprimibles, reportes de rentabilidad, inventario valorizado y productos más vendidos.
- Gestión de usuarios con perfiles Cajero, Encargado y Dueño.
- Ficha de empleados con legajo, puesto, datos laborales y permisos operativos individuales.
- Reglas configurables por el dueño: caja obligatoria, venta sin stock, descuento máximo, fiado y límites de crédito.
- Cuenta corriente por cliente, historial de deuda, cobros y saldo a favor.
- Devoluciones parciales con reposición de stock y lotes, reintegro en caja o crédito al cliente.
- Registro de auditoría de operaciones sensibles.
- Lotes con consumo FEFO, alertas de vencimiento y bloqueo de mercadería vencida.
- Venta por peso con precisión de gramos y precios expresados por kilogramo.
- Configuración del comercio y tickets térmicos de 58/80 mm.
- Costo histórico por venta, exportación CSV y bloqueo temporal por intentos fallidos.
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

En producción también definí `SPRING_PROFILES_ACTIVE=prod`. Los usuarios y datos de demostración sólo se cargan con el perfil `dev`.

## Operación del kiosco

1. El cajero abre su turno desde **Caja** e indica el efectivo inicial.
2. Registra ventas desde **Punto de venta**, buscando o escaneando el código de barras.
3. El encargado registra entregas desde **Compras**; el stock se actualiza automáticamente.
4. Las diferencias físicas se registran desde **Inventario** y quedan auditadas.
5. Al finalizar el turno, el cajero cuenta el efectivo y cierra la caja.

El Dueño define el comportamiento general desde **Reglas del negocio** y asigna accesos desde **Empleados**. La personalización visual y las integraciones específicas de cada rubro quedan separadas de estas reglas operativas, por lo que la misma base puede adaptarse sin bifurcar la lógica central.

Los roles técnicos se conservan por compatibilidad: `USUARIO` representa al Cajero, `ADMIN` al Encargado y `SUPER_ADMIN` al Dueño.

Para productos vendidos por peso, el operador carga y vende kilogramos (hasta tres decimales). Internamente el stock se conserva en gramos para mantener exactitud y compatibilidad con el inventario existente.

## Backups

Con Docker Compose en ejecución:

```powershell
./scripts/backup.ps1
./scripts/restore.ps1 -BackupFile ./backups/franco-AAAAMMDD-HHMMSS.sql
```

La restauración exige confirmación explícita y sólo acepta archivos ubicados dentro del proyecto. En producción se recomienda programar `backup.ps1` diariamente y copiar los resultados a otro equipo o almacenamiento externo.

## Facturación fiscal

Los tickets actuales son comprobantes comerciales no fiscales. La emisión de comprobantes con CAE debe integrarse con ARCA utilizando el certificado, clave privada, CUIT y punto de venta reales del comercio; esos secretos no deben almacenarse en el repositorio.

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

La suite cubre ventas, stock, productos, caja, lotes, cuenta corriente, reglas operativas, validaciones de negocio y generación/validación JWT.
