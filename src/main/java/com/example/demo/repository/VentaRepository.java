package com.example.demo.repository;

import com.example.demo.models.Venta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface VentaRepository extends JpaRepository<Venta, Long>, JpaSpecificationExecutor<Venta> {
    @Query("select distinct v from Venta v left join fetch v.items i left join fetch i.producto "
            + "where v.estado = com.example.demo.models.Venta.Estado.CONFIRMADA and v.fecha >= :desde and v.fecha < :hasta")
    List<Venta> confirmadasEntre(@Param("desde") LocalDateTime desde, @Param("hasta") LocalDateTime hasta);
}
