package com.example.demo.controller;

import com.example.demo.dto.ItemVentaCreateDTO;
import com.example.demo.dto.VentaCreateDTO;
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

/**
 * Controller para gestionar ventas
 * Proporciona endpoints para crear, actualizar y consultar ventas
 */
@RestController
@RequestMapping("/ventas")
@Tag(name = "Ventas", description = "Operaciones sobre ventas del sistema")
public class VentaController {

    private final VentaService ventaService;

    public VentaController(VentaService ventaService) {
        this.ventaService = ventaService;
    }

    /**
     * Obtiene el detalle de una venta específica
     *
     * @param id ID de la venta
     * @return Datos de la venta
     */
    @GetMapping("/{id}")
    @Operation(summary = "Obtener venta", description = "Obtiene los detalles de una venta específica")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Venta encontrada"),
            @ApiResponse(responseCode = "404", description = "Venta no encontrada")
    })
    public ResponseEntity<Venta> detalle(
            @Parameter(description = "ID de la venta") @PathVariable Long id){
        return ventaService.detalle(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Obtiene la lista de todas las ventas
     *
     * @return Lista de ventas
     */
    @GetMapping
    @Operation(summary = "Listar ventas", description = "Obtiene la lista de todas las ventas del sistema")
    @ApiResponse(responseCode = "200", description = "Lista de ventas obtenida exitosamente")
    public List<Venta> listar(){
        return ventaService.listar();
    }

    /**
     * Crea una nueva venta con sus items
     *
     * @param ventaDTO Datos de la venta a crear (incluye lista de items)
     * @return Venta creada con código 201
     */
    @PostMapping
    @Operation(summary = "Crear venta", description = "Crea una nueva venta en el sistema")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Venta creada exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos o stock insuficiente"),
            @ApiResponse(responseCode = "409", description = "Conflicto - sin stock disponible")
    })
    public ResponseEntity<Venta> crear(
            @Valid @RequestBody VentaCreateDTO ventaDTO){
        Venta venta = ventaService.crear(ventaDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(venta);
    }

    /**
     * Agrega un item a una venta existente
     *
     * @param id ID de la venta
     * @param itemDTO Datos del item a agregar
     * @return Venta actualizada
     */
    @PostMapping("/{id}/items")
    @Operation(summary = "Agregar item", description = "Agrega un producto a una venta existente")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Item agregado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "409", description = "Sin stock suficiente"),
            @ApiResponse(responseCode = "404", description = "Venta o producto no encontrado")
    })
    public ResponseEntity<Venta> agregarItem(
            @Parameter(description = "ID de la venta") @PathVariable Long id,
            @Valid @RequestBody ItemVentaCreateDTO itemDTO){
        Venta venta = ventaService.agregarItem(id, itemDTO);
        return ResponseEntity.ok(venta);
    }

    /**
     * Elimina un item de una venta
     *
     * @param ventaId ID de la venta
     * @param itemId ID del item a eliminar
     * @return 204 No Content
     */
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
}