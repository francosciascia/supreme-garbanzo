package com.example.demo.repository;

import com.example.demo.models.Auditoria;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface AuditoriaRepository extends JpaRepository<Auditoria, Long> {
    List<Auditoria> findAllByOrderByFechaDesc();

    @Query("""
            select a from Auditoria a
            where (:desde is null or a.fecha >= :desde)
              and (:hasta is null or a.fecha < :hasta)
              and (:entidad is null or :entidad = '' or upper(a.entidad) = upper(:entidad))
              and (:accion is null or :accion = '' or upper(a.accion) = upper(:accion))
              and (:usuarioId is null or a.usuario.id = :usuarioId)
            order by a.fecha desc
            """)
    Page<Auditoria> buscar(@Param("desde") LocalDateTime desde,
                           @Param("hasta") LocalDateTime hasta,
                           @Param("entidad") String entidad,
                           @Param("accion") String accion,
                           @Param("usuarioId") Long usuarioId,
                           Pageable pageable);
}
