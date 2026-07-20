package com.example.demo.dto;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;
public record ItemCompraCreateDTO(@NotNull Long productoId, @Min(1) int cantidad,
                                  @NotNull @DecimalMin("0.01") BigDecimal costoUnitario,
                                  LocalDate fechaVencimiento, String codigoLote) {
    public ItemCompraCreateDTO(Long productoId, int cantidad, BigDecimal costoUnitario, LocalDate fechaVencimiento) {
        this(productoId, cantidad, costoUnitario, fechaVencimiento, null);
    }
}
