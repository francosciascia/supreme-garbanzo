package com.example.demo.dto;
import java.time.LocalDateTime;
public record MovimientoStockDTO(Long id, LocalDateTime fecha, Long productoId, String producto,
                                 String tipo, Integer cantidad, Integer stockAnterior, Integer stockNuevo,
                                 String referencia, String descripcion, String usuario) {}
