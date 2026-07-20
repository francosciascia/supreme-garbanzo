package com.example.demo.controller;

import com.example.demo.dto.EmpleadoDTO;
import com.example.demo.services.EmpleadoService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/empleados")
public class EmpleadoController {
    private final EmpleadoService service;

    public EmpleadoController(EmpleadoService service) {
        this.service = service;
    }

    @GetMapping
    public List<EmpleadoDTO> listar() {
        return service.listar();
    }

    @PutMapping
    public EmpleadoDTO guardar(@RequestBody EmpleadoDTO dto,
                               @RequestParam(required = false) Long operadorId) {
        return service.guardar(dto, operadorId);
    }
}
