package com.example.demo.config;

import com.example.demo.models.Persona;
import com.example.demo.repository.PersonaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private PersonaRepository personaRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // Crear solo si no existen usuarios
        if (personaRepository.count() == 0) {
            // Usuario Común
            Persona usuario = Persona.builder()
                    .email("usuario@demo.com")
                    .contraseña(passwordEncoder.encode("123456"))
                    .nombre("Juan")
                    .apellido("Pérez")
                    .edad(25)
                    .dni(12345678)
                    .direccion("Calle 1, Apto 101")
                    .rol(Persona.Rol.USUARIO)
                    .activo(true)
                    .build();

            // Admin
            Persona admin = Persona.builder()
                    .email("admin@demo.com")
                    .contraseña(passwordEncoder.encode("123456"))
                    .nombre("Carlos")
                    .apellido("García")
                    .edad(35)
                    .dni(87654321)
                    .direccion("Avenida Principal, Piso 5")
                    .rol(Persona.Rol.ADMIN)
                    .activo(true)
                    .build();

            // Super Admin
            Persona superAdmin = Persona.builder()
                    .email("superadmin@demo.com")
                    .contraseña(passwordEncoder.encode("123456"))
                    .nombre("Franco")
                    .apellido("Sciascia")
                    .edad(30)
                    .dni(11111111)
                    .direccion("Boulevard Central, Torre A")
                    .rol(Persona.Rol.SUPER_ADMIN)
                    .activo(true)
                    .build();

            personaRepository.save(usuario);
            personaRepository.save(admin);
            personaRepository.save(superAdmin);

            System.out.println("✅ Datos de demostración creados exitosamente");
        }
    }
}

