package cl.tienda.mscarrito.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.Objects;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import cl.tienda.mscarrito.client.ProductoClient;
import cl.tienda.mscarrito.client.StockClient;
import cl.tienda.mscarrito.dto.*;
import cl.tienda.mscarrito.exception.CarritoException;
import cl.tienda.mscarrito.model.*;
import cl.tienda.mscarrito.repository.*;

@Service
@RequiredArgsConstructor
public class PedidoTransaccionService {

    private final CarritoRepository carritoRepository;

    private final DetalleCarritoRepository detalleCarritoRepository;

    private final PedidoRepository pedidoRepository;

    private final DetallePedidoRepository detallePedidoRepository;

    private final ProductoClient productoClient;

    private final StockClient stockClient;

    @Transactional
    public String prepararPedido(

            String usuarioId,
            PedidoRequest request,
            String token) {

        Carrito carrito = carritoRepository

                .findByUsuarioId(usuarioId)

                .orElseThrow(() -> new CarritoException(
                        HttpStatus.BAD_REQUEST,
                        "El carrito esta vacio"));

        // Recuperar el pedido pendiente antes de consultar
        // nuevamente productos o precios.

        Pedido pendiente = pedidoRepository

                .findFirstByUsuarioIdAndEstadoOrderByFechaDesc(
                        usuarioId,
                        "pendiente")

                .orElse(null);

        if (pendiente != null) {

            PedidoResponse anterior =
                    construirRespuesta(pendiente);

            if (!Objects.equals(
                        anterior.comprador(),
                        request.comprador())

                    || !Objects.equals(
                        anterior.entrega(),
                        request.entrega())) {

                throw new CarritoException(
                        HttpStatus.CONFLICT,
                        "Existe un pedido pendiente. "
                        + "Reintenta con los mismos datos");
            }

            return pendiente.getId();
        }

        validarEntrega(request.entrega());

        List<DetalleCarrito> detalles =
                detalleCarritoRepository
                        .findByCarrito_Id(carrito.getId());

        if (detalles.isEmpty()) {

            throw new CarritoException(
                    HttpStatus.BAD_REQUEST,
                    "El carrito esta vacio");
        }

        Pedido pedido = new Pedido();

        pedido.setId(UUID.randomUUID().toString());

        pedido.setUsuarioId(usuarioId);

        pedido.setFecha(
                LocalDateTime.now(ZoneOffset.UTC));

        pedido.setEstado("pendiente");

        pedido.setTipoEntrega(
                request.entrega().tipo());

        pedido.setNombreComprador(
                request.comprador().nombre());

        pedido.setApellidoComprador(
                request.comprador().apellido());

        pedido.setCorreoComprador(
                request.comprador().correo());

        pedido.setCalle(
                request.entrega().calle());

        pedido.setNumero(
                request.entrega().numero());

        pedido.setComplemento(
                request.entrega().complemento());

        pedido.setRegion(
                request.entrega().region());

        pedido.setComuna(
                request.entrega().comuna());

        pedido.setIndicaciones(
                request.entrega().indicaciones());

        BigDecimal total = BigDecimal.ZERO;

        List<DetallePedido> items =
                new ArrayList<>();

        for (DetalleCarrito detalle : detalles) {

            ProductoResponse producto =
                    productoClient.obtenerProducto(

                            detalle.getCodigoProducto(),

                            token
                    );

            if (producto.stock()
                    < detalle.getCantidad()) {

                throw new CarritoException(
                        HttpStatus.CONFLICT,
                        "Stock insuficiente para "
                                + producto.nombre());
            }

            DetallePedido item =
                    new DetallePedido();

            item.setId(
                    UUID.randomUUID().toString());

            item.setPedido(pedido);

            item.setCodigoProducto(
                    producto.codigo());

            item.setNombreProducto(
                    producto.nombre());

            item.setCantidad(
                    detalle.getCantidad());

            item.setPrecioUnitario(
                    producto.precio());

            items.add(item);

            total = total.add(

                    producto.precio().multiply(

                            BigDecimal.valueOf(
                                    detalle.getCantidad())
                    )
            );
        }

        pedido.setTotal(total);

        pedidoRepository.save(pedido);

        detallePedidoRepository.saveAll(items);

        return pedido.getId();
    }

    @Transactional
    public PedidoResponse confirmarPedido(

            String usuarioId,
            String pedidoId) {

        // Mantener el mismo orden de bloqueo:
        // primero carrito, luego pedido.

        Carrito carrito = carritoRepository

                .findByUsuarioId(usuarioId)

                .orElseThrow(() -> new CarritoException(
                        HttpStatus.NOT_FOUND,
                        "Carrito no encontrado"));

        Pedido pedido = pedidoRepository

                .findOneById(pedidoId)

                .orElseThrow(() -> new CarritoException(
                        HttpStatus.NOT_FOUND,
                        "Pedido no encontrado"));

        if (!usuarioId.equals(
                pedido.getUsuarioId())) {

            throw new CarritoException(
                    HttpStatus.NOT_FOUND,
                    "Pedido no encontrado");
        }

        if (!"pendiente".equals(
                pedido.getEstado())) {

            return construirRespuesta(pedido);
        }

        List<StockItemRequest> items =
                detallePedidoRepository

                .findByPedido_Id(pedidoId)

                .stream()

                .sorted((a, b) ->
                        a.getCodigoProducto().compareTo(
                                b.getCodigoProducto()))

                .map(detalle -> new StockItemRequest(

                        detalle.getCodigoProducto(),

                        detalle.getCantidad()
                ))

                .toList();

        StockDescuentoRequest solicitud =
                new StockDescuentoRequest(
                        pedidoId,
                        items);

        StockClient.Resultado resultado =
                stockClient.descontar(solicitud);

        if (resultado
                == StockClient.Resultado.DESCONTADO) {

            pedido.setEstado("pagado");

            detalleCarritoRepository
                    .deleteByCarrito_Id(
                            carrito.getId());

            carrito.setFechaActualizacion(
                    LocalDateTime.now(ZoneOffset.UTC));

            carritoRepository.save(carrito);

            pedidoRepository.save(pedido);

        } else if (resultado
                == StockClient.Resultado.STOCK_INSUFICIENTE) {

            pedido.setEstado("cancelado");

            pedidoRepository.save(pedido);
        }

        // Ante conflicto de idempotencia, timeout o
        // error de comunicacion, permanece pendiente.

        return construirRespuesta(pedido);
    }

    @Transactional(readOnly = true)
    public List<PedidoResponse> obtenerMisPedidos(
            String usuarioId) {

        return pedidoRepository

                .findByUsuarioIdOrderByFechaDesc(
                        usuarioId)

                .stream()

                .map(this::construirRespuesta)

                .toList();
    }

    @Transactional(readOnly = true)
    public List<PedidoResponse> obtenerTodos() {

        return pedidoRepository

                .findAllByOrderByFechaDesc()

                .stream()

                .map(this::construirRespuesta)

                .toList();
    }

    @Transactional(readOnly = true)
    public PedidoResponse obtenerDetalle(

            String id,

            String usuarioId,

            String rol) {

        Pedido pedido;

        if ("administrador".equals(rol)
                || "vendedor".equals(rol)) {

            pedido = pedidoRepository.findById(id)

                    .orElseThrow(() ->
                            new CarritoException(
                                    HttpStatus.NOT_FOUND,
                                    "Pedido no encontrado"));

        } else if ("cliente".equals(rol)) {

            pedido = pedidoRepository

                    .findByIdAndUsuarioId(
                            id,
                            usuarioId)

                    .orElseThrow(() ->
                            new CarritoException(
                                    HttpStatus.NOT_FOUND,
                                    "Pedido no encontrado"));

        } else {

            throw new CarritoException(
                    HttpStatus.FORBIDDEN,
                    "No tienes permisos");
        }

        return construirRespuesta(pedido);
    }

    @Transactional
    public PedidoResponse actualizarEstado(

            String id,

            String nuevoEstado) {

        Pedido pedido = pedidoRepository

                .findOneById(id)

                .orElseThrow(() -> new CarritoException(
                        HttpStatus.NOT_FOUND,
                        "Pedido no encontrado"));

        String actual = pedido.getEstado();

        boolean permitido = switch (actual) {

            case "pagado" ->
                    "enviado".equals(nuevoEstado)

                    || "cancelado".equals(nuevoEstado);

            case "enviado" ->
                    "entregado".equals(nuevoEstado);

            default -> false;
        };

        if (!permitido) {

            throw new CarritoException(
                    HttpStatus.CONFLICT,
                    "No se permite cambiar de "
                            + actual
                            + " a "
                            + nuevoEstado);
        }

        pedido.setEstado(nuevoEstado);

        pedidoRepository.save(pedido);

        return construirRespuesta(pedido);
    }

    private void validarEntrega(
            EntregaRequest entrega) {

        if ("despacho".equals(entrega.tipo())) {

            if (vacio(entrega.calle())

                    || vacio(entrega.numero())

                    || vacio(entrega.region())

                    || vacio(entrega.comuna())) {

                throw new CarritoException(
                        HttpStatus.BAD_REQUEST,
                        "La direccion de despacho "
                        + "esta incompleta");
            }
        }

        if (!"despacho".equals(entrega.tipo())
                && !"retiro".equals(entrega.tipo())) {

            throw new CarritoException(
                    HttpStatus.BAD_REQUEST,
                    "Tipo de entrega no valido");
        }
    }

    private boolean vacio(String valor) {

        return valor == null || valor.isBlank();
    }

    private PedidoResponse construirRespuesta(
            Pedido pedido) {

        CompradorRequest comprador =
                new CompradorRequest(

                        pedido.getNombreComprador(),

                        pedido.getApellidoComprador(),

                        pedido.getCorreoComprador()
                );

        EntregaRequest entrega =
                new EntregaRequest(

                        pedido.getTipoEntrega(),

                        pedido.getCalle(),

                        pedido.getNumero(),

                        pedido.getComplemento(),

                        pedido.getRegion(),

                        pedido.getComuna(),

                        pedido.getIndicaciones()
                );

        List<ItemPedidoResponse> items =
                detallePedidoRepository

                .findByPedido_Id(pedido.getId())

                .stream()

                .map(detalle ->
                        new ItemPedidoResponse(

                                detalle.getCodigoProducto(),

                                detalle.getNombreProducto(),

                                detalle.getCantidad(),

                                detalle.getPrecioUnitario()
                        ))

                .toList();

        return new PedidoResponse(

                pedido.getId(),

                pedido.getFecha(),

                pedido.getUsuarioId(),

                pedido.getEstado(),

                comprador,

                entrega,

                items,

                pedido.getTotal()
        );
    }
}