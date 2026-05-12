package com.example.demo.repository;

import com.example.demo.models.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Long> {
    Optional<Cliente> findByDni(Integer dni);
    Optional<Cliente> findByEmail(String email);
    boolean existsByDni(Integer dni);
    boolean existsByEmail(String email);
}
