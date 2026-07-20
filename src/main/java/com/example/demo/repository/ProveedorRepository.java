package com.example.demo.repository;
import com.example.demo.models.Proveedor;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface ProveedorRepository extends JpaRepository<Proveedor, Long> {
    List<Proveedor> findAllByOrderByNombreAsc();
}
