package com.example.demo.services;

import com.example.demo.dto.ReglasOperativasDTO;
import com.example.demo.models.ReglasOperativas;
import com.example.demo.repository.ReglasOperativasRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReglasOperativasServiceTest {
    @Mock ReglasOperativasRepository repository;
    @Mock AuditoriaService auditoria;
    @InjectMocks ReglasOperativasService service;

    @Test
    void actualizaLasReglasConfigurables() {
        ReglasOperativas entity = ReglasOperativas.builder().id(1L).build();
        when(repository.findById(1L)).thenReturn(Optional.of(entity));
        var request = new ReglasOperativasDTO(false, true, new BigDecimal("12.5"), true,
                new BigDecimal("30000"), false, 45, true);

        var result = service.guardar(request, 7L);

        assertEquals(request, result);
        verify(repository).save(entity);
        verify(auditoria).registrar(7L, "MODIFICAR", "REGLAS_OPERATIVAS", 1, "Reglas comerciales actualizadas");
    }

    @Test
    void rechazaDescuentoMayorAlCienPorCiento() {
        var request = new ReglasOperativasDTO(true, false, new BigDecimal("101"), false,
                BigDecimal.ZERO, true, 30, false);

        assertThrows(IllegalArgumentException.class, () -> service.guardar(request, 1L));
        verify(repository, never()).save(any());
    }
}
