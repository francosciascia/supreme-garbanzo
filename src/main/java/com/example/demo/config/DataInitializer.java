package com.example.demo.config;

import com.example.demo.models.Cliente;
import com.example.demo.models.Persona;
import com.example.demo.models.Producto;
import com.example.demo.models.Venta;
import com.example.demo.models.ItemVenta;
import com.example.demo.models.Categoria;
import com.example.demo.repository.ClienteRepository;
import com.example.demo.repository.PersonaRepository;
import com.example.demo.repository.ProductoRepository;
import com.example.demo.repository.VentaRepository;
import com.example.demo.repository.CategoriaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

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
    private ClienteRepository clienteRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // Estas dos siembras son idempotentes: agregan sólo lo que falte.
        sembrarCategorias();
        sembrarProductos();

        if (personaRepository.count() == 0) {
            crearUsuariosDemo();
        }

        if (clienteRepository.count() == 0) {
            crearClientesDemo();
        }

        if (ventaRepository.count() == 0) {
            crearVentasDemo();
        }
    }

    // ============================================================
    //  Categorías
    // ============================================================

    private void sembrarCategorias() {
        List<Categoria> deseadas = Arrays.asList(
                new Categoria("Electrónica", "Productos electrónicos y computadoras"),
                new Categoria("Limpieza", "Productos de limpieza y desinfección"),
                new Categoria("Lácteos", "Productos lácteos frescos"),
                new Categoria("Alimentos", "Alimentos secos y enlatados"),
                new Categoria("Higiene Personal", "Productos de higiene personal"),
                new Categoria("Bebidas", "Gaseosas, jugos, aguas y bebidas alcohólicas"),
                new Categoria("Panadería", "Pan, facturas, galletitas y productos de panadería"),
                new Categoria("Carnicería", "Cortes vacunos, de cerdo, pollo y embutidos"),
                new Categoria("Frutas y Verduras", "Frutas y verduras frescas"),
                new Categoria("Congelados", "Productos congelados y precocidos"),
                new Categoria("Mascotas", "Alimento y accesorios para mascotas")
        );

        int creadas = 0;
        for (Categoria c : deseadas) {
            if (!categoriaRepository.existsByNombre(c.getNombre())) {
                categoriaRepository.save(c);
                creadas++;
            }
        }

        if (creadas > 0) {
            System.out.println("Categorías de demostración creadas: " + creadas);
        }
    }

    // ============================================================
    //  Productos
    // ============================================================

    private void sembrarProductos() {
        // Categorías originales (5 productos cada una, como estaba)
        addProducto("Laptop Dell Inspiron", "Laptop de 15.6 pulgadas con procesador Intel Core i5", 10, false, "700.00", "850.00", "Electrónica");
        addProducto("Mouse Logitech MX Master", "Mouse inalámbrico ergonómico con batería de larga duración", 25, false, "30.00", "45.99", "Electrónica");
        addProducto("Teclado Mecánico RGB", "Teclado mecánico con switches Cherry MX y iluminación RGB", 15, false, "80.00", "120.50", "Electrónica");
        addProducto("Monitor 27\" 4K", "Monitor Ultra HD de 27 pulgadas con resolución 4K", 8, false, "250.00", "350.00", "Electrónica");
        addProducto("Auriculares Sony WH-1000XM4", "Auriculares inalámbricos con cancelación de ruido activa", 12, false, "200.00", "280.00", "Electrónica");
        addProducto("Desinfectante multiusos 500ml", "Desinfectante concentrado para limpiar y desinfectar superficies", 50, true, "2.50", "4.99", "Limpieza");
        addProducto("Papel higiénico pack 12", "Papel higiénico de doble hoja, ultra suave", 100, false, "5.00", "7.99", "Limpieza");
        addProducto("Leche entera 1L", "Leche entera fresca de granja", 30, true, "1.50", "2.49", "Lácteos");

        // ====== Bebidas (20) ======
        addProducto("Coca Cola 2L", "Gaseosa cola sabor original", 80, true, "1500.00", "2000.00", "Bebidas");
        addProducto("Sprite 1.5L", "Gaseosa lima-limón sin cafeína", 60, true, "1200.00", "1700.00", "Bebidas");
        addProducto("Agua Mineral 2L", "Agua mineral natural sin gas", 120, true, "500.00", "800.00", "Bebidas");
        addProducto("Cerveza Quilmes 1L", "Cerveza rubia tipo lager botella retornable", 70, true, "1800.00", "2500.00", "Bebidas");
        addProducto("Vino Malbec 750ml", "Vino tinto Malbec varietal mendocino", 45, false, "4500.00", "6500.00", "Bebidas");
        addProducto("Jugo de Naranja 1L", "Jugo de naranja exprimido sin azúcar agregada", 50, true, "1300.00", "1900.00", "Bebidas");
        addProducto("Energizante Speed 250ml", "Bebida energizante con cafeína y taurina", 90, true, "1500.00", "2200.00", "Bebidas");
        addProducto("Café Nescafé 170g", "Café instantáneo en frasco", 40, true, "5000.00", "7500.00", "Bebidas");
        addProducto("Té La Virginia 25 saquitos", "Té negro tradicional en saquitos", 55, true, "800.00", "1200.00", "Bebidas");
        addProducto("Leche Chocolatada Cindor 1L", "Leche con cacao lista para tomar", 35, true, "1400.00", "2000.00", "Bebidas");
        addProducto("Fanta Naranja 1.5L", "Gaseosa sabor naranja", 65, true, "1200.00", "1700.00", "Bebidas");
        addProducto("Pepsi 2.25L", "Gaseosa cola tradicional", 70, true, "1500.00", "2000.00", "Bebidas");
        addProducto("Cerveza Stella Artois 473ml", "Cerveza belga premium lata", 90, true, "1600.00", "2400.00", "Bebidas");
        addProducto("Vino Chardonnay 750ml", "Vino blanco varietal", 30, false, "4200.00", "6200.00", "Bebidas");
        addProducto("Fernet Branca 750ml", "Aperitivo italiano clásico", 25, false, "8500.00", "12500.00", "Bebidas");
        addProducto("Whisky Johnnie Walker Red 750ml", "Whisky escocés Red Label", 15, false, "18000.00", "26500.00", "Bebidas");
        addProducto("Agua Tónica Schweppes 1.5L", "Agua tónica con quinina", 50, true, "1100.00", "1700.00", "Bebidas");
        addProducto("Cerveza Heineken 473ml lata", "Cerveza rubia premium en lata", 100, true, "1700.00", "2500.00", "Bebidas");
        addProducto("Yerba Mate Playadito 1kg", "Yerba mate con palo seleccionada", 60, true, "3500.00", "5200.00", "Bebidas");
        addProducto("Mate Cocido La Virginia 25 saquitos", "Mate cocido en saquitos", 45, true, "900.00", "1400.00", "Bebidas");

        // ====== Panadería (20) ======
        addProducto("Pan Lactal Bimbo 540g", "Pan de molde clásico", 30, true, "1800.00", "2700.00", "Panadería");
        addProducto("Medialunas x12", "Medialunas dulces de manteca", 25, true, "2000.00", "3000.00", "Panadería");
        addProducto("Facturas surtidas x6", "Surtido de facturas dulces frescas", 20, true, "1500.00", "2300.00", "Panadería");
        addProducto("Pan rallado 500g", "Pan rallado fino para empanizar", 60, true, "600.00", "950.00", "Panadería");
        addProducto("Galletitas Oreo 117g", "Galletitas rellenas con crema sabor vainilla", 80, true, "1100.00", "1700.00", "Panadería");
        addProducto("Galletitas Pepitos 130g", "Galletitas dulces con chips de chocolate", 75, true, "950.00", "1450.00", "Panadería");
        addProducto("Tostadas Wasa 250g", "Tostadas crujientes de centeno", 40, true, "2200.00", "3300.00", "Panadería");
        addProducto("Bizcochos Don Satur 200g", "Bizcochos de grasa salados", 50, true, "700.00", "1100.00", "Panadería");
        addProducto("Pan de salvado 500g", "Pan integral con salvado de trigo", 28, true, "1600.00", "2400.00", "Panadería");
        addProducto("Pan ciabatta 350g", "Pan italiano artesanal", 22, true, "1400.00", "2100.00", "Panadería");
        addProducto("Pan Felipe x6", "Pan tipo felipe para sándwich", 35, true, "1300.00", "2000.00", "Panadería");
        addProducto("Bagels x4", "Bagels clásicos para sándwich", 18, true, "2400.00", "3600.00", "Panadería");
        addProducto("Croissants de chocolate x6", "Croissants rellenos con chocolate", 24, true, "2800.00", "4200.00", "Panadería");
        addProducto("Tortita Negra 200g", "Tortita negra tradicional con azúcar", 30, true, "1200.00", "1800.00", "Panadería");
        addProducto("Vainillas Bagley 150g", "Vainillas para postres y tiramisú", 60, true, "1100.00", "1650.00", "Panadería");
        addProducto("Criollitas Bagley 200g", "Galletitas de agua clásicas", 70, true, "900.00", "1400.00", "Panadería");
        addProducto("Galletitas Toddy 130g", "Galletitas dulces con cacao", 80, true, "1000.00", "1500.00", "Panadería");
        addProducto("Pan árabe x4", "Pan árabe blanco para rellenar", 40, true, "1100.00", "1700.00", "Panadería");
        addProducto("Mignon integral x6", "Pan mignon de harina integral", 32, true, "1500.00", "2300.00", "Panadería");
        addProducto("Galletitas Sonrisas 200g", "Galletitas rellenas con dulce de leche", 65, true, "1200.00", "1800.00", "Panadería");

        // ====== Carnicería (20) ======
        addProducto("Asado de tira 1kg", "Corte tradicional para parrilla", 20, true, "8000.00", "12000.00", "Carnicería");
        addProducto("Vacío 1kg", "Corte vacuno para asado, tierno y jugoso", 18, true, "9500.00", "14000.00", "Carnicería");
        addProducto("Bondiola de cerdo 1kg", "Bondiola de cerdo fresca", 15, true, "7500.00", "11000.00", "Carnicería");
        addProducto("Pollo entero 1kg", "Pollo fresco de granja", 30, true, "3500.00", "5200.00", "Carnicería");
        addProducto("Pechuga de pollo 1kg", "Pechuga de pollo sin piel", 35, true, "5000.00", "7500.00", "Carnicería");
        addProducto("Milanesas de ternera 1kg", "Milanesas de ternera ya preparadas", 25, true, "7000.00", "10500.00", "Carnicería");
        addProducto("Chorizo parrillero 1kg", "Chorizo fresco para parrilla", 40, true, "4500.00", "6800.00", "Carnicería");
        addProducto("Morcilla 1kg", "Morcilla criolla para parrilla", 30, true, "3800.00", "5600.00", "Carnicería");
        addProducto("Lomo 1kg", "Corte premium magro", 10, true, "12000.00", "17500.00", "Carnicería");
        addProducto("Carne picada especial 1kg", "Carne picada de cuadril", 28, true, "5500.00", "8200.00", "Carnicería");
        addProducto("Cuadril 1kg", "Cuadril fresco entero", 18, true, "8500.00", "12500.00", "Carnicería");
        addProducto("Matambre 1kg", "Matambre vacuno para horno o parrilla", 14, true, "7800.00", "11500.00", "Carnicería");
        addProducto("Costillas de cerdo 1kg", "Costillar de cerdo fresco", 16, true, "6800.00", "10200.00", "Carnicería");
        addProducto("Hígado vacuno 1kg", "Hígado vacuno fresco", 12, true, "4200.00", "6300.00", "Carnicería");
        addProducto("Riñones 500g", "Riñones vacunos frescos", 10, true, "2800.00", "4200.00", "Carnicería");
        addProducto("Mollejas 500g", "Mollejas de corazón para parrilla", 8, true, "9500.00", "14500.00", "Carnicería");
        addProducto("Chinchulines 500g", "Chinchulines de cordero", 12, true, "5200.00", "7800.00", "Carnicería");
        addProducto("Salchichas Vienissima x6", "Salchichas tipo Viena", 50, true, "2200.00", "3300.00", "Carnicería");
        addProducto("Jamón cocido 200g", "Jamón cocido en fetas", 60, true, "3200.00", "4800.00", "Carnicería");
        addProducto("Salame Cardín 200g", "Salame tipo Milán en fetas", 45, true, "3800.00", "5700.00", "Carnicería");

        // ====== Frutas y Verduras (20) ======
        addProducto("Manzana Roja 1kg", "Manzana roja fresca", 60, true, "1200.00", "1900.00", "Frutas y Verduras");
        addProducto("Banana 1kg", "Banana ecuatoriana madura", 80, true, "900.00", "1500.00", "Frutas y Verduras");
        addProducto("Naranja 1kg", "Naranja jugosa para exprimir", 70, true, "800.00", "1300.00", "Frutas y Verduras");
        addProducto("Tomate Perita 1kg", "Tomate perita fresco", 50, true, "1500.00", "2300.00", "Frutas y Verduras");
        addProducto("Papa 1kg", "Papa blanca lavada", 100, true, "700.00", "1100.00", "Frutas y Verduras");
        addProducto("Cebolla 1kg", "Cebolla blanca de campo", 90, true, "600.00", "1000.00", "Frutas y Verduras");
        addProducto("Lechuga unidad", "Lechuga mantecosa fresca", 40, true, "700.00", "1100.00", "Frutas y Verduras");
        addProducto("Zanahoria 1kg", "Zanahoria fresca", 65, true, "800.00", "1300.00", "Frutas y Verduras");
        addProducto("Palta unidad", "Palta Hass madura", 30, true, "1500.00", "2300.00", "Frutas y Verduras");
        addProducto("Frutilla 500g", "Frutillas frescas de estación", 25, true, "2500.00", "3700.00", "Frutas y Verduras");
        addProducto("Pera 1kg", "Pera Williams jugosa", 50, true, "1300.00", "2000.00", "Frutas y Verduras");
        addProducto("Limón 1kg", "Limón fresco tucumano", 55, true, "900.00", "1500.00", "Frutas y Verduras");
        addProducto("Ananá unidad", "Ananá maduro", 22, true, "2500.00", "3800.00", "Frutas y Verduras");
        addProducto("Sandía unidad", "Sandía fresca de estación", 18, true, "3500.00", "5200.00", "Frutas y Verduras");
        addProducto("Melón unidad", "Melón rocío de miel", 20, true, "2200.00", "3300.00", "Frutas y Verduras");
        addProducto("Uva verde 500g", "Uva blanca sin semilla", 30, true, "2000.00", "3000.00", "Frutas y Verduras");
        addProducto("Mandarina 1kg", "Mandarina dulce de estación", 60, true, "1100.00", "1700.00", "Frutas y Verduras");
        addProducto("Pimiento rojo 1kg", "Pimiento rojo fresco", 35, true, "2200.00", "3300.00", "Frutas y Verduras");
        addProducto("Pepino 1kg", "Pepino fresco", 45, true, "1300.00", "2000.00", "Frutas y Verduras");
        addProducto("Brócoli unidad", "Brócoli verde fresco", 30, true, "1500.00", "2300.00", "Frutas y Verduras");

        // ====== Congelados (20) ======
        addProducto("Hamburguesas Paty x4", "Hamburguesas de carne premium", 50, true, "2800.00", "4200.00", "Congelados");
        addProducto("Patitas de pollo Granja del Sol 500g", "Patitas de pollo rebozadas", 45, true, "3200.00", "4800.00", "Congelados");
        addProducto("Bastones de muzzarella 250g", "Bastones rebozados con muzzarella", 35, true, "2500.00", "3800.00", "Congelados");
        addProducto("Helado Frigor 1L", "Helado familiar sabores surtidos", 30, true, "3500.00", "5200.00", "Congelados");
        addProducto("Pizza congelada Sibarita", "Pizza muzzarella congelada", 25, true, "2200.00", "3300.00", "Congelados");
        addProducto("Verduras mixtas 500g", "Mix de verduras congeladas listas para cocinar", 40, true, "1800.00", "2700.00", "Congelados");
        addProducto("Empanadas surtidas x12", "Empanadas árabes, carne, pollo y jamón y queso", 30, true, "4500.00", "6800.00", "Congelados");
        addProducto("Papas fritas McCain 750g", "Papas fritas prefritas congeladas", 55, true, "2600.00", "3900.00", "Congelados");
        addProducto("Pescado merluza filet 500g", "Filet de merluza sin espinas", 22, true, "4000.00", "6000.00", "Congelados");
        addProducto("Camarones congelados 250g", "Camarones pelados listos para cocinar", 15, true, "7500.00", "11200.00", "Congelados");
        addProducto("Tarta jamón y queso congelada", "Tarta lista para horno", 28, true, "2600.00", "3900.00", "Congelados");
        addProducto("Helado Grido 1kg", "Helado artesanal en pote", 24, true, "4200.00", "6300.00", "Congelados");
        addProducto("Lasagna Sibarita 500g", "Lasagna boloñesa congelada", 18, true, "3800.00", "5700.00", "Congelados");
        addProducto("Nuggets de pollo 500g", "Nuggets de pollo rebozados", 40, true, "2900.00", "4400.00", "Congelados");
        addProducto("Sorrentinos congelados 500g", "Sorrentinos jamón y muzzarella", 25, true, "3300.00", "4900.00", "Congelados");
        addProducto("Ñoquis Don Vicente 500g", "Ñoquis de papa listos para cocinar", 35, true, "2400.00", "3600.00", "Congelados");
        addProducto("Calamares rebozados 400g", "Anillas de calamar rebozadas", 20, true, "4500.00", "6800.00", "Congelados");
        addProducto("Pollo a la napolitana 250g", "Suprema napolitana congelada", 30, true, "2200.00", "3300.00", "Congelados");
        addProducto("Brócoli IQF 500g", "Brócoli congelado individual", 38, true, "1900.00", "2900.00", "Congelados");
        addProducto("Espinaca congelada 400g", "Espinaca picada congelada", 42, true, "1600.00", "2400.00", "Congelados");

        // ====== Mascotas (20) ======
        addProducto("Alimento Pedigree Adultos 3kg", "Alimento balanceado para perros adultos", 40, true, "6500.00", "9800.00", "Mascotas");
        addProducto("Alimento Whiskas 1kg", "Alimento balanceado para gatos adultos", 50, true, "3500.00", "5200.00", "Mascotas");
        addProducto("Arena sanitaria 4kg", "Arena absorbente para gatos", 35, false, "2800.00", "4200.00", "Mascotas");
        addProducto("Hueso masticable Greenies", "Hueso dental para perros", 60, true, "1500.00", "2300.00", "Mascotas");
        addProducto("Pipeta antipulgas Frontline", "Pipeta antipulgas y garrapatas para perros", 25, true, "5500.00", "8200.00", "Mascotas");
        addProducto("Snack Pedigree Dentastix", "Snack dental para perros", 70, true, "2200.00", "3300.00", "Mascotas");
        addProducto("Shampoo para perros 500ml", "Shampoo neutro para mascotas", 40, true, "3200.00", "4800.00", "Mascotas");
        addProducto("Juguete pelota Kong", "Pelota de goma resistente para perros", 30, false, "4500.00", "6700.00", "Mascotas");
        addProducto("Correa retráctil 5m", "Correa retráctil para paseos", 20, false, "6000.00", "9000.00", "Mascotas");
        addProducto("Comedero doble inox", "Comedero doble de acero inoxidable", 25, false, "3500.00", "5200.00", "Mascotas");
        addProducto("Alimento Eukanuba cachorros 3kg", "Alimento premium para cachorros", 28, true, "8200.00", "12300.00", "Mascotas");
        addProducto("Alimento Pro Plan gato 1kg", "Alimento premium para gatos adultos", 32, true, "5500.00", "8200.00", "Mascotas");
        addProducto("Bebedero automático", "Bebedero automático con dispensador", 18, false, "7500.00", "11200.00", "Mascotas");
        addProducto("Transportadora rígida M", "Transportadora rígida para perros y gatos", 12, false, "12000.00", "18000.00", "Mascotas");
        addProducto("Cepillo deslanador", "Cepillo para remover pelo muerto", 30, false, "3200.00", "4800.00", "Mascotas");
        addProducto("Cama acolchada para perros M", "Cama acolchada lavable", 15, false, "9500.00", "14200.00", "Mascotas");
        addProducto("Rascador para gatos", "Rascador con plataforma y juguete", 14, false, "8800.00", "13200.00", "Mascotas");
        addProducto("Pelota mordedora ovalada", "Juguete mordedor de goma natural", 40, false, "1800.00", "2700.00", "Mascotas");
        addProducto("Pipeta Bayer antipulgas gatos", "Pipeta antipulgas para gatos", 28, true, "4200.00", "6300.00", "Mascotas");
        addProducto("Snack Temptations gatos 60g", "Snack crujiente para gatos sabor pollo", 65, true, "1500.00", "2300.00", "Mascotas");
    }

    /**
     * Crea el producto sólo si no existe otro con el mismo nombre.
     * Si la categoría no se encuentra, se loguea y se omite.
     */
    private void addProducto(
            String nombre,
            String descripcion,
            int stock,
            boolean vencimiento,
            String costo,
            String precioVenta,
            String nombreCategoria
    ) {
        if (productoRepository.findByNombre(nombre).isPresent()) {
            return;
        }

        Categoria categoria = categoriaRepository.findByNombre(nombreCategoria).orElse(null);
        if (categoria == null) {
            System.out.println("Categoría no encontrada al sembrar producto '" + nombre + "': " + nombreCategoria);
            return;
        }

        Producto producto = Producto.builder()
                .nombre(nombre)
                .descripcion(descripcion)
                .stock(stock)
                .vencimiento(vencimiento)
                .costo(new BigDecimal(costo))
                .precioVenta(new BigDecimal(precioVenta))
                .categoria(categoria)
                .build();

        productoRepository.save(producto);
    }

    // ============================================================
    //  Clientes
    // ============================================================

    private void crearClientesDemo() {
        Cliente c1 = Cliente.builder()
                .nombre("María")
                .apellido("González")
                .dni(30111222)
                .email("maria.gonzalez@example.com")
                .telefono("11-2345-6789")
                .direccion("Av. Corrientes 1234, CABA")
                .activo(true)
                .build();

        Cliente c2 = Cliente.builder()
                .nombre("Diego")
                .apellido("Martínez")
                .dni(28999111)
                .email("diego.martinez@example.com")
                .telefono("11-9876-5432")
                .direccion("Calle Falsa 742, CABA")
                .activo(true)
                .build();

        Cliente c3 = Cliente.builder()
                .nombre("Lucía")
                .apellido("Fernández")
                .dni(35444555)
                .email("lucia.fernandez@example.com")
                .telefono("11-1111-2222")
                .direccion("Av. Santa Fe 3030, CABA")
                .activo(true)
                .build();

        clienteRepository.saveAll(Arrays.asList(c1, c2, c3));
        System.out.println("Clientes de demostración creados exitosamente");
    }

    // ============================================================
    //  Usuarios
    // ============================================================

    private void crearUsuariosDemo() {
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
        System.out.println("Usuarios de demostración creados exitosamente");
    }

    // ============================================================
    //  Ventas demo
    // ============================================================

    private void crearVentasDemo() {
        Producto laptop = productoRepository.findByNombre("Laptop Dell Inspiron").orElse(null);
        Producto mouse = productoRepository.findByNombre("Mouse Logitech MX Master").orElse(null);
        Producto teclado = productoRepository.findByNombre("Teclado Mecánico RGB").orElse(null);
        Producto monitor = productoRepository.findByNombre("Monitor 27\" 4K").orElse(null);
        Producto auriculares = productoRepository.findByNombre("Auriculares Sony WH-1000XM4").orElse(null);

        if (laptop != null && mouse != null && teclado != null) {
            Venta venta1 = Venta.builder()
                    .fecha(java.time.LocalDate.now().minusDays(2))
                    .total(new BigDecimal("895.99"))
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

            if (monitor != null && auriculares != null) {
                Venta venta2 = Venta.builder()
                        .fecha(java.time.LocalDate.now().minusDays(1))
                        .total(new BigDecimal("630.00"))
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

            Venta venta3 = Venta.builder()
                    .fecha(java.time.LocalDate.now())
                    .total(new BigDecimal("120.50"))
                    .build();

            ItemVenta item5 = ItemVenta.builder()
                    .venta(venta3)
                    .producto(teclado)
                    .cantidad(1)
                    .precioUnitario(teclado.getPrecioVenta())
                    .build();

            venta3.setItems(Arrays.asList(item5));
            ventaRepository.save(venta3);

            System.out.println("Ventas de demostración creadas exitosamente");
        }
    }
}
