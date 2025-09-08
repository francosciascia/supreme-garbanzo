package com.example.demo.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record VentaCreateDTO(
        @NotEmpty List<@Valid ItemVentaCreateDTO> items
) {}