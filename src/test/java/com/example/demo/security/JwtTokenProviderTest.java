package com.example.demo.security;

import com.example.demo.models.Persona;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class JwtTokenProviderTest {
    private JwtTokenProvider provider;

    @BeforeEach
    void setUp() {
        provider = new JwtTokenProvider();
        ReflectionTestUtils.setField(provider, "jwtSecret",
                "clave-de-prueba-segura-con-mas-de-treinta-y-dos-caracteres");
        ReflectionTestUtils.setField(provider, "jwtExpirationMs", 60_000L);
    }

    @Test
    void generaTokenConEmailYRol() {
        Persona user = Persona.builder().id(7L).email("admin@test.com").nombre("Ada").apellido("Lovelace")
                .rol(Persona.Rol.ADMIN).activo(true).build();

        String token = provider.generateToken(user);

        assertTrue(provider.validateToken(token));
        assertEquals("admin@test.com", provider.getEmailFromToken(token));
        assertEquals("ADMIN", Jwts.parser().verifyWith(provider.getSigningKey()).build()
                .parseSignedClaims(token).getPayload().get("rol", String.class));
    }

    @Test
    void rechazaTokenAlterado() {
        Persona user = Persona.builder().id(7L).email("user@test.com").nombre("Test").apellido("User")
                .rol(Persona.Rol.USUARIO).activo(true).build();
        String token = provider.generateToken(user);
        assertFalse(provider.validateToken(token.substring(0, token.length() - 2) + "xx"));
    }
}
