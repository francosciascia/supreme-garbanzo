package com.example.demo.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record VentaCreateDTO(

        Long clienteId,

        @NotEmpty(message = "La venta debe tener al menos un item")
        @Valid
        List<ItemVentaCreateDTO> items
) { }
