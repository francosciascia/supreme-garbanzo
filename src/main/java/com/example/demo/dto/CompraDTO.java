package com.example.demo.dto;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
public record CompraDTO(Long id, LocalDateTime fecha, String numeroComprobante, BigDecimal total,
                        String estado, ProveedorDTO proveedor, List<ItemDTO> items) {
    public record ItemDTO(Long productoId, String producto, int cantidad, BigDecimal costoUnitario,
                          BigDecimal subtotal, String codigoLote, java.time.LocalDate fechaVencimiento) {}
}
