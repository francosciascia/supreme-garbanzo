package com.example.demo.dto;

import java.math.BigDecimal;

public record ReglasOperativasDTO(
        boolean cajaObligatoria,
        boolean requerirClienteVenta,
        boolean permitirVentaSinStock,
        BigDecimal descuentoMaximo,
        boolean permitirPrecioManual,
        String medioPagoPredeterminado,
        boolean fiadoHabilitado,
        BigDecimal limiteCreditoPredeterminado,
        boolean anulacionSoloDueno,
        boolean devolucionesHabilitadas,
        int diasMaximosDevolucion,
        boolean controlarVencimientos,
        boolean bloquearVentaVencidos,
        int diasAlertaVencimiento,
        boolean redondeoEfectivo,
        boolean motivoAnulacionObligatorio,
        BigDecimal margenMinimoPct,
        boolean imprimirTicketAuto
) {
    public ReglasOperativasDTO(
            boolean cajaObligatoria,
            boolean requerirClienteVenta,
            boolean permitirVentaSinStock,
            BigDecimal descuentoMaximo,
            boolean permitirPrecioManual,
            String medioPagoPredeterminado,
            boolean fiadoHabilitado,
            BigDecimal limiteCreditoPredeterminado,
            boolean anulacionSoloDueno,
            boolean devolucionesHabilitadas,
            int diasMaximosDevolucion,
            boolean controlarVencimientos,
            boolean bloquearVentaVencidos,
            int diasAlertaVencimiento) {
        this(cajaObligatoria, requerirClienteVenta, permitirVentaSinStock, descuentoMaximo, permitirPrecioManual,
                medioPagoPredeterminado, fiadoHabilitado, limiteCreditoPredeterminado, anulacionSoloDueno,
                devolucionesHabilitadas, diasMaximosDevolucion, controlarVencimientos, bloquearVentaVencidos,
                diasAlertaVencimiento, false, false, BigDecimal.ZERO, true);
    }

    public ReglasOperativasDTO(
            boolean cajaObligatoria,
            boolean permitirVentaSinStock,
            BigDecimal descuentoMaximo,
            boolean fiadoHabilitado,
            BigDecimal limiteCreditoPredeterminado,
            boolean anulacionSoloDueno,
            int diasAlertaVencimiento,
            boolean permitirPrecioManual) {
        this(cajaObligatoria, false, permitirVentaSinStock, descuentoMaximo, permitirPrecioManual, "EFECTIVO",
                fiadoHabilitado, limiteCreditoPredeterminado, anulacionSoloDueno, true, 30, true, true,
                diasAlertaVencimiento, false, false, BigDecimal.ZERO, true);
    }
}
