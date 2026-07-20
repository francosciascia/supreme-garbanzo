package com.example.demo.repository;
import com.example.demo.models.LoteProducto;import jakarta.persistence.LockModeType;import org.springframework.data.jpa.repository.*;import org.springframework.data.repository.query.Param;import java.time.LocalDate;import java.util.List;
public interface LoteProductoRepository extends JpaRepository<LoteProducto,Long>{
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select l from LoteProducto l where l.producto.id=:productoId and l.activo=true and l.cantidadDisponible>0 and (l.fechaVencimiento is null or l.fechaVencimiento>=current_date) order by case when l.fechaVencimiento is null then 1 else 0 end, l.fechaVencimiento, l.fechaIngreso, l.id")
    List<LoteProducto> disponiblesFefo(@Param("productoId")Long productoId);
    @Query("select coalesce(sum(l.cantidadDisponible),0) from LoteProducto l where l.producto.id=:productoId and l.activo=true")
    Long cantidadTrazada(@Param("productoId")Long productoId);
    @Query("select coalesce(sum(l.cantidadDisponible),0) from LoteProducto l where l.producto.id=:productoId and l.activo=true and (l.fechaVencimiento is null or l.fechaVencimiento>=current_date)")
    Long cantidadVendible(@Param("productoId")Long productoId);
    List<LoteProducto> findByProductoIdOrderByFechaVencimientoAsc(Long productoId);
    List<LoteProducto> findByCompraId(Long compraId);
    List<LoteProducto> findByActivoTrueAndCantidadDisponibleGreaterThanAndFechaVencimientoBetweenOrderByFechaVencimientoAsc(Integer cantidad,LocalDate desde,LocalDate hasta);
}
