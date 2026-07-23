package com.example.demo.controller;

import com.example.demo.dto.ItemVentaCreateDTO;
import com.example.demo.dto.VentaCreateDTO;
import com.example.demo.dto.VentaDTO;
import com.example.demo.mapper.VentaMapper;
import com.example.demo.models.Venta;
import com.example.demo.services.VentaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.time.LocalDate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;

/**
 * Controller para gestionar ventas.
 * Devuelve siempre VentaDTO para evitar exponer la entidad y problemas de
 * lazy loading o serialización.
 */
@RestController
@RequestMapping("/api/ventas")
@Tag(name = "Ventas", description = "Operaciones sobre ventas del sistema")
public class VentaController {

    private final VentaService ventaService;

    public VentaController(VentaService ventaService) {
        this.ventaService = ventaService;
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener venta", description = "Obtiene los detalles de una venta específica")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Venta encontrada"),
            @ApiResponse(responseCode = "404", description = "Venta no encontrada")
    })
    public ResponseEntity<VentaDTO> detalle(
            @Parameter(description = "ID de la venta") @PathVariable Long id){
        return ventaService.detalle(id)
                .map(VentaMapper::toDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    @Operation(summary = "Listar ventas", description = "Obtiene la lista de todas las ventas del sistema")
    @ApiResponse(responseCode = "200", description = "Lista de ventas obtenida exitosamente")
    public List<VentaDTO> listar(){
        return ventaService.listarDTO();
    }

    @GetMapping("/page")
    @Operation(summary = "Buscar ventas paginadas")
    public Page<VentaDTO> buscar(
            @RequestParam(required = false) Long clienteId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @PageableDefault(size = 20, sort = "fecha", direction = Sort.Direction.DESC) Pageable pageable) {
        return ventaService.buscar(clienteId, desde, hasta, pageable);
    }

    @PostMapping
    @Operation(summary = "Crear venta", description = "Crea una nueva venta en el sistema")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Venta creada exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "409", description = "Conflicto - sin stock disponible")
    })
    public ResponseEntity<VentaDTO> crear(
            @Valid @RequestBody VentaCreateDTO ventaDTO){
        Venta venta = ventaService.crear(ventaDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(VentaMapper.toDTO(venta));
    }

    @PostMapping("/{id}/items")
    @Operation(summary = "Agregar item", description = "Agrega un producto a una venta existente")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Item agregado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "409", description = "Sin stock suficiente"),
            @ApiResponse(responseCode = "404", description = "Venta o producto no encontrado")
    })
    public ResponseEntity<VentaDTO> agregarItem(
            @Parameter(description = "ID de la venta") @PathVariable Long id,
            @Valid @RequestBody ItemVentaCreateDTO itemDTO){
        Venta venta = ventaService.agregarItem(id, itemDTO);
        return ResponseEntity.ok(VentaMapper.toDTO(venta));
    }

    @DeleteMapping("/{ventaId}/items/{itemId}")
    @Operation(summary = "Eliminar item", description = "Elimina un item de una venta")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Item eliminado exitosamente"),
            @ApiResponse(responseCode = "404", description = "Venta o item no encontrado")
    })
    public ResponseEntity<Void> eliminar(
            @Parameter(description = "ID de la venta") @PathVariable Long ventaId,
            @Parameter(description = "ID del item") @PathVariable Long itemId){
        ventaService.quitarItem(ventaId, itemId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/anular")
    public VentaDTO anular(@PathVariable Long id, @RequestParam(required = false) String motivo) {
        return VentaMapper.toDTO(ventaService.anular(id, motivo));
    }
}
