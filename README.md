# Supreme Garbanzo

Sistema integral de gestión comercial desarrollado con **Java 21, Spring Boot, PostgreSQL y Vaadin**.

La aplicación permite administrar productos, categorías, clientes, proveedores, compras, ventas, inventario, usuarios y reportes desde una interfaz web moderna, implementando autenticación JWT, control de roles y auditoría.

---

# Demo

🌐 **Aplicación online**

https://supreme-garbanzo-1.onrender.com

> **Importante:** la aplicación está desplegada en el plan gratuito de Render. Si estuvo algunos minutos sin uso, el primer acceso puede tardar entre 30 y 60 segundos mientras el servidor se inicia.

---

# Usuarios de demostración

Contraseña para todas las cuentas:

```text
123456
```

| Rol | Usuario |
|------|----------|
| Cajero | usuario@demo.com |
| Encargado | admin@demo.com |
| Dueño | superadmin@demo.com |

---

# Funcionalidades

- Gestión de productos
- Gestión de categorías
- Gestión de clientes
- Gestión de proveedores
- Gestión de compras
- Gestión de ventas
- Gestión de cajas
- Gestión de lotes
- Gestión de inventario
- Control automático de stock
- Devoluciones
- Reportes comerciales
- Auditoría
- Gestión de empleados
- Gestión de usuarios
- Reglas de negocio
- Paginación
- Ordenamiento
- Búsquedas
- Filtros dinámicos
- Validaciones
- Manejo global de excepciones
- Inicio de sesión seguro
- Autenticación JWT
- Roles y permisos
- Contraseñas encriptadas con BCrypt
- Bloqueo por intentos fallidos

---

# Tecnologías utilizadas

## Backend

- Java 21
- Spring Boot
- Spring Security
- Spring Data JPA
- Hibernate
- PostgreSQL
- Flyway
- JWT
- BCrypt

## Frontend

- Vaadin

## Testing

- JUnit
- Mockito

## DevOps

- Maven
- Docker
- Render
- Neon PostgreSQL
- Git
- GitHub

---

# Arquitectura

```
                 Vaadin
                    │
                    ▼
            REST Controllers
                    │
                    ▼
                Services
                    │
                    ▼
             Spring Data JPA
                    │
                    ▼
               PostgreSQL
```

El proyecto está organizado siguiendo una arquitectura en capas:

- Views
- Controllers
- Services
- Repositories
- Models
- DTOs
- Security
- Exceptions

---

# Seguridad

La aplicación implementa:

- Login mediante correo electrónico
- Autenticación con JWT
- Roles:

  - USUARIO
  - ADMIN
  - SUPER_ADMIN

- Contraseñas protegidas con BCrypt
- Usuarios activos/inactivos
- Bloqueo por intentos fallidos
- Manejo personalizado de errores HTTP
- Auditoría de acciones

---

# Base de datos

Se utiliza PostgreSQL.

La estructura es administrada mediante **Flyway**, por lo que las migraciones se ejecutan automáticamente al iniciar la aplicación.

Las migraciones se encuentran en:

```
src/main/resources/db/migration
```

---

# Instalación

## Clonar el repositorio

```bash
git clone https://github.com/francosciascia/supreme-garbanzo.git

cd supreme-garbanzo
```

---

## Configurar PostgreSQL

Crear o modificar el archivo:

```
application-dev.properties
```

O utilizar variables de entorno:

```properties
DB_URL=jdbc:postgresql://localhost:5432/postgres
DB_USERNAME=postgres
DB_PASSWORD=1234
```

---

## Ejecutar la aplicación

Windows

```powershell
.\mvnw.cmd spring-boot:run
```

Linux / Mac

```bash
./mvnw spring-boot:run
```

Luego ingresar a

```
http://localhost:8083
```

---

# Variables de entorno

En producción se utilizan:

```properties
PORT

DB_URL

DB_USERNAME

DB_PASSWORD

JWT_SECRET

JWT_EXPIRATION

HIBERNATE_SQL_LOG

HIBERNATE_BIND_LOG

JPA_SHOW_SQL
```

---

# Compilar

```bash
./mvnw clean package -DskipTests
```

El archivo generado queda en

```
target/
```

---

# Ejecutar Tests

```bash
./mvnw test
```

---

# Deploy

La aplicación se encuentra desplegada utilizando:

```
GitHub
      │
      ▼
 Render
      │
      ▼
Spring Boot + Vaadin
      │
      ▼
 Neon PostgreSQL
```

Cada vez que se realiza un:

```bash
git push
```

Render detecta automáticamente los cambios, recompila el proyecto y publica la nueva versión sin necesidad de realizar ninguna acción adicional.

---

# Capturas

Agregar imágenes de la aplicación.

Ejemplo:

```markdown
![Login](docs/images/login.png)

![Productos](docs/images/productos.png)

![Ventas](docs/images/ventas.png)

![Clientes](docs/images/clientes.png)

![Dashboard](docs/images/dashboard.png)
```

---

# Roadmap

- Exportación PDF
- Exportación Excel
- Dashboard comercial
- Estadísticas avanzadas
- Gráficos de ventas
- Notificaciones
- Mejor cobertura de tests
- Optimización de consultas
- Documentación completa de la API

---

# Autor

**Franco Oscar Sciascia**

GitHub

https://github.com/francosciascia

---

# Licencia

Proyecto desarrollado con fines educativos y como portfolio personal.
