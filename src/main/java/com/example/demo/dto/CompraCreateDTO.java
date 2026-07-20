package com.example.demo.dto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.List;
public record CompraCreateDTO(@NotNull Long proveedorId, String numeroComprobante,
                              Long usuarioId, @NotEmpty @Valid List<ItemCompraCreateDTO> items) {}
