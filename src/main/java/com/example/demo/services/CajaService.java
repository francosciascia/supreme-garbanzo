package com.example.demo.services;

import com.example.demo.dto.CajaDTO;
import com.example.demo.dto.CierreCajaResumenDTO;
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
    private final AuditoriaService auditoria;

    public CajaService(CajaRepository cajas, MovimientoCajaRepository movimientos, PersonaRepository personas,
                       AuditoriaService auditoria) {
        this.cajas = cajas;
        this.movimientos = movimientos;
        this.personas = personas;
        this.auditoria = auditoria;
    }

    @Transactional
    public CajaDTO abrir(Long usuarioId, BigDecimal montoInicial) {
        if (montoInicial == null || montoInicial.signum() < 0) throw new IllegalArgumentException("Monto inicial invalido");
        if (cajas.findFirstByUsuarioIdAndEstadoOrderByFechaAperturaDesc(usuarioId, Caja.Estado.ABIERTA).isPresent())
            throw new IllegalStateException("El usuario ya tiene una caja abierta");
        Persona usuario = personas.findById(usuarioId).orElseThrow(() -> new IllegalArgumentException("Usuario inexistente"));
        CajaDTO abierta = toDTO(cajas.save(Caja.builder().usuario(usuario).fechaApertura(LocalDateTime.now())
                .montoInicial(montoInicial).estado(Caja.Estado.ABIERTA).build()));
        auditoria.registrar(usuarioId, "CREAR", "CAJA", abierta.id(), "Apertura con inicial " + montoInicial);
        return abierta;
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

    @Transactional(readOnly = true)
    public CierreCajaResumenDTO resumenCierre(Long cajaId) {
        Caja caja = cajaAbierta(cajaId);
        List<MovimientoCajaDTO> movs = movimientos(cajaId);
        BigDecimal ventas = sum(movs, MovimientoCaja.Tipo.VENTA.name());
        BigDecimal ingresos = sum(movs, MovimientoCaja.Tipo.INGRESO.name());
        BigDecimal retiros = sum(movs, MovimientoCaja.Tipo.RETIRO.name());
        BigDecimal anulaciones = sum(movs, MovimientoCaja.Tipo.ANULACION.name());
        BigDecimal esperado = caja.getMontoInicial().add(movimientos.saldoMovimientos(cajaId));
        return new CierreCajaResumenDTO(caja.getId(), caja.getMontoInicial(), ventas, ingresos, retiros, anulaciones,
                esperado, null, null, movs);
    }

    @Transactional
    public CierreCajaResumenDTO cerrar(Long cajaId, BigDecimal montoReal, Long usuarioId) {
        Caja caja = cajaAbierta(cajaId);
        if (montoReal == null || montoReal.signum() < 0) throw new IllegalArgumentException("Monto real invalido");
        CierreCajaResumenDTO previo = resumenCierre(cajaId);
        BigDecimal esperado = previo.esperado();
        caja.setMontoFinalEsperado(esperado);
        caja.setMontoFinalReal(montoReal);
        caja.setDiferencia(montoReal.subtract(esperado));
        caja.setFechaCierre(LocalDateTime.now());
        caja.setEstado(Caja.Estado.CERRADA);
        cajas.save(caja);
        auditoria.registrar(usuarioId, "CERRAR", "CAJA", cajaId,
                "Esperado " + esperado + " · Contado " + montoReal + " · Diferencia " + caja.getDiferencia());
        return new CierreCajaResumenDTO(caja.getId(), caja.getMontoInicial(), previo.totalVentas(), previo.totalIngresos(),
                previo.totalRetiros(), previo.totalAnulaciones(), esperado, montoReal, caja.getDiferencia(), previo.movimientos());
    }

    /** @deprecated usar {@link #cerrar(Long, BigDecimal, Long)} */
    @Transactional
    public CajaDTO cerrar(Long cajaId, BigDecimal montoReal) {
        CierreCajaResumenDTO resumen = cerrar(cajaId, montoReal, null);
        return new CajaDTO(resumen.cajaId(), null, null, null, LocalDateTime.now(), resumen.montoInicial(),
                resumen.esperado(), resumen.real(), resumen.diferencia(), "CERRADA");
    }

    @Transactional(readOnly = true)
    public List<MovimientoCajaDTO> movimientos(Long cajaId) {
        return movimientos.findByCajaIdOrderByFechaDesc(cajaId).stream().map(this::movimientoDTO).toList();
    }

    @Transactional(readOnly = true)
    public List<CajaDTO> historialCerradas() {
        return cajas.findByEstadoOrderByFechaCierreDesc(Caja.Estado.CERRADA).stream().map(this::toDTO).toList();
    }

    private BigDecimal sum(List<MovimientoCajaDTO> movs, String tipo) {
        return movs.stream().filter(m -> tipo.equals(m.tipo())).map(MovimientoCajaDTO::monto)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private Caja cajaAbierta(Long id) {
        Caja caja = cajas.findById(id).orElseThrow(() -> new IllegalArgumentException("Caja inexistente"));
        if (caja.getEstado() != Caja.Estado.ABIERTA) throw new IllegalStateException("La caja esta cerrada");
        return caja;
    }

    private CajaDTO toDTO(Caja c) {
        return new CajaDTO(c.getId(), c.getUsuario().getId(), c.getUsuario().getNombre() + " " + c.getUsuario().getApellido(),
                c.getFechaApertura(), c.getFechaCierre(), c.getMontoInicial(), c.getMontoFinalEsperado(),
                c.getMontoFinalReal(), c.getDiferencia(), c.getEstado().name());
    }

    private MovimientoCajaDTO movimientoDTO(MovimientoCaja m) {
        return new MovimientoCajaDTO(m.getId(), m.getFecha(), m.getTipo().name(),
                m.getMonto(), m.getDescripcion(), m.getVenta() == null ? null : m.getVenta().getId());
    }
}
