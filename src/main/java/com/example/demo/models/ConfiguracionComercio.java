package com.example.demo.models;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "configuracion_comercio")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConfiguracionComercio {
    @Id
    private Long id;
    @Column(nullable = false)
    private String nombre;
    private String rubro;
    private String slogan;
    @Column(name = "razon_social")
    private String razonSocial;
    private String cuit;
    @Column(name = "condicion_iva")
    private String condicionIva;
    private String direccion;
    private String telefono;
    private String email;
    private String whatsapp;
    @Column(name = "sitio_web")
    private String sitioWeb;
    @Column(name = "logo_url", length = 1000)
    private String logoUrl;
    @Column(name = "color_primario", nullable = false)
    @Builder.Default
    private String colorPrimario = "#2563EB";
    @Column(name = "color_secundario", nullable = false)
    @Builder.Default
    private String colorSecundario = "#0F172A";
    @Column(name = "encabezado_ticket")
    private String encabezadoTicket;
    @Column(name = "mensaje_ticket")
    private String mensajeTicket;
    @Column(name = "mostrar_datos_fiscales_ticket", nullable = false)
    @Builder.Default
    private boolean mostrarDatosFiscalesTicket = true;
    @Column(name = "ancho_ticket", nullable = false)
    @Builder.Default
    private Integer anchoTicket = 80;
    @Column(nullable = false)
    @Builder.Default
    private String moneda = "ARS";
    @Column(name = "zona_horaria", nullable = false)
    @Builder.Default
    private String zonaHoraria = "America/Argentina/Buenos_Aires";
    @Column(name = "setup_completado", nullable = false)
    @Builder.Default
    private boolean setupCompletado = true;
    @Column(name = "mostrar_iva_ticket", nullable = false)
    @Builder.Default
    private boolean mostrarIvaTicket = false;
}
