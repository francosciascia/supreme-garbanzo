package com.example.demo.controller;
import com.example.demo.dto.*;
import com.example.demo.models.MovimientoCaja;
import com.example.demo.services.CajaService;
import jakarta.validation.constraints.*;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.List;

@RestController @RequestMapping("/api/cajas")
public class CajaController {
    private final CajaService service;
    public CajaController(CajaService service) { this.service = service; }
    @PostMapping("/abrir") public CajaDTO abrir(@RequestBody AbrirCajaRequest r) { return service.abrir(r.usuarioId(), r.montoInicial()); }
    @GetMapping("/activa/{usuarioId}") public CajaDTO activa(@PathVariable Long usuarioId) { return service.activa(usuarioId).orElse(null); }
    @PostMapping("/{id}/movimientos") public MovimientoCajaDTO movimiento(@PathVariable Long id, @RequestBody MovimientoRequest r) {
        return service.movimiento(id, MovimientoCaja.Tipo.valueOf(r.tipo()), r.monto(), r.descripcion());
    }
    @GetMapping("/{id}/movimientos") public List<MovimientoCajaDTO> movimientos(@PathVariable Long id) { return service.movimientos(id); }
    @PostMapping("/{id}/cerrar") public CajaDTO cerrar(@PathVariable Long id, @RequestBody CerrarCajaRequest r) { return service.cerrar(id, r.montoReal()); }
    public record AbrirCajaRequest(@NotNull Long usuarioId, @NotNull @PositiveOrZero BigDecimal montoInicial) {}
    public record MovimientoRequest(@NotBlank String tipo, @NotNull @Positive BigDecimal monto, String descripcion) {}
    public record CerrarCajaRequest(@NotNull @PositiveOrZero BigDecimal montoReal) {}
}
