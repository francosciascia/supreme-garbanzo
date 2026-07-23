package com.example.demo.services;

import com.example.demo.dto.AuditoriaDTO;
import com.example.demo.models.Auditoria;
import com.example.demo.repository.AuditoriaRepository;
import com.example.demo.repository.PersonaRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class AuditoriaService {
    private final AuditoriaRepository repository;
    private final PersonaRepository personas;

    public AuditoriaService(AuditoriaRepository repository, PersonaRepository personas) {
        this.repository = repository;
        this.personas = personas;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void registrar(Long usuarioId, String accion, String entidad, Object entidadId, String detalle) {
        repository.save(Auditoria.builder()
                .fecha(LocalDateTime.now())
                .usuario(usuarioId == null ? null : personas.findById(usuarioId).orElse(null))
                .accion(accion)
                .entidad(entidad)
                .entidadId(entidadId == null ? null : String.valueOf(entidadId))
                .detalle(detalle)
                .build());
    }

    @Transactional(readOnly = true)
    public List<AuditoriaDTO> listar() {
        return repository.findAllByOrderByFechaDesc().stream().map(this::dto).toList();
    }

    @Transactional(readOnly = true)
    public Page<AuditoriaDTO> listar(Pageable pageable) {
        return repository.findAll(pageable).map(this::dto);
    }

    @Transactional(readOnly = true)
    public Page<AuditoriaDTO> buscar(LocalDate desde, LocalDate hasta, String entidad, String accion,
                                     Long usuarioId, Pageable pageable) {
        Specification<Auditoria> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (desde != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("fecha"), desde.atStartOfDay()));
            }
            if (hasta != null) {
                predicates.add(cb.lessThan(root.get("fecha"), hasta.plusDays(1).atStartOfDay()));
            }
            String entidadFiltro = blankToNull(entidad);
            if (entidadFiltro != null) {
                predicates.add(cb.equal(cb.upper(root.get("entidad")), entidadFiltro.toUpperCase()));
            }
            String accionFiltro = blankToNull(accion);
            if (accionFiltro != null) {
                predicates.add(cb.equal(cb.upper(root.get("accion")), accionFiltro.toUpperCase()));
            }
            if (usuarioId != null) {
                predicates.add(cb.equal(root.get("usuario").get("id"), usuarioId));
            }
            return cb.and(predicates.toArray(Predicate[]::new));
        };
        return repository.findAll(spec, pageable).map(this::dto);
    }

    @Transactional(readOnly = true)
    public long cantidad() {
        return repository.count();
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private AuditoriaDTO dto(Auditoria a) {
        return new AuditoriaDTO(
                a.getId(),
                a.getFecha(),
                a.getUsuario() == null ? "Sistema" : a.getUsuario().getNombre() + " " + a.getUsuario().getApellido(),
                a.getAccion(),
                a.getEntidad(),
                a.getEntidadId(),
                a.getDetalle());
    }
}
