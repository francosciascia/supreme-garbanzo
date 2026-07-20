package com.example.demo.controller;
import com.example.demo.dto.ProveedorDTO;
import com.example.demo.services.ProveedorService;
import org.springframework.web.bind.annotation.*;
import java.util.List;
@RestController @RequestMapping("/api/proveedores")
public class ProveedorController {
    private final ProveedorService service;
    public ProveedorController(ProveedorService service) { this.service = service; }
    @GetMapping public List<ProveedorDTO> listar() { return service.listar(); }
    @PostMapping public ProveedorDTO crear(@RequestBody ProveedorDTO dto) { return service.guardar(null, dto); }
    @PutMapping("/{id}") public ProveedorDTO editar(@PathVariable Long id, @RequestBody ProveedorDTO dto) { return service.guardar(id, dto); }
    @DeleteMapping("/{id}") public void eliminar(@PathVariable Long id) { service.eliminar(id); }
}
