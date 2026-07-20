package com.example.demo.repository;

import com.example.demo.models.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import jakarta.persistence.LockModeType;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long> {
    Optional<Producto> findByNombre(String nombre);
    Optional<Producto> findByCodigoBarras(String codigoBarras);
    List<Producto> findByCategoria_Id(Long categoriaId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Producto p where p.id = :id")
    Optional<Producto> findByIdForUpdate(@Param("id") Long id);

    @Query("""
            select p from Producto p
            where (:categoriaId is null or p.categoria.id = :categoriaId)
              and (:search = '' or lower(p.nombre) like lower(concat('%', :search, '%'))
                   or lower(coalesce(p.descripcion, '')) like lower(concat('%', :search, '%'))
                   or lower(coalesce(p.codigoBarras, '')) like lower(concat('%', :search, '%'))
                   or lower(coalesce(p.marca, '')) like lower(concat('%', :search, '%')))
            """)
    Page<Producto> buscar(@Param("search") String search, @Param("categoriaId") Long categoriaId,
                          Pageable pageable);
}
