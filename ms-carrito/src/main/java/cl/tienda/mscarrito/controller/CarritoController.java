package cl.tienda.mscarrito.controller;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;

import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import cl.tienda.mscarrito.dto.*;
import cl.tienda.mscarrito.service.CarritoService;

@RestController
@RequestMapping("/api/carrito")
@RequiredArgsConstructor
@Tag(
        name = "Carrito",
        description = "Gestion del carrito del cliente autenticado"
)
@SecurityRequirement(name = "bearerAuth")
public class CarritoController {

    private final CarritoService carritoService;

    @GetMapping
    @Operation(summary = "Obtener carrito del cliente")
    public ResponseEntity<CarritoResponse> obtenerCarrito(
            @AuthenticationPrincipal Jwt jwt) {

        return ResponseEntity.ok(
                carritoService.obtenerCarrito(
                        jwt.getSubject(),
                        jwt.getTokenValue()));
    }

    @PostMapping("/items")
    @Operation(summary = "Agregar producto al carrito")
    public ResponseEntity<CarritoResponse> agregarProducto(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody ItemCarritoRequest request) {

        return ResponseEntity.ok(
                carritoService.agregarProducto(
                        jwt.getSubject(),
                        request,
                        jwt.getTokenValue()));
    }

    @PutMapping("/items/{codigo}")
    @Operation(summary = "Actualizar cantidad de un producto")
    public ResponseEntity<CarritoResponse> actualizarCantidad(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable String codigo,
            @Valid @RequestBody CantidadRequest request) {

        return ResponseEntity.ok(
                carritoService.actualizarCantidad(
                        jwt.getSubject(),
                        codigo,
                        request,
                        jwt.getTokenValue()));
    }

    @DeleteMapping("/items/{codigo}")
    @ApiResponse(responseCode = "200", description = "Producto eliminado; devuelve el carrito actualizado")
    @Operation(summary = "Eliminar producto del carrito")
    public ResponseEntity<CarritoResponse> eliminarProducto(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable String codigo) {

        return ResponseEntity.ok(
                carritoService.eliminarProducto(
                        jwt.getSubject(),
                        codigo,
                        jwt.getTokenValue()));
    }

    @DeleteMapping
    @ApiResponse(responseCode = "204", description = "Carrito vaciado; sin cuerpo de respuesta")
    @Operation(summary = "Vaciar carrito del cliente")
    public ResponseEntity<Void> vaciarCarrito(
            @AuthenticationPrincipal Jwt jwt) {

        carritoService.vaciarCarrito(
                jwt.getSubject());

        return ResponseEntity.noContent().build();
    }
}
