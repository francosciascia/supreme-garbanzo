package com.example.demo.repository;

import com.example.demo.models.HistorialCostoProducto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HistorialCostoProductoRepository extends JpaRepository<HistorialCostoProducto, Long> {
    List<HistorialCostoProducto> findByProductoIdOrderByFechaDesc(Long productoId);
}
