package com.example.demo.services;

import com.example.demo.dto.ProveedorDTO;
import com.example.demo.models.Proveedor;
import com.example.demo.repository.ProveedorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class ProveedorService {
    private final ProveedorRepository repository;
    public ProveedorService(ProveedorRepository repository) { this.repository = repository; }
    @Transactional(readOnly = true) public List<ProveedorDTO> listar() { return repository.findAllByOrderByNombreAsc().stream().map(this::dto).toList(); }
    @Transactional public ProveedorDTO guardar(Long id, ProveedorDTO value) {
        Proveedor p = id == null ? new Proveedor() : repository.findById(id).orElseThrow(() -> new IllegalArgumentException("Proveedor inexistente"));
        if (value.nombre() == null || value.nombre().isBlank()) throw new IllegalArgumentException("El nombre es obligatorio");
        p.setNombre(value.nombre().trim()); p.setCuit(limpiar(value.cuit())); p.setTelefono(limpiar(value.telefono()));
        p.setEmail(limpiar(value.email())); p.setDireccion(limpiar(value.direccion())); p.setActivo(value.activo());
        return dto(repository.save(p));
    }
    @Transactional public void eliminar(Long id) { Proveedor p = repository.findById(id).orElseThrow(() -> new IllegalArgumentException("Proveedor inexistente")); p.setActivo(false); }
    private ProveedorDTO dto(Proveedor p) { return new ProveedorDTO(p.getId(), p.getNombre(), p.getCuit(), p.getTelefono(), p.getEmail(), p.getDireccion(), p.isActivo()); }
    private String limpiar(String v) { return v == null || v.isBlank() ? null : v.trim(); }
}
