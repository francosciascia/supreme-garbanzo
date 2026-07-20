package com.example.demo.repository;
import com.example.demo.models.Compra;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface CompraRepository extends JpaRepository<Compra, Long> {
    List<Compra> findAllByOrderByFechaDesc();
}
