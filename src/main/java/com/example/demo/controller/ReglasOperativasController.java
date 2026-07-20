package com.example.demo.controller;

import com.example.demo.dto.ReglasOperativasDTO;
import com.example.demo.services.ReglasOperativasService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reglas")
public class ReglasOperativasController {
    private final ReglasOperativasService service;

    public ReglasOperativasController(ReglasOperativasService service) {
        this.service = service;
    }

    @GetMapping
    public ReglasOperativasDTO obtener() {
        return service.obtener();
    }

    @PutMapping
    public ReglasOperativasDTO guardar(@RequestBody ReglasOperativasDTO dto,
                                        @RequestParam(required = false) Long usuarioId) {
        return service.guardar(dto, usuarioId);
    }
}
