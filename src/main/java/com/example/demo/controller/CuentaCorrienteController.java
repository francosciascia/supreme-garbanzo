package com.example.demo.controller;

import com.example.demo.dto.MovimientoCuentaDTO;
import com.example.demo.services.CuentaCorrienteService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/cuentas-corrientes")
public class CuentaCorrienteController {
    private final CuentaCorrienteService service;

    public CuentaCorrienteController(CuentaCorrienteService service) {
        this.service = service;
    }

    @GetMapping("/{clienteId}/movimientos")
    public List<MovimientoCuentaDTO> movimientos(@PathVariable Long clienteId) {
        return service.movimientos(clienteId);
    }

    @PostMapping("/{clienteId}/pagos")
    public MovimientoCuentaDTO registrarPago(@PathVariable Long clienteId, @RequestBody PagoRequest request) {
        return service.registrarPago(clienteId, request.monto(), request.descripcion(), request.usuarioId());
    }

    public record PagoRequest(BigDecimal monto, String descripcion, Long usuarioId) { }
}
