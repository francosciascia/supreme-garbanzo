package com.example.demo.dto;

public record AlertaOperativaDTO(
        String tipo,
        String severidad,
        String mensaje,
        String enlace
) {}
