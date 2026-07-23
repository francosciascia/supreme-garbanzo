package com.example.demo.dto;

public record ConfiguracionComercioDTO(
        String nombre,
        String rubro,
        String slogan,
        String razonSocial,
        String cuit,
        String condicionIva,
        String direccion,
        String telefono,
        String email,
        String whatsapp,
        String sitioWeb,
        String logoUrl,
        String colorPrimario,
        String colorSecundario,
        String encabezadoTicket,
        String mensajeTicket,
        Integer anchoTicket,
        String moneda,
        String zonaHoraria,
        boolean mostrarDatosFiscalesTicket,
        boolean setupCompletado,
        boolean mostrarIvaTicket
) {
    public ConfiguracionComercioDTO(
            String nombre, String rubro, String slogan, String razonSocial, String cuit, String condicionIva,
            String direccion, String telefono, String email, String whatsapp, String sitioWeb, String logoUrl,
            String colorPrimario, String colorSecundario, String encabezadoTicket, String mensajeTicket,
            Integer anchoTicket, String moneda, String zonaHoraria, boolean mostrarDatosFiscalesTicket) {
        this(nombre, rubro, slogan, razonSocial, cuit, condicionIva, direccion, telefono, email, whatsapp,
                sitioWeb, logoUrl, colorPrimario, colorSecundario, encabezadoTicket, mensajeTicket,
                anchoTicket, moneda, zonaHoraria, mostrarDatosFiscalesTicket, true, false);
    }
}
