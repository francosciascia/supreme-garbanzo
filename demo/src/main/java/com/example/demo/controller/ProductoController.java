package com.example.demo.controller;

import com.example.demo.models.Producto;
import com.example.demo.services.ProductoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/productos")
public class ProductoController {
    private final ProductoService productoService;

    public ProductoController(ProductoService productoService){
        this.productoService = productoService;
    }

    @GetMapping
    public List<Producto> listar(){
        return productoService.listar();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Producto> detalle(@PathVariable Long id){
        return productoService.detalle(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id){
        productoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping
    public ResponseEntity<Producto> crear(@RequestBody Producto producto){
        Producto nuevo = productoService.crear(producto);
        return ResponseEntity.ok(nuevo);

    }

    @PutMapping("/{id}")
    public ResponseEntity<Producto> actualizar (@PathVariable Long id, @RequestBody Producto producto){
        try{
            Producto actualizado = productoService.actualizar(id,producto);
            return ResponseEntity.ok(actualizado);
        } catch (IllegalArgumentException e){
            return ResponseEntity.notFound().build();
        }
    }
}
