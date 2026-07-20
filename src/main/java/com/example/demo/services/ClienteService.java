package com.example.demo.services;

import com.example.demo.dto.ClienteCUDTO;
import com.example.demo.dto.ClienteDTO;
import com.example.demo.exceptions.ResourceNotFoundException;
import com.example.demo.mapper.ClienteMapper;
import com.example.demo.models.Cliente;
import com.example.demo.repository.ClienteRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ClienteService {

    private final ClienteRepository clienteRepository;

    public ClienteService(ClienteRepository clienteRepository) {
        this.clienteRepository = clienteRepository;
    }

    @Transactional
    public List<ClienteDTO> listar() {
        return clienteRepository.findAll()
                .stream()
                .map(ClienteMapper::toDTO)
                .toList();
    }

    @Transactional
    public ClienteDTO obtener(Long id) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado: " + id));
        return ClienteMapper.toDTO(cliente);
    }

    @Transactional
    public ClienteDTO crear(ClienteCUDTO dto) {
        if (clienteRepository.existsByDni(dto.dni())) {
            throw new IllegalArgumentException("Ya existe un cliente con el DNI " + dto.dni());
        }
        if (dto.email() != null && !dto.email().isBlank() && clienteRepository.existsByEmail(dto.email())) {
            throw new IllegalArgumentException("Ya existe un cliente con el email " + dto.email());
        }

        Cliente cliente = Cliente.builder()
                .nombre(dto.nombre())
                .apellido(dto.apellido())
                .dni(dto.dni())
                .email(normalizarEmail(dto.email()))
                .telefono(dto.telefono())
                .direccion(dto.direccion())
                .activo(dto.activo() == null ? Boolean.TRUE : dto.activo())
                .limiteCredito(dto.limiteCredito() == null ? java.math.BigDecimal.ZERO : dto.limiteCredito())
                .build();

        return ClienteMapper.toDTO(clienteRepository.save(cliente));
    }

    @Transactional
    public ClienteDTO actualizar(Long id, ClienteCUDTO cambios) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado: " + id));

        if (cambios.dni() != null && !cambios.dni().equals(cliente.getDni())) {
            if (clienteRepository.existsByDni(cambios.dni())) {
                throw new IllegalArgumentException("Ya existe un cliente con el DNI " + cambios.dni());
            }
            cliente.setDni(cambios.dni());
        }

        String emailNuevo = normalizarEmail(cambios.email());
        if (emailNuevo != null && !emailNuevo.equalsIgnoreCase(cliente.getEmail())) {
            if (clienteRepository.existsByEmail(emailNuevo)) {
                throw new IllegalArgumentException("Ya existe un cliente con el email " + emailNuevo);
            }
            cliente.setEmail(emailNuevo);
        }

        if (cambios.nombre() != null) cliente.setNombre(cambios.nombre());
        if (cambios.apellido() != null) cliente.setApellido(cambios.apellido());
        if (cambios.telefono() != null) cliente.setTelefono(cambios.telefono());
        if (cambios.direccion() != null) cliente.setDireccion(cambios.direccion());
        if (cambios.activo() != null) cliente.setActivo(cambios.activo());
        if (cambios.limiteCredito() != null) cliente.setLimiteCredito(cambios.limiteCredito());

        return ClienteMapper.toDTO(clienteRepository.save(cliente));
    }

    @Transactional
    public void eliminar(Long id) {
        if (!clienteRepository.existsById(id)) {
            throw new ResourceNotFoundException("Cliente no encontrado: " + id);
        }
        clienteRepository.deleteById(id);
    }

    private String normalizarEmail(String email) {
        if (email == null) return null;
        String trimmed = email.trim();
        return trimmed.isEmpty() ? null : trimmed.toLowerCase();
    }
}
