package com.example.demo.services;

import com.example.demo.dto.AuthResponseDTO;
import com.example.demo.dto.LoginDTO;
import com.example.demo.dto.RegisterDTO;
import com.example.demo.exceptions.ResourceNotFoundException;
import com.example.demo.models.Persona;
import com.example.demo.repository.PersonaRepository;
import com.example.demo.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    @Autowired
    private PersonaRepository personaRepository;

    @Autowired
    private JwtTokenProvider tokenProvider;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Transactional
    public AuthResponseDTO login(LoginDTO loginDTO) {
        Persona usuario = personaRepository.findByEmail(loginDTO.email())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        if (!usuario.isActivo()) {
            throw new RuntimeException("El usuario está inactivo");
        }

        if (!passwordEncoder.matches(loginDTO.contraseña(), usuario.getContraseña())) {
            throw new RuntimeException("Contraseña incorrecta");
        }

        String token = tokenProvider.generateToken(usuario);
        return AuthResponseDTO.from(token, usuario);
    }

    @Transactional
    public AuthResponseDTO register(RegisterDTO registerDTO) {
        // Validar que el email no exista
        if (personaRepository.existsByEmail(registerDTO.email())) {
            throw new RuntimeException("El email ya está registrado");
        }

        // Validar que el DNI no exista
        if (personaRepository.existsByDni(registerDTO.dni())) {
            throw new RuntimeException("El DNI ya está registrado");
        }

        // Crear nuevo usuario con rol USUARIO por defecto
        Persona nuevoUsuario = Persona.builder()
                .email(registerDTO.email())
                .contraseña(passwordEncoder.encode(registerDTO.contraseña()))
                .nombre(registerDTO.nombre())
                .apellido(registerDTO.apellido())
                .edad(registerDTO.edad())
                .dni(registerDTO.dni())
                .direccion(registerDTO.direccion())
                .rol(Persona.Rol.USUARIO)
                .activo(true)
                .build();

        personaRepository.save(nuevoUsuario);

        String token = tokenProvider.generateToken(nuevoUsuario);
        return AuthResponseDTO.from(token, nuevoUsuario);
    }

    public AuthResponseDTO verifyToken(String token) {
        if (!tokenProvider.validateToken(token)) {
            throw new RuntimeException("Token inválido o expirado");
        }

        String email = tokenProvider.getEmailFromToken(token);
        Persona usuario = personaRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        return AuthResponseDTO.from(token, usuario);
    }
}

