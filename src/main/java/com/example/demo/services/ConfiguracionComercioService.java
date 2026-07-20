package com.example.demo.services;

import com.example.demo.dto.ConfiguracionComercioDTO;
import com.example.demo.models.ConfiguracionComercio;
import com.example.demo.repository.ConfiguracionComercioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.util.Set;

@Service
public class ConfiguracionComercioService {
    private static final Set<String> MONEDAS = Set.of("ARS", "USD", "UYU", "BRL", "PYG", "CLP");
    private final ConfiguracionComercioRepository repository;

    public ConfiguracionComercioService(ConfiguracionComercioRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public ConfiguracionComercioDTO obtener() {
        return dto(repository.findById(1L).orElseGet(this::nueva));
    }

    @Transactional
    public ConfiguracionComercioDTO guardar(ConfiguracionComercioDTO value) {
        if (value.nombre() == null || value.nombre().isBlank())
            throw new IllegalArgumentException("El nombre comercial es obligatorio");
        if (value.anchoTicket() == null || value.anchoTicket() != 58 && value.anchoTicket() != 80)
            throw new IllegalArgumentException("El ticket debe ser de 58 u 80 mm");
        if (!MONEDAS.contains(value.moneda())) throw new IllegalArgumentException("Moneda no admitida");
        validarColor(value.colorPrimario(), "primario");
        validarColor(value.colorSecundario(), "secundario");
        validarUrl(value.logoUrl(), "logo");
        validarUrl(value.sitioWeb(), "sitio web");

        ConfiguracionComercio c = repository.findById(1L).orElseGet(this::nueva);
        c.setNombre(value.nombre().trim());
        c.setRubro(clean(value.rubro()));
        c.setSlogan(clean(value.slogan()));
        c.setRazonSocial(clean(value.razonSocial()));
        c.setCuit(clean(value.cuit()));
        c.setCondicionIva(clean(value.condicionIva()));
        c.setDireccion(clean(value.direccion()));
        c.setTelefono(clean(value.telefono()));
        c.setEmail(clean(value.email()));
        c.setWhatsapp(clean(value.whatsapp()));
        c.setSitioWeb(clean(value.sitioWeb()));
        c.setLogoUrl(clean(value.logoUrl()));
        c.setColorPrimario(value.colorPrimario().toUpperCase());
        c.setColorSecundario(value.colorSecundario().toUpperCase());
        c.setEncabezadoTicket(clean(value.encabezadoTicket()));
        c.setMensajeTicket(clean(value.mensajeTicket()));
        c.setAnchoTicket(value.anchoTicket());
        c.setMoneda(value.moneda());
        c.setZonaHoraria(clean(value.zonaHoraria()));
        c.setMostrarDatosFiscalesTicket(value.mostrarDatosFiscalesTicket());
        return dto(repository.save(c));
    }

    private ConfiguracionComercio nueva() {
        return ConfiguracionComercio.builder().id(1L).nombre("Franco").colorPrimario("#2563EB")
                .colorSecundario("#0F172A").anchoTicket(80).moneda("ARS")
                .zonaHoraria("America/Argentina/Buenos_Aires").mostrarDatosFiscalesTicket(true).build();
    }

    private ConfiguracionComercioDTO dto(ConfiguracionComercio c) {
        return new ConfiguracionComercioDTO(c.getNombre(), c.getRubro(), c.getSlogan(), c.getRazonSocial(),
                c.getCuit(), c.getCondicionIva(), c.getDireccion(), c.getTelefono(), c.getEmail(), c.getWhatsapp(),
                c.getSitioWeb(), c.getLogoUrl(), c.getColorPrimario(), c.getColorSecundario(), c.getEncabezadoTicket(),
                c.getMensajeTicket(), c.getAnchoTicket(), c.getMoneda(), c.getZonaHoraria(), c.isMostrarDatosFiscalesTicket());
    }

    private void validarColor(String color, String nombre) {
        if (color == null || !color.matches("#[0-9a-fA-F]{6}"))
            throw new IllegalArgumentException("El color " + nombre + " debe tener formato #RRGGBB");
    }

    private void validarUrl(String value, String nombre) {
        if (value == null || value.isBlank()) return;
        try {
            URI uri = URI.create(value.trim());
            if (!Set.of("http", "https").contains(uri.getScheme())) throw new IllegalArgumentException();
        } catch (RuntimeException exception) {
            throw new IllegalArgumentException("La URL de " + nombre + " no es válida");
        }
    }

    private String clean(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
