package com.example.demo.dto;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record VentaCreateDTO(

        @NotEmpty(message = "La venta debe tener al menos un item")
        List<ItemVentaCreateDTO> items
) {
}