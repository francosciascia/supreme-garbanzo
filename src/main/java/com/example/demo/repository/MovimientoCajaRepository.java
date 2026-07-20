package com.example.demo.repository;
import com.example.demo.models.MovimientoCaja;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.math.BigDecimal;
import java.util.List;
public interface MovimientoCajaRepository extends JpaRepository<MovimientoCaja, Long> {
    List<MovimientoCaja> findByCajaIdOrderByFechaDesc(Long cajaId);
    @Query("select coalesce(sum(case when m.tipo in ('VENTA','INGRESO') then m.monto else -m.monto end), 0) from MovimientoCaja m where m.caja.id = :cajaId")
    BigDecimal saldoMovimientos(Long cajaId);
}
