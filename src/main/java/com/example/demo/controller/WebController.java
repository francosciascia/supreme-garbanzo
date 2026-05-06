package com.example.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WebController {

    /**
     * Sirve la página principal del frontend
     */
    @GetMapping("/")
    public String index() {
        return "forward:/index.html";
    }

    /**
     * Redirige /home a la página principal
     */
    @GetMapping("/home")
    public String home() {
        return "forward:/index.html";
    }
}
