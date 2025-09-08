package com.example.demo.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record VentaDTO(
        long id,
        LocalDate fecha,
        BigDecimal total,
        List<ItemVentaDTO> items
) {}