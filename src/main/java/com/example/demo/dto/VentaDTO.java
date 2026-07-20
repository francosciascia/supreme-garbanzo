package com.example.demo.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record VentaDTO(
        Long id,
        LocalDate fecha,
        BigDecimal total,
        ClienteResumenDTO cliente,
        List<ItemVentaDTO> items,
        String estado,
        String medioPago,
        BigDecimal montoRecibido,
        BigDecimal vuelto,
        BigDecimal descuento,
        String numeroComprobante
) {
    /**
     * Resumen mínimo del cliente embebido en una venta, para no traer
     * todos los campos.
     */
    public record ClienteResumenDTO(
            Long id,
            String nombre,
            String apellido,
            Integer dni
    ) {}
}
