package com.example.demo.services;

import com.example.demo.dto.ReglasOperativasDTO;
import com.example.demo.models.Cliente;
import com.example.demo.models.MovimientoCuenta;
import com.example.demo.models.Venta;
import com.example.demo.repository.ClienteRepository;
import com.example.demo.repository.MovimientoCuentaRepository;
import com.example.demo.repository.PersonaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CuentaCorrienteServiceTest {
    @Mock ClienteRepository clientes;
    @Mock PersonaRepository personas;
    @Mock MovimientoCuentaRepository movimientos;
    @Mock ReglasOperativasService reglas;
    @Mock AuditoriaService auditoria;
    @InjectMocks CuentaCorrienteService service;

    @Test
    void registraVentaFiadaDentroDelLimite() {
        Cliente cliente = Cliente.builder().id(1L).saldoCuenta(new BigDecimal("1000"))
                .limiteCredito(new BigDecimal("5000")).build();
        Venta venta = Venta.builder().id(2L).cliente(cliente).total(new BigDecimal("1500"))
                .numeroComprobante("V-2").build();
        when(reglas.obtener()).thenReturn(reglas(true, BigDecimal.ZERO));
        when(movimientos.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        service.registrarVenta(venta, null);

        assertEquals(new BigDecimal("2500"), cliente.getSaldoCuenta());
        assertEquals(new BigDecimal("1500"), venta.getMontoFiado());
        verify(movimientos).save(any(MovimientoCuenta.class));
    }

    @Test
    void rechazaVentaQueSuperaElLimite() {
        Cliente cliente = Cliente.builder().id(1L).saldoCuenta(new BigDecimal("4500"))
                .limiteCredito(new BigDecimal("5000")).build();
        Venta venta = Venta.builder().cliente(cliente).total(new BigDecimal("600")).build();
        when(reglas.obtener()).thenReturn(reglas(true, BigDecimal.ZERO));

        assertThrows(IllegalStateException.class, () -> service.registrarVenta(venta, null));
        verify(movimientos, never()).save(any());
    }

    @Test
    void registraPagoYReduceLaDeuda() {
        Cliente cliente = Cliente.builder().id(1L).saldoCuenta(new BigDecimal("2000")).build();
        when(clientes.findById(1L)).thenReturn(Optional.of(cliente));
        when(movimientos.save(any())).thenAnswer(invocation -> {
            MovimientoCuenta movimiento = invocation.getArgument(0);
            movimiento.setId(10L);
            return movimiento;
        });

        var resultado = service.registrarPago(1L, new BigDecimal("750"), "Pago", null);

        assertEquals(new BigDecimal("1250"), cliente.getSaldoCuenta());
        assertEquals(new BigDecimal("1250"), resultado.saldoResultante());
    }

    private ReglasOperativasDTO reglas(boolean fiado, BigDecimal limite) {
        return new ReglasOperativasDTO(true, false, new BigDecimal("20"), fiado, limite, true, 30, false);
    }
}
