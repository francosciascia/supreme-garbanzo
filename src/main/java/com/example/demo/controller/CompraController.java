package com.example.demo.controller;
import com.example.demo.dto.*;
import com.example.demo.services.CompraService;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.util.List;
@RestController @RequestMapping("/api/compras")
public class CompraController {
    private final CompraService service;
    public CompraController(CompraService service) { this.service = service; }
    @GetMapping public List<CompraDTO> listar() { return service.listar(); }
    @PostMapping public ResponseEntity<CompraDTO> crear(@Valid @RequestBody CompraCreateDTO dto) { return ResponseEntity.status(HttpStatus.CREATED).body(service.crear(dto)); }
    @PostMapping("/{id}/anular") public CompraDTO anular(@PathVariable Long id) { return service.anular(id); }
}
