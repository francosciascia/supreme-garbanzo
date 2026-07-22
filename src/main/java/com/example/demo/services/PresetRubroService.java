package com.example.demo.services;

import com.example.demo.dto.PresetRubro;
import com.example.demo.dto.ReglasOperativasDTO;
import com.example.demo.models.ConfiguracionComercio;
import com.example.demo.repository.ConfiguracionComercioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PresetRubroService {
    private final ReglasOperativasService reglas;
    private final ConfiguracionComercioRepository configuracion;
    private final AuditoriaService auditoria;

    public PresetRubroService(ReglasOperativasService reglas, ConfiguracionComercioRepository configuracion,
                              AuditoriaService auditoria) {
        this.reglas = reglas;
        this.configuracion = configuracion;
        this.auditoria = auditoria;
    }

    @Transactional
    public ReglasOperativasDTO aplicar(PresetRubro preset, Long usuarioId) {
        ReglasOperativasDTO aplicadas = reglas.guardar(preset.reglas(), usuarioId);
        ConfiguracionComercio comercio = configuracion.findById(1L).orElseGet(() ->
                ConfiguracionComercio.builder().id(1L).nombre("Franco").colorPrimario("#2563EB")
                        .colorSecundario("#0F172A").anchoTicket(80).moneda("ARS")
                        .zonaHoraria("America/Argentina/Buenos_Aires").mostrarDatosFiscalesTicket(true).build());
        comercio.setRubro(preset.rubro());
        configuracion.save(comercio);
        auditoria.registrar(usuarioId, "MODIFICAR", "PRESET_RUBRO", preset.name(),
                "Aplicado preset " + preset.nombre());
        return aplicadas;
    }
}
