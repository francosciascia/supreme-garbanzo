package com.example.demo.controller;

import com.example.demo.models.ItemVenta;
import com.example.demo.models.Producto;
import com.example.demo.models.Venta;
import com.example.demo.services.VentaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/ventas")
public class VentaController {

    private final VentaService ventaService;

    public VentaController(VentaService ventaService) {
        this.ventaService = ventaService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<Venta> detalle(@PathVariable Long id){
        Venta venta = ventaService.detalle(id).orElse(null);
        return ResponseEntity.ok(venta);
    }

    @GetMapping
    public List<Venta> listar(){
        return ventaService.listar();
    }

    @PostMapping
    public Venta crear(@RequestBody Venta venta){
        return ventaService.crear(venta);
    }

    @DeleteMapping("/{ventaId}/items/{itemId}")
    public void eliminar(@PathVariable Long ventaId, @PathVariable Long itemId){
        ventaService.quitarItem(ventaId, itemId);
    }

    @PostMapping("/{id}/items")
    public Venta agregarItem(@PathVariable Long id, @RequestBody ItemVenta itemVenta){
        return ventaService.agregarItem(id, itemVenta);
    }
}