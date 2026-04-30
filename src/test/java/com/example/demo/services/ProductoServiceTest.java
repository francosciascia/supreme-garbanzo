package com.example.demo.services;

import com.example.demo.dto.ProductoCUDTO;
import com.example.demo.dto.ProductoDTO;
import com.example.demo.models.Producto;
import com.example.demo.repository.ProductoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para ProductoService
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ProductoService Tests")
class ProductoServiceTest {

    @Mock
    private ProductoRepository productoRepository;

    @InjectMocks
    private ProductoService productoService;

    private Producto productoMock;
    private ProductoCUDTO productoDTOMock;

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

        productoDTOMock = new ProductoCUDTO(
                "Laptop",
                "Laptop Gaming",
                10,
                false,
                new BigDecimal("500.00"),
                new BigDecimal("800.00")
        );
    }

    @Test
    @DisplayName("Debería listar todos los productos")
    void testListar() {
        // Arrange
        when(productoRepository.findAll()).thenReturn(Arrays.asList(productoMock));

        // Act
        List<ProductoDTO> resultado = productoService.listar();

        // Assert
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals("Laptop", resultado.get(0).nombre());
        verify(productoRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Debería obtener detalle de un producto existente")
    void testDetalleExistente() {
        // Arrange
        when(productoRepository.findById(1L)).thenReturn(Optional.of(productoMock));

        // Act
        Optional<ProductoDTO> resultado = productoService.detalle(1L);

        // Assert
        assertTrue(resultado.isPresent());
        assertEquals("Laptop", resultado.get().nombre());
        verify(productoRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Debería retornar vacio si producto no existe")
    void testDetalleNoExistente() {
        // Arrange
        when(productoRepository.findById(999L)).thenReturn(Optional.empty());

        // Act
        Optional<ProductoDTO> resultado = productoService.detalle(999L);

        // Assert
        assertFalse(resultado.isPresent());
        verify(productoRepository, times(1)).findById(999L);
    }

    @Test
    @DisplayName("Debería crear un producto exitosamente")
    void testCrearProducto() {
        // Arrange
        when(productoRepository.save(any(Producto.class))).thenReturn(productoMock);

        // Act
        ProductoDTO resultado = productoService.crear(productoDTOMock);

        // Assert
        assertNotNull(resultado);
        assertEquals("Laptop", resultado.nombre());
        assertEquals(new BigDecimal("800.00"), resultado.precioVenta());
        verify(productoRepository, times(1)).save(any(Producto.class));
    }

    @Test
    @DisplayName("Debería fallar al crear producto con precio venta menor a costo")
    void testCrearProductoPrecioInvalido() {
        // Arrange
        ProductoCUDTO productoInvalido = new ProductoCUDTO(
                "Producto",
                "Descripción",
                10,
                false,
                new BigDecimal("500.00"),
                new BigDecimal("300.00") // precio < costo
        );

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            productoService.crear(productoInvalido);
        });

        verify(productoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debería actualizar un producto exitosamente")
    void testActualizarProducto() {
        // Arrange
        ProductoCUDTO cambios = new ProductoCUDTO(
                "Laptop Actualizada",
                "Nueva descripción",
                5,
                false,
                new BigDecimal("400.00"),
                new BigDecimal("700.00")
        );

        when(productoRepository.findById(1L)).thenReturn(Optional.of(productoMock));
        when(productoRepository.save(any(Producto.class))).thenReturn(productoMock);

        // Act
        ProductoDTO resultado = productoService.actualizar(1L, cambios);

        // Assert
        assertNotNull(resultado);
        verify(productoRepository, times(1)).findById(1L);
        verify(productoRepository, times(1)).save(any(Producto.class));
    }

    @Test
    @DisplayName("Debería fallar al actualizar producto que no existe")
    void testActualizarProductoNoExistente() {
        // Arrange
        when(productoRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            productoService.actualizar(999L, productoDTOMock);
        });

        verify(productoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debería eliminar un producto exitosamente")
    void testEliminarProducto() {
        // Arrange
        when(productoRepository.existsById(1L)).thenReturn(true);

        // Act
        productoService.eliminar(1L);

        // Assert
        verify(productoRepository, times(1)).existsById(1L);
        verify(productoRepository, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("Debería fallar al eliminar producto que no existe")
    void testEliminarProductoNoExistente() {
        // Arrange
        when(productoRepository.existsById(999L)).thenReturn(false);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            productoService.eliminar(999L);
        });

        verify(productoRepository, never()).deleteById(any());
    }
}

