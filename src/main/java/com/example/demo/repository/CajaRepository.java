package com.example.demo.repository;
import com.example.demo.models.Caja;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
public interface CajaRepository extends JpaRepository<Caja, Long> {
    Optional<Caja> findFirstByUsuarioIdAndEstadoOrderByFechaAperturaDesc(Long usuarioId, Caja.Estado estado);
    List<Caja> findByEstado(Caja.Estado estado);
}
