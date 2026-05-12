package com.example.demo.services;

import com.example.demo.dto.AuthResponseDTO;
import com.example.demo.dto.LoginDTO;
import com.example.demo.dto.RegisterDTO;
import com.example.demo.exceptions.ConflictException;
import com.example.demo.exceptions.UnauthorizedException;
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
        // Evitamos filtrar si el usuario existe o no: mismo mensaje genérico.
        Persona usuario = personaRepository.findByEmail(loginDTO.email())
                .orElseThrow(() -> new UnauthorizedException("Credenciales inválidas"));

        if (!usuario.isActivo()) {
            throw new UnauthorizedException("El usuario está inactivo");
        }

        if (!passwordEncoder.matches(loginDTO.contraseña(), usuario.getContraseña())) {
            throw new UnauthorizedException("Credenciales inválidas");
        }

        String token = tokenProvider.generateToken(usuario);
        return AuthResponseDTO.from(token, usuario);
    }

    @Transactional
    public AuthResponseDTO register(RegisterDTO registerDTO) {
        if (personaRepository.existsByEmail(registerDTO.email())) {
            throw new ConflictException("El email ya está registrado");
        }

        if (personaRepository.existsByDni(registerDTO.dni())) {
            throw new ConflictException("El DNI ya está registrado");
        }

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
            throw new UnauthorizedException("Token inválido o expirado");
        }

        String email = tokenProvider.getEmailFromToken(token);
        Persona usuario = personaRepository.findByEmail(email)
                .orElseThrow(() -> new UnauthorizedException("Usuario no encontrado"));

        return AuthResponseDTO.from(token, usuario);
    }
}
