package com.example.demo.services;

import com.example.demo.dto.CategoriaDTO;
import com.example.demo.dto.CategoriaCUDTO;
import com.example.demo.models.Categoria;
import com.example.demo.repository.CategoriaRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;

    public CategoriaService(CategoriaRepository categoriaRepository) {
        this.categoriaRepository = categoriaRepository;
    }

    @Transactional
    public List<CategoriaDTO> listar() {
        return categoriaRepository.findAll()
                .stream()
                .map(c -> new CategoriaDTO(c.getId(), c.getNombre(), c.getDescripcion()))
                .toList();
    }

    @Transactional
    public CategoriaDTO obtener(Long id) {
        return categoriaRepository.findById(id)
                .map(c -> new CategoriaDTO(c.getId(), c.getNombre(), c.getDescripcion()))
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada: " + id));
    }

    @Transactional
    public CategoriaDTO crear(CategoriaCUDTO categoriaDTO) {
        if (categoriaRepository.existsByNombre(categoriaDTO.nombre())) {
            throw new RuntimeException("La categoría ya existe: " + categoriaDTO.nombre());
        }

        Categoria categoria = new Categoria(categoriaDTO.nombre(), categoriaDTO.descripcion());
        Categoria guardada = categoriaRepository.save(categoria);

        return new CategoriaDTO(guardada.getId(), guardada.getNombre(), guardada.getDescripcion());
    }

    @Transactional
    public CategoriaDTO actualizar(Long id, CategoriaCUDTO cambios) {
        Categoria categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada: " + id));

        if (cambios.nombre() != null && !cambios.nombre().equals(categoria.getNombre())) {
            if (categoriaRepository.existsByNombre(cambios.nombre())) {
                throw new RuntimeException("El nombre de la categoría ya existe");
            }
            categoria.setNombre(cambios.nombre());
        }

        if (cambios.descripcion() != null) {
            categoria.setDescripcion(cambios.descripcion());
        }

        Categoria actualizada = categoriaRepository.save(categoria);
        return new CategoriaDTO(actualizada.getId(), actualizada.getNombre(), actualizada.getDescripcion());
    }

    @Transactional
    public void eliminar(Long id) {
        if (!categoriaRepository.existsById(id)) {
            throw new RuntimeException("Categoría no encontrada: " + id);
        }
        categoriaRepository.deleteById(id);
    }
}

