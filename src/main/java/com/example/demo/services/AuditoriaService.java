package com.example.demo.services;

import com.example.demo.dto.AuditoriaDTO;
import com.example.demo.models.Auditoria;
import com.example.demo.repository.AuditoriaRepository;
import com.example.demo.repository.PersonaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
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
        LocalDateTime from = desde == null ? null : desde.atStartOfDay();
        LocalDateTime to = hasta == null ? null : hasta.plusDays(1).atStartOfDay();
        return repository.buscar(from, to, blankToNull(entidad), blankToNull(accion), usuarioId, pageable).map(this::dto);
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
