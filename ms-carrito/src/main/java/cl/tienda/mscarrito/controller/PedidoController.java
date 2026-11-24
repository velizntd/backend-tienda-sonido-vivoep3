package cl.tienda.mscarrito.controller;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;

import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import cl.tienda.mscarrito.dto.*;
import cl.tienda.mscarrito.service.PedidoService;

@RestController
@RequestMapping("/api/pedidos")
@RequiredArgsConstructor

@Tag(
        name = "Pedidos",
        description = "Checkout y gestion de pedidos"
)

@SecurityRequirement(name = "bearerAuth")

public class PedidoController {

    private final PedidoService pedidoService;

    @PostMapping

    @Operation(
            summary = "Crear o recuperar un pedido pendiente"
    )

    public ResponseEntity<PedidoResponse> crearPedido(

            @AuthenticationPrincipal Jwt jwt,

            @Valid @RequestBody PedidoRequest request) {

        PedidoResponse pedido =
                pedidoService.crearPedido(

                        jwt.getSubject(),

                        request,

                        jwt.getTokenValue()
                );

        if ("pendiente".equals(pedido.estado())) {

            return ResponseEntity

                    .status(HttpStatus.ACCEPTED)

                    .body(pedido);
        }

        return ResponseEntity

                .status(HttpStatus.CREATED)

                .body(pedido);
    }

    @GetMapping("/mis-pedidos")

    @Operation(
            summary = "Consultar historial del cliente"
    )

    public ResponseEntity<List<PedidoResponse>>
            obtenerMisPedidos(

                    @AuthenticationPrincipal Jwt jwt) {

        return ResponseEntity.ok(

                pedidoService.obtenerMisPedidos(
                        jwt.getSubject())
        );
    }

    @GetMapping

    @Operation(
            summary = "Listar todos los pedidos"
    )

    public ResponseEntity<List<PedidoResponse>>
            obtenerTodos() {

        return ResponseEntity.ok(

                pedidoService.obtenerTodos()
        );
    }

    @GetMapping("/{id}")

    @Operation(
            summary = "Consultar detalle de un pedido"
    )

    public ResponseEntity<PedidoResponse>
            obtenerDetalle(

                    @AuthenticationPrincipal Jwt jwt,

                    @PathVariable String id) {

        return ResponseEntity.ok(

                pedidoService.obtenerDetalle(

                        id,

                        jwt.getSubject(),

                        jwt.getClaimAsString("rol")
                )
        );
    }

    @PatchMapping("/{id}/estado")

    @Operation(
            summary = "Actualizar estado del pedido"
    )

    public ResponseEntity<PedidoResponse>
            actualizarEstado(

                    @PathVariable String id,

                    @Valid @RequestBody
                    EstadoPedidoRequest request) {

        return ResponseEntity.ok(

                pedidoService.actualizarEstado(

                        id,

                        request.estado()
                )
        );
    }
}