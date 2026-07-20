package com.example.demo.repository;
import com.example.demo.models.MovimientoStock;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface MovimientoStockRepository extends JpaRepository<MovimientoStock, Long> {
    List<MovimientoStock> findTop100ByOrderByFechaDesc();
    List<MovimientoStock> findByProductoIdOrderByFechaDesc(Long productoId);
}
