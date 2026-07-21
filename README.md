# Supreme Garbanzo

Sistema de Gestión Comercial desarrollado con Java, Spring Boot, PostgreSQL y Vaadin.

## Características

- Gestión de productos
- Gestión de categorías
- Gestión de clientes
- Gestión de proveedores
- Gestión de ventas
- Control de stock
- Movimientos de inventario
- Reportes
- Autenticación JWT
- Roles y permisos
- Auditoría
- Paginación y filtros
- Validaciones
- Manejo global de excepciones

## Tecnologías

- Java 21
- Spring Boot
- Spring Security
- Spring Data JPA
- Hibernate
- PostgreSQL
- Flyway
- JWT
- BCrypt
- Vaadin
- Maven
- JUnit
- Mockito

## Arquitectura

Controller
↓
Service
↓
Repository
↓
PostgreSQL

## Capturas

_(agregar imágenes de la aplicación)_

## Instalación

```bash
git clone https://github.com/francosciascia/supreme-garbanzo.git
cd supreme-garbanzo
```

Configurar:

```
application-dev.properties
```

Luego:

```bash
./mvnw spring-boot:run
```

## Funcionalidades

- CRUD de productos
- Gestión de inventario
- Registro de ventas
- Actualización automática de stock
- Reportes
- Seguridad con JWT
- Roles
- Auditoría

## Testing

```bash
./mvnw test
```

## Roadmap

- Exportación PDF
- Dashboard
- Estadísticas avanzadas
- Notificaciones
