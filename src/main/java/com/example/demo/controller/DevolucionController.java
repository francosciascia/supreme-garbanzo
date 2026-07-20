package com.example.demo.controller;

import com.example.demo.dto.DevolucionCreateDTO;
import com.example.demo.dto.DevolucionDTO;
import com.example.demo.services.DevolucionService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/devoluciones")
public class DevolucionController {
    private final DevolucionService service;

    public DevolucionController(DevolucionService service) {
        this.service = service;
    }

    @GetMapping
    public List<DevolucionDTO> listar() {
        return service.listar();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DevolucionDTO crear(@RequestBody DevolucionCreateDTO dto) {
        return service.crear(dto);
    }
}
