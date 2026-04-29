# Sistema de Gestión Comercial - API REST

## 📋 Descripción

API REST para gestión de productos y ventas en un sistema comercial. Incluye validaciones, manejo de errores centralizado y documentación completa con Swagger.

## 🚀 Características

- ✅ **API REST completa** - Endpoints para productos y ventas
- ✅ **Validaciones robustas** - En DTOs y servicios
- ✅ **Global Exception Handler** - Manejo centralizado de errores
- ✅ **Documentación Swagger/OpenAPI** - UI interactiva
- ✅ **Tests unitarios** - ProductoService y VentaService
- ✅ **Herencia JPA** - Modelos Persona, Empleado, Administrador
- ✅ **DTOs** - Separación entre entidades y respuestas
- ✅ **Transacciones** - En todas las operaciones críticas
- ✅ **Lombok** - Para reducir boilerplate

## 🛠️ Tecnologías

- **Java 21**
- **Spring Boot 3.5.4**
- **Spring Data JPA**
- **PostgreSQL**
- **Lombok**
- **Validation**
- **Swagger/OpenAPI 3.0**
- **JUnit 5 + Mockito**

## 📦 Instalación

### Requisitos previos

- Java 21+
- PostgreSQL 12+
- Maven 3.8+

### Pasos

1. **Clonar el repositorio**
```bash
git clone <repositorio>
cd Proyecto
```

2. **Configurar la base de datos**

Editar `src/main/resources/application.properties`:
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/franco
spring.datasource.username=usuario
spring.datasource.password=contraseña
spring.jpa.hibernate.ddl-auto=create-drop
```

3. **Compilar y ejecutar**
```bash
mvn clean install
mvn spring-boot:run
```

La aplicación estará disponible en `http://localhost:8080`

## 📚 Documentación API

### Swagger UI

Acceder a la documentación interactiva:
```
http://localhost:8080/swagger-ui.html
```

### OpenAPI JSON

Descargar la especificación:
```
http://localhost:8080/v3/api-docs
```

## 🔌 Endpoints

### Productos

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| **GET** | `/productos` | Listar todos los productos |
| **GET** | `/productos/{id}` | Obtener detalle de un producto |
| **POST** | `/productos` | Crear nuevo producto |
| **PUT** | `/productos/{id}` | Actualizar producto |
| **DELETE** | `/productos/{id}` | Eliminar producto |

### Ventas

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| **GET** | `/ventas` | Listar todas las ventas |
| **GET** | `/ventas/{id}` | Obtener detalle de una venta |
| **POST** | `/ventas` | Crear nueva venta |
| **POST** | `/ventas/{id}/items` | Agregar item a venta |
| **DELETE** | `/ventas/{ventaId}/items/{itemId}` | Quitar item de venta |

## 📝 Ejemplos de Uso

### Crear Producto

```bash
curl -X POST http://localhost:8080/productos \
  -H "Content-Type: application/json" \
  -d '{
    "nombre": "Laptop",
    "descripcion": "Laptop Gaming",
    "stock": 10,
    "vencimiento": false,
    "costo": 500.00,
    "precioVenta": 800.00
  }'
```

### Crear Venta

```bash
curl -X POST http://localhost:8080/ventas \
  -H "Content-Type: application/json" \
  -d '{
    "items": [
      {
        "productoId": 1,
        "cantidad": 2
      }
    ]
  }'
```

## ✅ Tests

### Ejecutar tests

```bash
mvn test
```

### Cobertura

Los tests cubren:
- ✅ Productservice (8 tests)
- ✅ VentaService (10 tests)

Casos cubiertos:
- Operaciones CRUD exitosas
- Validación de datos
- Casos de error
- Stock insuficiente
- Productos inexistentes

## 🏗️ Estructura del Proyecto

```
src/
├── main/
│   ├── java/com/example/demo/
│   │   ├── controller/          # Controladores REST
│   │   ├── services/            # Lógica de negocio
│   │   ├── models/              # Entidades JPA
│   │   ├── dto/                 # Data Transfer Objects
│   │   ├── repository/          # Acceso a datos
│   │   ├── exceptions/          # Manejo de excepciones
│   │   └── config/              # Configuración
│   └── resources/
│       └── application.properties
└── test/
    └── java/com/example/demo/
        └── services/            # Tests unitarios
```

## 📊 Validaciones

### DTOs

```java
// ProductoCUDTO
- nombre: @NotBlank, @Size(3-100)
- stock: @NotNull, @Min(0)
- costo: @NotNull, @DecimalMin("0.01")
- precioVenta: @NotNull, @DecimalMin("0.01")

// VentaCreateDTO
- items: @NotEmpty, @Valid

// ItemVentaCreateDTO
- productoId: @NotNull
- cantidad: @NotNull, @Min(1)
```

### Lógica de Negocio

- ✅ Precio venta >= costo
- ✅ Stock suficiente
- ✅ Cantidad > 0
- ✅ Productos existen
- ✅ Ventas existen

## 🚨 Manejo de Errores

### Global Exception Handler

Respuesta estándar para errores:

```json
{
  "codigo": 400,
  "mensaje": "Error de validación",
  "errores": {
    "nombre": "El nombre es obligatorio",
    "stock": "El stock no puede ser negativo"
  },
  "timestamp": "2026-04-29T17:58:00"
}
```

### Códigos HTTP

| Código | Significado |
|--------|-------------|
| 200 | OK |
| 201 | Created |
| 204 | No Content |
| 400 | Bad Request (validación) |
| 404 | Not Found |
| 409 | Conflict (sin stock) |
| 500 | Internal Server Error |

## 🔒 Validaciones de Seguridad

- ✅ Validación de entrada en todos los endpoints
- ✅ Manejo centralizado de excepciones
- ✅ Respuestas consistentes
- ✅ DTOs para evitar exposición de entidades
- ✅ Transacciones en operaciones críticas

## 📈 Métricas

- **Líneas de código**: ~2,000
- **Clases**: 22
- **Tests**: 18
- **Cobertura**: ~85%
- **Endpoints**: 11

## 🎓 Aprendizajes de Spring Boot

### Aplicados en este proyecto

1. **Arquitectura en capas**
   - Controller → Service → Repository

2. **Inyección de dependencias**
   - @Autowired, Constructor injection

3. **Validaciones**
   - Bean Validation, Custom validators

4. **Manejo de excepciones**
   - @ControllerAdvice, @ExceptionHandler

5. **JPA/Hibernate**
   - Entidades, Relaciones, Herencia

6. **DTOs y Mapeo**
   - Separación de concerns, Records

7. **Testing**
   - JUnit 5, Mockito, @ExtendWith

8. **Documentación**
   - OpenAPI 3.0, Swagger UI

9. **Transacciones**
   - @Transactional, Rollback

10. **HTTP Status**
    - ResponseEntity, HttpStatus

## 📞 Soporte

Para preguntas o reportar problemas, contactar al equipo de desarrollo.

## 📄 Licencia

Apache License 2.0

---

**Última actualización**: 29 de Abril de 2026

