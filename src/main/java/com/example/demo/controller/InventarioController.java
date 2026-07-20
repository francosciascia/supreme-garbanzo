package com.example.demo.controller;
import com.example.demo.dto.MovimientoStockDTO;
import com.example.demo.services.InventarioService;
import org.springframework.web.bind.annotation.*;
import java.util.List;
@RestController @RequestMapping("/api/inventario")
public class InventarioController{
    private final InventarioService service;public InventarioController(InventarioService service){this.service=service;}
    @GetMapping("/movimientos") public List<MovimientoStockDTO> listar(){return service.listar();}
    @PostMapping("/ajustar") public MovimientoStockDTO ajustar(@RequestBody AjusteRequest r){return service.ajustar(r.productoId(),r.stockNuevo(),r.motivo(),r.usuarioId());}
    public record AjusteRequest(Long productoId,int stockNuevo,String motivo,Long usuarioId){}
}
