package com.example.demo.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Value("${app.cors.allowed-origin:http://localhost:8083}")
    private String allowedOrigin;

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Autowired
    private ApiAuthenticationEntryPoint authenticationEntryPoint;

    @Autowired
    private ApiAccessDeniedHandler accessDeniedHandler;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler))
                .authorizeHttpRequests(authz -> authz

                        // Archivos públicos
                        .requestMatchers("/", "/login", "/productos", "/categorias", "/clientes", "/ventas", "/caja", "/compras", "/proveedores", "/inventario", "/lotes", "/devoluciones", "/reportes", "/usuarios", "/empleados", "/reglas", "/auditoria", "/configuracion", "/ticket/**",
                                "/favicon.ico", "/VAADIN/**", "/frontend/**", "/webjars/**", "/icons/**",
                                "/images/**", "/manifest.webmanifest", "/sw.js", "/offline.html").permitAll()

                        // Auth pública
                        .requestMatchers("/api/auth/**").permitAll()

                        // Categorías - GET público
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/categorias",
                                "/api/categorias/**"
                        ).permitAll()

                        // Categorías - ADMIN / SUPER_ADMIN
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/categorias"
                        ).hasAnyRole("ADMIN", "SUPER_ADMIN")

                        .requestMatchers(
                                HttpMethod.PUT,
                                "/api/categorias/**"
                        ).hasAnyRole("ADMIN", "SUPER_ADMIN")

                        .requestMatchers(
                                HttpMethod.DELETE,
                                "/api/categorias/**"
                        ).hasRole("SUPER_ADMIN")

                        // Productos - GET público
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/productos",
                                "/api/productos/**"
                        ).permitAll()

                        // Productos - ADMIN / SUPER_ADMIN
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/productos"
                        ).hasAnyRole("ADMIN", "SUPER_ADMIN")

                        .requestMatchers(
                                HttpMethod.PUT,
                                "/api/productos/**"
                        ).hasAnyRole("ADMIN", "SUPER_ADMIN")

                        .requestMatchers(
                                HttpMethod.DELETE,
                                "/api/productos/**"
                        ).hasRole("SUPER_ADMIN")

                        // Clientes - lectura para usuarios autenticados
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/clientes",
                                "/api/clientes/**"
                        ).authenticated()

                        // Clientes - ADMIN / SUPER_ADMIN pueden crear / editar
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/clientes"
                        ).hasAnyRole("ADMIN", "SUPER_ADMIN")

                        .requestMatchers(
                                HttpMethod.PUT,
                                "/api/clientes/**"
                        ).hasAnyRole("ADMIN", "SUPER_ADMIN")

                        // Clientes - sólo SUPER_ADMIN elimina
                        .requestMatchers(
                                HttpMethod.DELETE,
                                "/api/clientes/**"
                        ).hasRole("SUPER_ADMIN")

                        // Ventas - autenticados
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/ventas",
                                "/api/ventas/**"
                        ).authenticated()

                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/ventas"
                        ).authenticated()

                        // Ventas - solo SUPER_ADMIN
                        .requestMatchers(
                                HttpMethod.DELETE,
                                "/api/ventas/**"
                        ).hasRole("SUPER_ADMIN")

                        .requestMatchers("/api/cajas/**").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/proveedores/**", "/api/compras/**").hasAnyRole("ADMIN", "SUPER_ADMIN")
                        .requestMatchers("/api/proveedores/**", "/api/compras/**").hasAnyRole("ADMIN", "SUPER_ADMIN")
                        .requestMatchers("/api/inventario/**").hasAnyRole("ADMIN", "SUPER_ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/ventas/*/anular").hasRole("SUPER_ADMIN")
                        .requestMatchers("/api/usuarios/**").hasRole("SUPER_ADMIN")
                        .requestMatchers("/api/configuracion/**").hasRole("SUPER_ADMIN")
                        .requestMatchers("/api/reglas/**", "/api/empleados/**", "/api/auditoria/**").hasRole("SUPER_ADMIN")
                        .requestMatchers("/api/devoluciones/**").hasAnyRole("ADMIN", "SUPER_ADMIN")
                        .requestMatchers("/api/cuentas-corrientes/**").authenticated()
                        .requestMatchers("/api/lotes/**").hasAnyRole("ADMIN", "SUPER_ADMIN")

                        // Swagger
                        .requestMatchers(
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/v3/api-docs/**"
                        ).permitAll()

                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.stream(allowedOrigin.split(",")).map(String::trim).toList());
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(false);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}

