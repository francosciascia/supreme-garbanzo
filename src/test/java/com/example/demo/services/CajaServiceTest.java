package com.example.demo.services;

import com.example.demo.models.*;
import com.example.demo.repository.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import java.math.BigDecimal;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CajaServiceTest {
    @Mock CajaRepository cajas; @Mock MovimientoCajaRepository movimientos; @Mock PersonaRepository personas;
    @Mock AuditoriaService auditoria;
    @InjectMocks CajaService service;

    @Test void abreUnaCajaParaElUsuario() {
        Persona usuario=Persona.builder().id(1L).nombre("Ana").apellido("Cajera").build();
        when(cajas.findFirstByUsuarioIdAndEstadoOrderByFechaAperturaDesc(1L,Caja.Estado.ABIERTA)).thenReturn(Optional.empty());
        when(personas.findById(1L)).thenReturn(Optional.of(usuario));when(cajas.save(any())).thenAnswer(i->{Caja c=i.getArgument(0);c.setId(10L);return c;});
        var result=service.abrir(1L,new BigDecimal("10000"));
        assertEquals(10L,result.id());assertEquals("ABIERTA",result.estado());verify(cajas).save(any());
    }

    @Test void impideDosCajasAbiertasParaElMismoUsuario() {
        when(cajas.findFirstByUsuarioIdAndEstadoOrderByFechaAperturaDesc(1L,Caja.Estado.ABIERTA)).thenReturn(Optional.of(new Caja()));
        assertThrows(IllegalStateException.class,()->service.abrir(1L,BigDecimal.ZERO));verify(cajas,never()).save(any());
    }

    @Test void cierraCalculandoDiferencia() {
        Persona usuario=Persona.builder().id(1L).nombre("Ana").apellido("Cajera").build();
        Caja caja=Caja.builder().id(2L).usuario(usuario).montoInicial(new BigDecimal("1000")).estado(Caja.Estado.ABIERTA).build();
        when(cajas.findById(2L)).thenReturn(Optional.of(caja));
        when(movimientos.findByCajaIdOrderByFechaDesc(2L)).thenReturn(java.util.List.of());
        when(movimientos.saldoMovimientos(2L)).thenReturn(new BigDecimal("500"));
        when(cajas.save(any())).thenAnswer(i->i.getArgument(0));
        var result=service.cerrar(2L,new BigDecimal("1490"));
        assertEquals(new BigDecimal("1500"),result.esperado());assertEquals(new BigDecimal("-10"),result.diferencia());assertEquals("CERRADA",result.estado());
    }
}
