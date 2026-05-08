package com.example.demo.config;

import com.example.demo.models.Persona;
import com.example.demo.models.Producto;
import com.example.demo.models.Venta;
import com.example.demo.models.ItemVenta;
import com.example.demo.models.Categoria;
import com.example.demo.repository.PersonaRepository;
import com.example.demo.repository.ProductoRepository;
import com.example.demo.repository.VentaRepository;
import com.example.demo.repository.CategoriaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private PersonaRepository personaRepository;

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private VentaRepository ventaRepository;

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // Crear categorías de demo si no existen
        if (categoriaRepository.count() == 0) {
            crearCategoriasDemo();
        }

        // Crear usuarios de demo si no existen
        if (personaRepository.count() == 0) {
            crearUsuariosDemo();
        }

        // Crear productos de demo si no existen
        if (productoRepository.count() == 0) {
            crearProductosDemo();
        }

        // Crear ventas de demo si no existen
        if (ventaRepository.count() == 0) {
            crearVentasDemo();
        }
    }

    private void crearCategoriasDemo() {
        Categoria electronica = new Categoria("Electrónica", "Productos electrónicos y computadoras");
        Categoria limpieza = new Categoria("Limpieza", "Productos de limpieza y desinfección");
        Categoria lacteos = new Categoria("Lácteos", "Productos lácteos frescos");
        Categoria alimentos = new Categoria("Alimentos", "Alimentos secos y enlatados");
        Categoria higiene = new Categoria("Higiene Personal", "Productos de higiene personal");

        categoriaRepository.saveAll(Arrays.asList(electronica, limpieza, lacteos, alimentos, higiene));
        System.out.println("✅ Categorías de demostración creadas exitosamente");
    }

    private void crearUsuariosDemo() {
        // Usuario Común
        Persona usuario = Persona.builder()
                .email("usuario@demo.com")
                .contraseña(passwordEncoder.encode("123456"))
                .nombre("Juan")
                .apellido("Pérez")
                .edad(25)
                .dni(12345678)
                .direccion("Calle 1, Apto 101")
                .rol(Persona.Rol.USUARIO)
                .activo(true)
                .build();

        // Admin
        Persona admin = Persona.builder()
                .email("admin@demo.com")
                .contraseña(passwordEncoder.encode("123456"))
                .nombre("Carlos")
                .apellido("García")
                .edad(35)
                .dni(87654321)
                .direccion("Avenida Principal, Piso 5")
                .rol(Persona.Rol.ADMIN)
                .activo(true)
                .build();

        // Super Admin
        Persona superAdmin = Persona.builder()
                .email("superadmin@demo.com")
                .contraseña(passwordEncoder.encode("123456"))
                .nombre("Franco")
                .apellido("Sciascia")
                .edad(30)
                .dni(11111111)
                .direccion("Boulevard Central, Torre A")
                .rol(Persona.Rol.SUPER_ADMIN)
                .activo(true)
                .build();

        personaRepository.saveAll(Arrays.asList(usuario, admin, superAdmin));
        System.out.println("✅ Usuarios de demostración creados exitosamente");
    }

    private void crearProductosDemo() {
        // Obtener categorías
        Categoria electronica = categoriaRepository.findByNombre("Electrónica").orElse(null);
        Categoria limpieza = categoriaRepository.findByNombre("Limpieza").orElse(null);
        Categoria lacteos = categoriaRepository.findByNombre("Lácteos").orElse(null);

        Producto producto1 = Producto.builder()
                .nombre("Laptop Dell Inspiron")
                .descripcion("Laptop de 15.6 pulgadas con procesador Intel Core i5")
                .stock(10)
                .vencimiento(false)
                .costo(new java.math.BigDecimal("700.00"))
                .precioVenta(new java.math.BigDecimal("850.00"))
                .categoria(electronica)
                .build();

        Producto producto2 = Producto.builder()
                .nombre("Mouse Logitech MX Master")
                .descripcion("Mouse inalámbrico ergonómico con batería de larga duración")
                .stock(25)
                .vencimiento(false)
                .costo(new java.math.BigDecimal("30.00"))
                .precioVenta(new java.math.BigDecimal("45.99"))
                .categoria(electronica)
                .build();

        Producto producto3 = Producto.builder()
                .nombre("Teclado Mecánico RGB")
                .descripcion("Teclado mecánico con switches Cherry MX y iluminación RGB")
                .stock(15)
                .vencimiento(false)
                .costo(new java.math.BigDecimal("80.00"))
                .precioVenta(new java.math.BigDecimal("120.50"))
                .categoria(electronica)
                .build();

        Producto producto4 = Producto.builder()
                .nombre("Monitor 27\" 4K")
                .descripcion("Monitor Ultra HD de 27 pulgadas con resolución 4K")
                .stock(8)
                .vencimiento(false)
                .costo(new java.math.BigDecimal("250.00"))
                .precioVenta(new java.math.BigDecimal("350.00"))
                .categoria(electronica)
                .build();

        Producto producto5 = Producto.builder()
                .nombre("Auriculares Sony WH-1000XM4")
                .descripcion("Auriculares inalámbricos con cancelación de ruido activa")
                .stock(12)
                .vencimiento(false)
                .costo(new java.math.BigDecimal("200.00"))
                .precioVenta(new java.math.BigDecimal("280.00"))
                .categoria(electronica)
                .build();

        Producto producto6 = Producto.builder()
                .nombre("Desinfectante multiusos 500ml")
                .descripcion("Desinfectante concentrado para limpiar y desinfectar superficies")
                .stock(50)
                .vencimiento(true)
                .costo(new java.math.BigDecimal("2.50"))
                .precioVenta(new java.math.BigDecimal("4.99"))
                .categoria(limpieza)
                .build();

        Producto producto7 = Producto.builder()
                .nombre("Papel higiénico pack 12")
                .descripcion("Papel higiénico de doble hoja, ultra suave")
                .stock(100)
                .vencimiento(false)
                .costo(new java.math.BigDecimal("5.00"))
                .precioVenta(new java.math.BigDecimal("7.99"))
                .categoria(limpieza)
                .build();

        Producto producto8 = Producto.builder()
                .nombre("Leche entera 1L")
                .descripcion("Leche entera fresca de granja")
                .stock(30)
                .vencimiento(true)
                .costo(new java.math.BigDecimal("1.50"))
                .precioVenta(new java.math.BigDecimal("2.49"))
                .categoria(lacteos)
                .build();

        productoRepository.saveAll(Arrays.asList(producto1, producto2, producto3, producto4, producto5, producto6, producto7, producto8));
        System.out.println("✅ Productos de demostración creados exitosamente");
    }

    private void crearVentasDemo() {
        // Obtener productos para crear ventas
        Producto laptop = productoRepository.findByNombre("Laptop Dell Inspiron").orElse(null);
        Producto mouse = productoRepository.findByNombre("Mouse Logitech MX Master").orElse(null);
        Producto teclado = productoRepository.findByNombre("Teclado Mecánico RGB").orElse(null);
        Producto monitor = productoRepository.findByNombre("Monitor 27\" 4K").orElse(null);
        Producto auriculares = productoRepository.findByNombre("Auriculares Sony WH-1000XM4").orElse(null);

        if (laptop != null && mouse != null && teclado != null) {
            // Venta 1
            Venta venta1 = Venta.builder()
                    .fecha(java.time.LocalDate.now().minusDays(2))
                    .total(new java.math.BigDecimal("895.99"))
                    .build();

            ItemVenta item1 = ItemVenta.builder()
                    .venta(venta1)
                    .producto(laptop)
                    .cantidad(1)
                    .precioUnitario(laptop.getPrecioVenta())
                    .build();

            ItemVenta item2 = ItemVenta.builder()
                    .venta(venta1)
                    .producto(mouse)
                    .cantidad(1)
                    .precioUnitario(mouse.getPrecioVenta())
                    .build();

            venta1.setItems(Arrays.asList(item1, item2));
            ventaRepository.save(venta1);

            // Venta 2
            if (monitor != null && auriculares != null) {
                Venta venta2 = Venta.builder()
                        .fecha(java.time.LocalDate.now().minusDays(1))
                        .total(new java.math.BigDecimal("630.00"))
                        .build();

                ItemVenta item3 = ItemVenta.builder()
                        .venta(venta2)
                        .producto(monitor)
                        .cantidad(1)
                        .precioUnitario(monitor.getPrecioVenta())
                        .build();

                ItemVenta item4 = ItemVenta.builder()
                        .venta(venta2)
                        .producto(auriculares)
                        .cantidad(1)
                        .precioUnitario(auriculares.getPrecioVenta())
                        .build();

                venta2.setItems(Arrays.asList(item3, item4));
                ventaRepository.save(venta2);
            }

            // Venta 3
            Venta venta3 = Venta.builder()
                    .fecha(java.time.LocalDate.now())
                    .total(new java.math.BigDecimal("120.50"))
                    .build();

            ItemVenta item5 = ItemVenta.builder()
                    .venta(venta3)
                    .producto(teclado)
                    .cantidad(1)
                    .precioUnitario(teclado.getPrecioVenta())
                    .build();

            venta3.setItems(Arrays.asList(item5));
            ventaRepository.save(venta3);

            System.out.println("✅ Ventas de demostración creadas exitosamente");
        }
    }
}
