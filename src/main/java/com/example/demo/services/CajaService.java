package com.example.demo.services;

import com.example.demo.dto.CajaDTO;
import com.example.demo.dto.MovimientoCajaDTO;
import com.example.demo.models.*;
import com.example.demo.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class CajaService {
    private final CajaRepository cajas;
    private final MovimientoCajaRepository movimientos;
    private final PersonaRepository personas;

    public CajaService(CajaRepository cajas, MovimientoCajaRepository movimientos, PersonaRepository personas) {
        this.cajas = cajas; this.movimientos = movimientos; this.personas = personas;
    }

    @Transactional
    public CajaDTO abrir(Long usuarioId, BigDecimal montoInicial) {
        if (montoInicial == null || montoInicial.signum() < 0) throw new IllegalArgumentException("Monto inicial invalido");
        if (cajas.findFirstByUsuarioIdAndEstadoOrderByFechaAperturaDesc(usuarioId, Caja.Estado.ABIERTA).isPresent())
            throw new IllegalStateException("El usuario ya tiene una caja abierta");
        Persona usuario = personas.findById(usuarioId).orElseThrow(() -> new IllegalArgumentException("Usuario inexistente"));
        return toDTO(cajas.save(Caja.builder().usuario(usuario).fechaApertura(LocalDateTime.now())
                .montoInicial(montoInicial).estado(Caja.Estado.ABIERTA).build()));
    }

    @Transactional(readOnly = true)
    public Optional<CajaDTO> activa(Long usuarioId) {
        return cajas.findFirstByUsuarioIdAndEstadoOrderByFechaAperturaDesc(usuarioId, Caja.Estado.ABIERTA).map(this::toDTO);
    }

    @Transactional
    public MovimientoCajaDTO movimiento(Long cajaId, MovimientoCaja.Tipo tipo, BigDecimal monto, String descripcion) {
        Caja caja = cajaAbierta(cajaId);
        if (tipo == MovimientoCaja.Tipo.VENTA || tipo == MovimientoCaja.Tipo.ANULACION)
            throw new IllegalArgumentException("Tipo reservado para ventas");
        if (monto == null || monto.signum() <= 0) throw new IllegalArgumentException("El monto debe ser mayor a cero");
        return movimientoDTO(movimientos.save(MovimientoCaja.builder().caja(caja).fecha(LocalDateTime.now())
                .tipo(tipo).monto(monto).descripcion(descripcion).build()));
    }

    @Transactional
    public CajaDTO cerrar(Long cajaId, BigDecimal montoReal) {
        Caja caja = cajaAbierta(cajaId);
        if (montoReal == null || montoReal.signum() < 0) throw new IllegalArgumentException("Monto real invalido");
        BigDecimal esperado = caja.getMontoInicial().add(movimientos.saldoMovimientos(cajaId));
        caja.setMontoFinalEsperado(esperado); caja.setMontoFinalReal(montoReal);
        caja.setDiferencia(montoReal.subtract(esperado)); caja.setFechaCierre(LocalDateTime.now()); caja.setEstado(Caja.Estado.CERRADA);
        return toDTO(cajas.save(caja));
    }

    @Transactional(readOnly = true)
    public List<MovimientoCajaDTO> movimientos(Long cajaId) {
        return movimientos.findByCajaIdOrderByFechaDesc(cajaId).stream().map(this::movimientoDTO).toList();
    }

    private Caja cajaAbierta(Long id) {
        Caja caja = cajas.findById(id).orElseThrow(() -> new IllegalArgumentException("Caja inexistente"));
        if (caja.getEstado() != Caja.Estado.ABIERTA) throw new IllegalStateException("La caja esta cerrada");
        return caja;
    }
    private CajaDTO toDTO(Caja c) { return new CajaDTO(c.getId(), c.getUsuario().getId(), c.getUsuario().getNombre()+" "+c.getUsuario().getApellido(),
            c.getFechaApertura(), c.getFechaCierre(), c.getMontoInicial(), c.getMontoFinalEsperado(), c.getMontoFinalReal(), c.getDiferencia(), c.getEstado().name()); }
    private MovimientoCajaDTO movimientoDTO(MovimientoCaja m) { return new MovimientoCajaDTO(m.getId(), m.getFecha(), m.getTipo().name(),
            m.getMonto(), m.getDescripcion(), m.getVenta() == null ? null : m.getVenta().getId()); }
}
