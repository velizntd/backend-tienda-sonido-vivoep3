package cl.tienda.mscarrito.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import cl.tienda.mscarrito.client.ProductoClient;
import cl.tienda.mscarrito.dto.*;
import cl.tienda.mscarrito.exception.CarritoException;
import cl.tienda.mscarrito.model.Carrito;
import cl.tienda.mscarrito.model.DetalleCarrito;
import cl.tienda.mscarrito.repository.CarritoRepository;
import cl.tienda.mscarrito.repository.DetalleCarritoRepository;
import cl.tienda.mscarrito.repository.PedidoRepository;

@Service
@RequiredArgsConstructor
@Transactional
public class CarritoService {

    private final CarritoRepository carritoRepository;

    private final DetalleCarritoRepository detalleRepository;

    private final ProductoClient productoClient;

    private final PedidoRepository pedidoRepository;

    private static final int CANTIDAD_MAXIMA = 999;

    private Carrito obtenerOCrearCarrito(
            String usuarioId) {

        carritoRepository.crearSiNoExiste(
                UUID.randomUUID().toString(),
                usuarioId,
                LocalDateTime.now(ZoneOffset.UTC));

        return carritoRepository
                .findByUsuarioId(usuarioId)
                .orElseThrow(() -> new CarritoException(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "No fue posible obtener el carrito"));
    }

    private void validarSinPedidoPendiente(
            String usuarioId) {

        if (pedidoRepository.existsByUsuarioIdAndEstado(
                usuarioId,
                "pendiente")) {

            throw new CarritoException(
                    HttpStatus.CONFLICT,
                    "Existe un pedido pendiente. "
                    + "Debes confirmar su resultado "
                    + "antes de modificar el carrito");
        }
    }

    public CarritoResponse obtenerCarrito(
            String usuarioId,
            String token) {

        Carrito carrito =
                obtenerOCrearCarrito(usuarioId);

        return construirRespuesta(carrito, token);
    }

    public CarritoResponse agregarProducto(
            String usuarioId,
            ItemCarritoRequest request,
            String token) {

        Carrito carrito =
                obtenerOCrearCarrito(usuarioId);

        validarSinPedidoPendiente(usuarioId);

        validarCantidad(request.cantidad());

        ProductoResponse producto =
                productoClient.obtenerProducto(
                        request.codigo(),
                        token);

        DetalleCarrito detalle = detalleRepository
                .findByCarrito_IdAndCodigoProducto(
                        carrito.getId(),
                        producto.codigo())
                .orElseGet(() -> {

                    DetalleCarrito nuevo =
                            new DetalleCarrito();

                    nuevo.setId(
                            UUID.randomUUID().toString());

                    nuevo.setCarrito(carrito);

                    nuevo.setCodigoProducto(
                            producto.codigo());

                    nuevo.setCantidad(0);

                    return nuevo;
                });

        int cantidadFinal =
                detalle.getCantidad()
                        + request.cantidad();

        validarCantidad(cantidadFinal);

        validarStock(producto, cantidadFinal);

        detalle.setCantidad(cantidadFinal);

        detalleRepository.save(detalle);

        actualizarFecha(carrito);

        return construirRespuesta(carrito, token);
    }

    public CarritoResponse actualizarCantidad(
            String usuarioId,
            String codigo,
            CantidadRequest request,
            String token) {

        Carrito carrito =
                obtenerOCrearCarrito(usuarioId);

        validarSinPedidoPendiente(usuarioId);

        validarCantidad(request.cantidad());

        DetalleCarrito detalle = detalleRepository
                .findByCarrito_IdAndCodigoProducto(
                        carrito.getId(),
                        codigo)
                .orElseThrow(() -> new CarritoException(
                        HttpStatus.NOT_FOUND,
                        "El producto no existe en el carrito"));

        ProductoResponse producto =
                productoClient.obtenerProducto(
                        codigo,
                        token);

        validarStock(
                producto,
                request.cantidad());

        detalle.setCantidad(request.cantidad());

        detalleRepository.save(detalle);

        actualizarFecha(carrito);

        return construirRespuesta(carrito, token);
    }

    public CarritoResponse eliminarProducto(
            String usuarioId,
            String codigo,
            String token) {

        Carrito carrito =
                obtenerOCrearCarrito(usuarioId);

        validarSinPedidoPendiente(usuarioId);

        DetalleCarrito detalle = detalleRepository
                .findByCarrito_IdAndCodigoProducto(
                        carrito.getId(),
                        codigo)
                .orElseThrow(() -> new CarritoException(
                        HttpStatus.NOT_FOUND,
                        "El producto no existe en el carrito"));

        detalleRepository.delete(detalle);

        actualizarFecha(carrito);

        return construirRespuesta(carrito, token);
    }

    public void vaciarCarrito(String usuarioId) {

        Carrito carrito =
                obtenerOCrearCarrito(usuarioId);

        validarSinPedidoPendiente(usuarioId);

        detalleRepository.deleteByCarrito_Id(
                carrito.getId());

        actualizarFecha(carrito);
    }

    private void validarCantidad(Integer cantidad) {

        if (cantidad == null
                || cantidad < 1
                || cantidad > CANTIDAD_MAXIMA) {

            throw new CarritoException(
                    HttpStatus.BAD_REQUEST,
                    "La cantidad debe estar entre 1 y 999");
        }
    }

    private void validarStock(
            ProductoResponse producto,
            int cantidad) {

        if (cantidad > producto.stock()) {

            throw new CarritoException(
                    HttpStatus.CONFLICT,
                    "Stock insuficiente para "
                            + producto.nombre());
        }
    }

    private void actualizarFecha(Carrito carrito) {

        carrito.setFechaActualizacion(
                LocalDateTime.now(ZoneOffset.UTC));

        carritoRepository.save(carrito);
    }

    private CarritoResponse construirRespuesta(
            Carrito carrito,
            String token) {

        List<DetalleCarrito> detalles =
                detalleRepository.findByCarrito_Id(
                        carrito.getId());

        List<ItemCarritoResponse> items =
                new ArrayList<>();

        BigDecimal total = BigDecimal.ZERO;

        int cantidadTotal = 0;

        for (DetalleCarrito detalle : detalles) {

            ProductoResponse producto;

            try {

                producto =
                        productoClient.obtenerProducto(
                                detalle.getCodigoProducto(),
                                token);

            } catch (CarritoException ex) {

                if (ex.getEstado()
                        != HttpStatus.NOT_FOUND) {

                    throw ex;
                }

                items.add(new ItemCarritoResponse(

                        detalle.getCodigoProducto(),
                        "Producto no disponible",
                        null,
                        detalle.getCantidad(),
                        0,
                        BigDecimal.ZERO,
                        BigDecimal.ZERO,
                        false
                ));

                cantidadTotal += detalle.getCantidad();

                continue;
            }

            BigDecimal subtotal =
                    producto.precio().multiply(
                            BigDecimal.valueOf(
                                    detalle.getCantidad()));

            boolean disponible =
                    producto.stock()
                            >= detalle.getCantidad();

            items.add(new ItemCarritoResponse(

                    producto.codigo(),
                    producto.nombre(),
                    producto.imagen(),
                    detalle.getCantidad(),
                    producto.stock(),
                    producto.precio(),
                    subtotal,
                    disponible
            ));

            total = total.add(subtotal);

            cantidadTotal += detalle.getCantidad();
        }

        return new CarritoResponse(

                carrito.getId(),
                carrito.getUsuarioId(),
                items,
                cantidadTotal,
                total
        );
    }
}