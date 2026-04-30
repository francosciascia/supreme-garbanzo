package com.example.demo.controller;

import com.example.demo.dto.ProductoDTO;
import com.example.demo.dto.ProductoCUDTO;
import com.example.demo.models.Producto;
import com.example.demo.services.ProductoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller para gestionar productos
 * Proporciona endpoints para CRUD de productos
 */
@RestController
@RequestMapping("/productos")
@Tag(name = "Productos", description = "Operaciones sobre productos del sistema")
public class ProductoController {
    private final ProductoService productoService;

    public ProductoController(ProductoService productoService){
        this.productoService = productoService;
    }

    /**
     * Obtiene la lista de todos los productos
     *
     * @return Lista de ProductoDTO
     */
    @GetMapping
    @Operation(summary = "Listar productos", description = "Obtiene la lista de todos los productos disponibles")
    @ApiResponse(responseCode = "200", description = "Lista de productos obtenida exitosamente",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProductoDTO.class)))
    public List<ProductoDTO> listar(){
        return productoService.listar();
    }

    /**
     * Obtiene el detalle de un producto específico
     *
     * @param id ID del producto
     * @return ProductoDTO con los datos del producto
     */
    @GetMapping("/{id}")
    @Operation(summary = "Obtener producto", description = "Obtiene los detalles de un producto específico")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Producto encontrado"),
            @ApiResponse(responseCode = "404", description = "Producto no encontrado")
    })
    public ResponseEntity<ProductoDTO> detalle(
            @Parameter(description = "ID del producto") @PathVariable Long id){
        return productoService.detalle(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    /**
     * Elimina un producto
     *
     * @param id ID del producto a eliminar
     * @return 204 No Content si se elimina exitosamente
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar producto", description = "Elimina un producto del sistema")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Producto eliminado exitosamente"),
            @ApiResponse(responseCode = "404", description = "Producto no encontrado")
    })
    public ResponseEntity<Void> eliminar(
            @Parameter(description = "ID del producto") @PathVariable Long id){
        productoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Crea un nuevo producto
     *
     * @param productoDTO Datos del producto a crear
     * @return ProductoDTO del producto creado con código 201
     */
    @PostMapping
    @Operation(summary = "Crear producto", description = "Crea un nuevo producto en el sistema")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Producto creado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos")
    })
    public ResponseEntity<ProductoDTO> crear(
            @Valid @RequestBody ProductoCUDTO productoDTO){
        ProductoDTO nuevo = productoService.crear(productoDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevo);
    }

    /**
     * Actualiza un producto existente
     *
     * @param id ID del producto a actualizar
     * @param productoDTO Nuevos datos del producto
     * @return ProductoDTO con los datos actualizados
     */
    @PutMapping("/{id}")
    @Operation(summary = "Actualizar producto", description = "Actualiza los datos de un producto existente")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Producto actualizado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "404", description = "Producto no encontrado")
    })
    public ResponseEntity<ProductoDTO> actualizar(
            @Parameter(description = "ID del producto") @PathVariable Long id,
            @Valid @RequestBody ProductoCUDTO productoDTO){
        ProductoDTO actualizado = productoService.actualizar(id, productoDTO);
        return ResponseEntity.ok(actualizado);
    }
}
