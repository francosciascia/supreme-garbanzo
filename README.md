📊 Sistema de Gestión Comercial

Proyecto desarrollado en Java + Spring Boot, con el objetivo de aprender Spring Boot, mejorar la calidad de código y trabajar con bases de datos.
Permite administrar productos, registrar ventas, controlar stock y calcular resultados básicos de la actividad comercial.

Actualmente lo estamos testeando con Postman.

📌 Funcionalidades

📦 Gestión de productos

Crear, editar, listar y obtener productos.

Validaciones de stock, costo y precio de venta.

🧾 Gestión de ventas

Crear ventas con múltiples ítems.

Cálculo automático de totales y subtotales.

Asociación de productos a ítems de venta.

✅ DTOs para separar entidades de la API (últimos cambios incorporados).

⚙️ Uso de Lombok para simplificar el código (getters, setters, constructores, builder).

🗄️ Persistencia con Spring Data JPA y base de datos relacional.

🛠️ Tecnologías

Java 17+

Spring Boot

Spring Data JPA

Hibernate

Lombok

Maven

Base de datos relacional (ej: MySQL, PostgreSQL o H2 para pruebas).

▶️ Cómo correr el proyecto

Cloná el repo:

git clone https://github.com/francosciascia/supreme-garbanzo.git
cd supreme-garbanzo


Compilá y corré con Maven/IntelliJ:

mvn spring-boot:run


La API quedará levantada en:

http://localhost:8080

📂 Endpoints principales
Productos

POST /api/productos → crear producto.

PUT /api/productos/{id} → editar producto.

GET /api/productos → listar todos.

GET /api/productos/{id} → obtener por ID.

Ventas

POST /api/ventas → crear venta (con lista de ítems).

GET /api/ventas/{id} → detalle de venta.

📑 Ejemplos de uso en Postman
🔹 Crear un producto

Request

POST /api/productos
Content-Type: application/json

{
  "nombre": "Pepsi 2L",
  "descripcion": "Gaseosa cola",
  "stock": 100,
  "vencimiento": false,
  "costo": 60.00,
  "precioVenta": 120.00
}


Response

{
  "id": 1,
  "nombre": "Pepsi 2L",
  "descripcion": "Gaseosa cola",
  "stock": 100,
  "vencimiento": false,
  "precioVenta": 120.00
}

🔹 Listar productos

Request

GET /api/productos


Response

[
  {
    "id": 1,
    "nombre": "Pepsi 2L",
    "descripcion": "Gaseosa cola",
    "stock": 100,
    "vencimiento": false,
    "precioVenta": 120.00
  },
  {
    "id": 2,
    "nombre": "Sprite 1.5L",
    "descripcion": "Gaseosa lima-limón",
    "stock": 50,
    "vencimiento": false,
    "precioVenta": 90.00
  }
]

🔹 Crear una venta

Request

POST /api/ventas
Content-Type: application/json

{
  "items": [
    { "productoId": 1, "cantidad": 2 },
    { "productoId": 2, "cantidad": 1 }
  ]
}


Response

{
  "id": 10,
  "fecha": "2025-09-08",
  "total": 330.00,
  "items": [
    {
      "productoId": 1,
      "nombreProducto": "Pepsi 2L",
      "cantidad": 2,
      "precioUnitario": 120.00,
      "subtotal": 240.00
    },
    {
      "productoId": 2,
      "nombreProducto": "Sprite 1.5L",
      "cantidad": 1,
      "precioUnitario": 90.00,
      "subtotal": 90.00
    }
  ]
}

🚀 Próximos pasos

Agregar seguridad (Spring Security, JWT).

Implementar reportes de ventas.

Crear interfaz front-end (React/Angular).

Optimizar consultas con @EntityGraph.

Continuar con pruebas en Postman.

Añadir un panel simple para ver ganancias y estadísticas básicas.

👨‍💻 Autor

Proyecto personal de Franco Sciascia, como práctica de backend con Java, Spring Boot y bases de datos relacionales, buscando mejorar la calidad de código y testeando con Postman.
