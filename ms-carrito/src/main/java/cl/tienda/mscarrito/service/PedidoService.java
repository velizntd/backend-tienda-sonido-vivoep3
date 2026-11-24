package cl.tienda.mscarrito.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

import cl.tienda.mscarrito.dto.PedidoRequest;
import cl.tienda.mscarrito.dto.PedidoResponse;
import cl.tienda.mscarrito.exception.CarritoException;

@Service
@RequiredArgsConstructor
public class PedidoService {

    private final PedidoTransaccionService transacciones;

    public PedidoResponse crearPedido(

            String usuarioId,

            PedidoRequest request,

            String token) {

        // Primera transaccion:
        // guardar el pedido pendiente.

        String pedidoId =
                transacciones.prepararPedido(

                        usuarioId,

                        request,

                        token
                );

        // Segunda transaccion:
        // confirmar el descuento de inventario.

        PedidoResponse respuesta =
                transacciones.confirmarPedido(

                        usuarioId,

                        pedidoId
                );

        if ("cancelado".equals(respuesta.estado())) {

            throw new CarritoException(
                    HttpStatus.CONFLICT,
                    "Stock insuficiente. "
                    + "El carrito se ha conservado");
        }

        return respuesta;
    }

    public List<PedidoResponse> obtenerMisPedidos(
            String usuarioId) {

        return transacciones.obtenerMisPedidos(
                usuarioId);
    }

    public List<PedidoResponse> obtenerTodos() {

        return transacciones.obtenerTodos();
    }

    public PedidoResponse obtenerDetalle(

            String id,

            String usuarioId,

            String rol) {

        return transacciones.obtenerDetalle(

                id,

                usuarioId,

                rol
        );
    }

    public PedidoResponse actualizarEstado(

            String id,

            String estado) {

        return transacciones.actualizarEstado(

                id,

                estado
        );
    }
}