package com.example.demo.services;

import com.example.demo.dto.ProductoCUDTO;
import com.example.demo.dto.ProductoDTO;
import com.example.demo.repository.ProductoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
public class ProductoImportService {
    private final ProductoService productos;
    private final ProductoRepository repository;
    private final AuditoriaService auditoria;

    public ProductoImportService(ProductoService productos, ProductoRepository repository, AuditoriaService auditoria) {
        this.productos = productos;
        this.repository = repository;
        this.auditoria = auditoria;
    }

    public record Resultado(int creados, int actualizados, List<String> errores) {}

    @Transactional
    public Resultado importarCsv(String csv, Long usuarioId) {
        if (csv == null || csv.isBlank()) throw new IllegalArgumentException("El archivo CSV está vacío");
        String[] lines = csv.replace("\r\n", "\n").replace('\r', '\n').split("\n");
        int creados = 0;
        int actualizados = 0;
        List<String> errores = new ArrayList<>();
        int start = 0;
        if (lines.length > 0 && lines[0].toLowerCase(Locale.ROOT).contains("nombre")) start = 1;
        for (int i = start; i < lines.length; i++) {
            String line = lines[i].trim();
            if (line.isEmpty()) continue;
            try {
                String[] cols = parseLine(line);
                // Formato alineado con ExportService.productosCsv:
                // nombre,codigo_barras,marca,costo,precio,stock,unidad,alicuota_iva
                String nombre = col(cols, 0);
                String codigo = col(cols, 1);
                String marca = col(cols, 2);
                BigDecimal costo = decimal(col(cols, 3), "costo");
                BigDecimal precio = decimal(col(cols, 4), "precio");
                Integer stockCsv = integerOrNull(col(cols, 5));
                String unidad = blank(col(cols, 6), "UNIDAD").toUpperCase(Locale.ROOT);
                BigDecimal iva = decimalOr(col(cols, 7), new BigDecimal("21"));
                if (nombre == null || nombre.length() < 3)
                    throw new IllegalArgumentException("Nombre inválido");

                Long existingId = null;
                if (codigo != null) {
                    existingId = repository.findByCodigoBarras(codigo).map(p -> p.getId()).orElse(null);
                }
                if (existingId == null) {
                    int stock = stockCsv == null ? 0 : stockCsv;
                    ProductoCUDTO dto = new ProductoCUDTO(nombre, null, stock, false, costo, precio, null,
                            codigo, marca, 5, unidad, null, null, null, iva);
                    productos.crear(dto, usuarioId);
                    creados++;
                } else {
                    ProductoDTO actual = productos.detalle(existingId).orElseThrow();
                    int stock = stockCsv == null ? actual.stock() : stockCsv;
                    ProductoCUDTO dto = new ProductoCUDTO(nombre, actual.descripcion(), stock, actual.vencimiento(),
                            costo, precio, actual.categoria() == null ? null : actual.categoria().id(),
                            codigo, marca, actual.stockMinimo(), unidad, actual.fechaVencimiento(),
                            actual.cantidadMinimaPromo(), actual.precioPromocional(), iva);
                    productos.actualizar(existingId, dto, usuarioId);
                    actualizados++;
                }
            } catch (RuntimeException ex) {
                errores.add("Línea " + (i + 1) + ": " + ex.getMessage());
            }
        }
        auditoria.registrar(usuarioId, "IMPORTAR", "PRODUCTO", null,
                "CSV · creados " + creados + " · actualizados " + actualizados + " · errores " + errores.size());
        return new Resultado(creados, actualizados, errores);
    }

    private static String[] parseLine(String line) {
        List<String> cols = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean quoted = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                quoted = !quoted;
            } else if (c == ',' && !quoted) {
                cols.add(current.toString().trim());
                current.setLength(0);
            } else {
                current.append(c);
            }
        }
        cols.add(current.toString().trim());
        return cols.toArray(String[]::new);
    }

    private static String col(String[] cols, int index) {
        if (index >= cols.length) return null;
        String value = cols[index];
        return value == null || value.isBlank() ? null : value.trim();
    }

    private static String blank(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    private static BigDecimal decimal(String value, String field) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException("Falta " + field);
        try {
            return new BigDecimal(value.replace(',', '.'));
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException(field + " inválido");
        }
    }

    private static BigDecimal decimalOr(String value, BigDecimal fallback) {
        if (value == null || value.isBlank()) return fallback;
        return new BigDecimal(value.replace(',', '.'));
    }

    private static Integer integerOrNull(String value) {
        if (value == null || value.isBlank()) return null;
        try {
            return new BigDecimal(value.replace(',', '.')).intValueExact();
        } catch (RuntimeException ex) {
            throw new IllegalArgumentException("stock inválido");
        }
    }
}
