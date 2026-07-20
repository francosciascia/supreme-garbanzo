package com.example.demo.services;
import com.example.demo.models.*;import com.example.demo.repository.*;import org.junit.jupiter.api.Test;import org.junit.jupiter.api.extension.ExtendWith;import org.mockito.*;import org.mockito.junit.jupiter.MockitoExtension;import java.util.Optional;import static org.junit.jupiter.api.Assertions.*;import static org.mockito.ArgumentMatchers.any;import static org.mockito.Mockito.*;
@ExtendWith(MockitoExtension.class) class InventarioServiceTest{
    @Mock ProductoRepository productos;@Mock MovimientoStockRepository movimientos;@Mock PersonaRepository personas;@InjectMocks InventarioService service;
    @Test void ajustaStockYRegistraTrazabilidad(){Producto p=Producto.builder().id(3L).nombre("Gaseosa").stock(5).build();when(productos.findByIdForUpdate(3L)).thenReturn(Optional.of(p));when(movimientos.save(any())).thenAnswer(i->{MovimientoStock m=i.getArgument(0);m.setId(8L);return m;});var result=service.ajustar(3L,9,"Conteo físico",null);assertEquals(9,p.getStock());assertEquals(4,result.cantidad());assertEquals("AJUSTE",result.tipo());verify(movimientos).save(any());}
    @Test void rechazaStockNegativo(){assertThrows(IllegalArgumentException.class,()->service.ajustar(3L,-1,"error",null));verifyNoInteractions(productos,movimientos);}
}
