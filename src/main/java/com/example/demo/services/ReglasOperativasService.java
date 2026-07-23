package com.example.demo.services;

import com.example.demo.dto.ReglasOperativasDTO;
import com.example.demo.models.ReglasOperativas;
import com.example.demo.models.Venta;
import com.example.demo.repository.ReglasOperativasRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class ReglasOperativasService {
    private final ReglasOperativasRepository repository;
    private final AuditoriaService auditoria;

    public ReglasOperativasService(ReglasOperativasRepository repository, AuditoriaService auditoria) {
        this.repository = repository;
        this.auditoria = auditoria;
    }

    @Transactional(readOnly = true)
    public ReglasOperativasDTO obtener() {
        return dto(entity());
    }

    @Transactional
    public ReglasOperativasDTO guardar(ReglasOperativasDTO d, Long usuarioId) {
        if (d.descuentoMaximo() == null || d.descuentoMaximo().signum() < 0
                || d.descuentoMaximo().compareTo(BigDecimal.valueOf(100)) > 0)
            throw new IllegalArgumentException("Descuento máximo inválido");
        if (d.limiteCreditoPredeterminado() == null || d.limiteCreditoPredeterminado().signum() < 0)
            throw new IllegalArgumentException("Límite de crédito inválido");
        if (d.margenMinimoPct() == null || d.margenMinimoPct().signum() < 0
                || d.margenMinimoPct().compareTo(BigDecimal.valueOf(100)) > 0)
            throw new IllegalArgumentException("Margen mínimo inválido");
        if (d.diasAlertaVencimiento() < 0) throw new IllegalArgumentException("Días de alerta inválidos");
        if (d.diasMaximosDevolucion() < 0) throw new IllegalArgumentException("Plazo de devolución inválido");
        try {
            Venta.MedioPago.valueOf(d.medioPagoPredeterminado());
        } catch (RuntimeException exception) {
            throw new IllegalArgumentException("Medio de pago predeterminado inválido");
        }

        ReglasOperativas r = entity();
        r.setCajaObligatoria(d.cajaObligatoria());
        r.setRequerirClienteVenta(d.requerirClienteVenta());
        r.setPermitirVentaSinStock(d.permitirVentaSinStock());
        r.setDescuentoMaximo(d.descuentoMaximo());
        r.setPermitirPrecioManual(d.permitirPrecioManual());
        r.setMedioPagoPredeterminado(d.medioPagoPredeterminado());
        r.setFiadoHabilitado(d.fiadoHabilitado());
        r.setLimiteCreditoPredeterminado(d.limiteCreditoPredeterminado());
        r.setAnulacionSoloDueno(d.anulacionSoloDueno());
        r.setDevolucionesHabilitadas(d.devolucionesHabilitadas());
        r.setDiasMaximosDevolucion(d.diasMaximosDevolucion());
        r.setControlarVencimientos(d.controlarVencimientos());
        r.setBloquearVentaVencidos(d.bloquearVentaVencidos());
        r.setDiasAlertaVencimiento(d.diasAlertaVencimiento());
        r.setRedondeoEfectivo(d.redondeoEfectivo());
        r.setMotivoAnulacionObligatorio(d.motivoAnulacionObligatorio());
        r.setMargenMinimoPct(d.margenMinimoPct());
        r.setImprimirTicketAuto(d.imprimirTicketAuto());
        repository.save(r);
        auditoria.registrar(usuarioId, "MODIFICAR", "REGLAS_OPERATIVAS", 1, "Reglas comerciales actualizadas");
        return dto(r);
    }

    private ReglasOperativas entity() {
        return repository.findById(1L).orElseGet(() -> ReglasOperativas.builder().id(1L).cajaObligatoria(true)
                .descuentoMaximo(BigDecimal.valueOf(20)).medioPagoPredeterminado("EFECTIVO")
                .limiteCreditoPredeterminado(BigDecimal.ZERO).anulacionSoloDueno(true)
                .devolucionesHabilitadas(true).diasMaximosDevolucion(30).controlarVencimientos(true)
                .bloquearVentaVencidos(true).diasAlertaVencimiento(30).redondeoEfectivo(false)
                .motivoAnulacionObligatorio(false).margenMinimoPct(BigDecimal.ZERO).imprimirTicketAuto(true).build());
    }

    private ReglasOperativasDTO dto(ReglasOperativas r) {
        return new ReglasOperativasDTO(r.isCajaObligatoria(), r.isRequerirClienteVenta(),
                r.isPermitirVentaSinStock(), r.getDescuentoMaximo(), r.isPermitirPrecioManual(),
                r.getMedioPagoPredeterminado(), r.isFiadoHabilitado(), r.getLimiteCreditoPredeterminado(),
                r.isAnulacionSoloDueno(), r.isDevolucionesHabilitadas(), r.getDiasMaximosDevolucion(),
                r.isControlarVencimientos(), r.isBloquearVentaVencidos(), r.getDiasAlertaVencimiento(),
                r.isRedondeoEfectivo(), r.isMotivoAnulacionObligatorio(),
                r.getMargenMinimoPct() == null ? BigDecimal.ZERO : r.getMargenMinimoPct(),
                r.isImprimirTicketAuto());
    }
}
