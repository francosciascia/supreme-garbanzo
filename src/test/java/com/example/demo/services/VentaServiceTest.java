package com.example.demo.services;

import com.example.demo.dto.ItemVentaCreateDTO;
import com.example.demo.dto.VentaCreateDTO;
import com.example.demo.models.ItemVenta;
import com.example.demo.models.Producto;
import com.example.demo.models.Venta;
import com.example.demo.repository.ProductoRepository;
import com.example.demo.repository.VentaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para VentaService
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("VentaService Tests")
class VentaServiceTest {

    @Mock
    private VentaRepository ventaRepository;

    @Mock
    private ProductoRepository productoRepository;

    @InjectMocks
    private VentaService ventaService;

    private Venta ventaMock;
    private Producto productoMock;
    private VentaCreateDTO ventaCreateDTOMock;

    @BeforeEach
    void setUp() {
        productoMock = Producto.builder()
                .id(1L)
                .nombre("Laptop")
                .descripcion("Laptop Gaming")
                .stock(10)
                .vencimiento(false)
                .costo(new BigDecimal("500.00"))
                .precioVenta(new BigDecimal("800.00"))
                .build();

        ventaMock = new Venta();
        ventaMock.setId(1L);
        ventaMock.setFecha(LocalDate.now());
        ventaMock.setItems(new ArrayList<>());
        ventaMock.setTotal(BigDecimal.ZERO);

        ventaCreateDTOMock = new VentaCreateDTO(
                Arrays.asList(
                        new ItemVentaCreateDTO(1L, 2)
                )
        );
    }

    @Test
    @DisplayName("Debería listar todas las ventas")
    void testListar() {
        // Arrange
        when(ventaRepository.findAll()).thenReturn(Arrays.asList(ventaMock));

        // Act
        List<Venta> resultado = ventaService.listar();

        // Assert
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        verify(ventaRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Debería obtener detalle de una venta existente")
    void testDetalleExistente() {
        // Arrange
        when(ventaRepository.findById(1L)).thenReturn(Optional.of(ventaMock));

        // Act
        Optional<Venta> resultado = ventaService.detalle(1L);

        // Assert
        assertTrue(resultado.isPresent());
        assertEquals(1L, resultado.get().getId());
        verify(ventaRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Debería retornar vacio si venta no existe")
    void testDetalleNoExistente() {
        // Arrange
        when(ventaRepository.findById(999L)).thenReturn(Optional.empty());

        // Act
        Optional<Venta> resultado = ventaService.detalle(999L);

        // Assert
        assertFalse(resultado.isPresent());
    }

    @Test
    @DisplayName("Debería crear una venta exitosamente con stock disponible")
    void testCrearVenta() {
        // Arrange
        Venta ventaPersistida = new Venta();
        ventaPersistida.setId(1L);
        ventaPersistida.setFecha(LocalDate.now());
        ventaPersistida.setItems(new ArrayList<>());
        ventaPersistida.setTotal(BigDecimal.ZERO);

        when(productoRepository.findById(1L)).thenReturn(Optional.of(productoMock));
        when(ventaRepository.save(any(Venta.class))).thenReturn(ventaPersistida);

        // Act
        Venta resultado = ventaService.crear(ventaCreateDTOMock);

        // Assert
        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        verify(productoRepository, times(1)).findById(1L);
        verify(ventaRepository, times(1)).save(any(Venta.class));
    }

    @Test
    @DisplayName("Debería fallar al crear venta sin stock suficiente")
    void testCrearVentaSinStock() {
        // Arrange
        Producto productoSinStock = Producto.builder()
                .id(1L)
                .nombre("Laptop")
                .stock(1) // Stock insuficiente
                .costo(new BigDecimal("500.00"))
                .precioVenta(new BigDecimal("800.00"))
                .build();

        when(productoRepository.findById(1L)).thenReturn(Optional.of(productoSinStock));

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> {
            ventaService.crear(ventaCreateDTOMock);
        });

        verify(ventaRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debería fallar al crear venta con producto inexistente")
    void testCrearVentaProductoNoExistente() {
        // Arrange
        when(productoRepository.findById(999L)).thenReturn(Optional.empty());

        VentaCreateDTO ventaConProductoInvalido = new VentaCreateDTO(
                Arrays.asList(new ItemVentaCreateDTO(999L, 2))
        );

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            ventaService.crear(ventaConProductoInvalido);
        });

        verify(ventaRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debería agregar item a venta existente")
    void testAgregarItem() {
        // Arrange
        when(ventaRepository.findById(1L)).thenReturn(Optional.of(ventaMock));
        when(productoRepository.findById(1L)).thenReturn(Optional.of(productoMock));
        when(ventaRepository.save(any(Venta.class))).thenReturn(ventaMock);

        ItemVentaCreateDTO itemDTO = new ItemVentaCreateDTO(1L, 2);

        // Act
        Venta resultado = ventaService.agregarItem(1L, itemDTO);

        // Assert
        assertNotNull(resultado);
        verify(ventaRepository, times(1)).findById(1L);
        verify(productoRepository, times(1)).findById(1L);
        verify(ventaRepository, times(1)).save(any(Venta.class));
    }

    @Test
    @DisplayName("Debería fallar al agregar item a venta inexistente")
    void testAgregarItemVentaNoExistente() {
        // Arrange
        when(ventaRepository.findById(999L)).thenReturn(Optional.empty());

        ItemVentaCreateDTO itemDTO = new ItemVentaCreateDTO(1L, 2);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            ventaService.agregarItem(999L, itemDTO);
        });

        verify(ventaRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debería fallar al agregar item sin stock suficiente")
    void testAgregarItemSinStock() {
        // Arrange
        Producto productoSinStock = Producto.builder()
                .id(1L)
                .nombre("Laptop")
                .stock(1)
                .costo(new BigDecimal("500.00"))
                .precioVenta(new BigDecimal("800.00"))
                .build();

        when(ventaRepository.findById(1L)).thenReturn(Optional.of(ventaMock));
        when(productoRepository.findById(1L)).thenReturn(Optional.of(productoSinStock));

        ItemVentaCreateDTO itemDTO = new ItemVentaCreateDTO(1L, 5);

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> {
            ventaService.agregarItem(1L, itemDTO);
        });

        verify(ventaRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debería quitar item de venta")
    void testQuitarItem() {
        // Arrange
        ItemVenta itemVenta = new ItemVenta();
        itemVenta.setId(1L);
        itemVenta.setProducto(productoMock);
        itemVenta.setCantidad(2);
        itemVenta.setPrecioUnitario(new BigDecimal("800.00"));

        ventaMock.addItems(itemVenta);

        when(ventaRepository.findById(1L)).thenReturn(Optional.of(ventaMock));
        when(ventaRepository.save(any(Venta.class))).thenReturn(ventaMock);

        // Act
        Venta resultado = ventaService.quitarItem(1L, 1L);

        // Assert
        assertNotNull(resultado);
        verify(ventaRepository, times(1)).findById(1L);
        verify(ventaRepository, times(1)).save(any(Venta.class));
    }

    @Test
    @DisplayName("Debería fallar al quitar item inexistente")
    void testQuitarItemNoExistente() {
        // Arrange
        when(ventaRepository.findById(1L)).thenReturn(Optional.of(ventaMock));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            ventaService.quitarItem(1L, 999L);
        });

        verify(ventaRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debería anular una venta")
    void testAnularVenta() {
        // Arrange
        ItemVenta itemVenta = new ItemVenta();
        itemVenta.setId(1L);
        itemVenta.setProducto(productoMock);
        itemVenta.setCantidad(2);
        itemVenta.setPrecioUnitario(new BigDecimal("800.00"));

        ventaMock.addItems(itemVenta);

        when(ventaRepository.findById(1L)).thenReturn(Optional.of(ventaMock));
        when(ventaRepository.save(any(Venta.class))).thenReturn(ventaMock);

        // Act
        Venta resultado = ventaService.anular(1L);

        // Assert
        assertNotNull(resultado);
        verify(ventaRepository, times(1)).findById(1L);
        verify(ventaRepository, times(1)).save(any(Venta.class));
    }

    @Test
    @DisplayName("Debería fallar al anular venta inexistente")
    void testAnularVentaNoExistente() {
        // Arrange
        when(ventaRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            ventaService.anular(999L);
        });

        verify(ventaRepository, never()).save(any());
    }
}

