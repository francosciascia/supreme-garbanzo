package com.example.demo.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import java.math.BigDecimal;

public record VentaCreateDTO(

        Long clienteId,

        @NotEmpty(message = "La venta debe tener al menos un item")
        @Valid
        List<ItemVentaCreateDTO> items,
        String medioPago,
        BigDecimal montoRecibido,
        BigDecimal descuento,
        Long cajaId,
        Long usuarioId
) {
    public VentaCreateDTO(Long clienteId, List<ItemVentaCreateDTO> items) {
        this(clienteId, items, "EFECTIVO", null, BigDecimal.ZERO, null, null);
    }
}
