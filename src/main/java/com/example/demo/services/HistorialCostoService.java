package com.example.demo.services;

import com.example.demo.dto.HistorialCostoDTO;
import com.example.demo.models.HistorialCostoProducto;
import com.example.demo.models.Persona;
import com.example.demo.models.Producto;
import com.example.demo.repository.HistorialCostoProductoRepository;
import com.example.demo.repository.PersonaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class HistorialCostoService {
    private final HistorialCostoProductoRepository repository;
    private final PersonaRepository personas;

    public HistorialCostoService(HistorialCostoProductoRepository repository, PersonaRepository personas) {
        this.repository = repository;
        this.personas = personas;
    }

    @Transactional
    public void registrar(Producto producto, BigDecimal anterior, BigDecimal nuevo, String origen,
                          String referencia, Long usuarioId) {
        if (producto == null || nuevo == null) return;
        if (anterior != null && anterior.compareTo(nuevo) == 0) return;
        Persona usuario = usuarioId == null ? null : personas.findById(usuarioId).orElse(null);
        repository.save(HistorialCostoProducto.builder()
                .producto(producto)
                .fecha(LocalDateTime.now())
                .costoAnterior(anterior)
                .costoNuevo(nuevo)
                .origen(origen)
                .referencia(referencia)
                .usuario(usuario)
                .build());
    }

    @Transactional(readOnly = true)
    public List<HistorialCostoDTO> porProducto(Long productoId) {
        return repository.findByProductoIdOrderByFechaDesc(productoId).stream()
                .map(h -> new HistorialCostoDTO(h.getId(), h.getProducto().getId(), h.getProducto().getNombre(),
                        h.getFecha(), h.getCostoAnterior(), h.getCostoNuevo(), h.getOrigen(), h.getReferencia()))
                .toList();
    }
}
