package cl.tienda.msproductos.service;

import cl.tienda.msproductos.dto.DescuentoStockDTO;
import cl.tienda.msproductos.dto.RespuestaStockDTO;
import cl.tienda.msproductos.dto.StockItemDTO;
import cl.tienda.msproductos.entity.DescuentoStock;
import cl.tienda.msproductos.entity.Producto;
import cl.tienda.msproductos.exception.StockException;
import cl.tienda.msproductos.repository.DescuentoStockRepository;
import cl.tienda.msproductos.repository.ProductoRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.LinkedHashMap;

@Service
@RequiredArgsConstructor
public class StockService {

    private final ProductoRepository productoRepository;
    private final DescuentoStockRepository descuentoStockRepository;

    @Transactional
    public RespuestaStockDTO descontar(DescuentoStockDTO solicitud) {

        Map<String, Integer> cantidades = agruparItems(solicitud);

        String huella = generarHuella(cantidades);

        descuentoStockRepository.registrarSolicitud(
                solicitud.getPedidoId(),
                huella
        );

        DescuentoStock descuento = descuentoStockRepository
                .buscarBloqueado(solicitud.getPedidoId())
                .orElseThrow(() -> new IllegalStateException(
                        "No se pudo registrar el descuento"
                ));

        // Verificar que el pedido tenga los mismos datos.
        if (!huella.equals(descuento.getHuellaSolicitud())) {

            throw new StockException(
                    "CONFLICTO_IDEMPOTENCIA",
                    "El pedido ya fue registrado con otros productos o cantidades"
            );
        }

        // Evitar descuentos duplicados.
        if (Boolean.TRUE.equals(descuento.getProcesado())) {

            return new RespuestaStockDTO(
                    solicitud.getPedidoId(),
                    "DESCONTADO"
            );
        }

        Map<String, Producto> productos = new LinkedHashMap<>();

        // Bloquear y validar todos los productos.
        for (Map.Entry<String, Integer> item : cantidades.entrySet()) {

            Producto producto = productoRepository
                    .buscarParaActualizar(item.getKey())
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            "Producto no encontrado: " + item.getKey()
                    ));

            if (producto.getStock() < item.getValue()) {

                throw new StockException(
                        "STOCK_INSUFICIENTE",
                        "Stock insuficiente para: " + item.getKey()
                );
            }

            productos.put(item.getKey(), producto);
        }

        // Descontar solamente si todos tienen stock suficiente.
        for (Map.Entry<String, Integer> item : cantidades.entrySet()) {

            Producto producto = productos.get(item.getKey());

            producto.setStock(
                    producto.getStock() - item.getValue()
            );

            productoRepository.save(producto);
        }

        descuento.setProcesado(true);

        descuentoStockRepository.save(descuento);

        return new RespuestaStockDTO(
                solicitud.getPedidoId(),
                "DESCONTADO"
        );
    }

    private Map<String, Integer> agruparItems(
            DescuentoStockDTO solicitud) {

        Map<String, Integer> cantidades = new TreeMap<>();

        for (StockItemDTO item : solicitud.getItems()) {

            String codigo = item.getCodigo()
                    .trim()
                    .toUpperCase(Locale.ROOT);

            try {

                cantidades.merge(
                        codigo,
                        item.getCantidad(),
                        Math::addExact
                );

            } catch (ArithmeticException error) {

                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Cantidad de productos demasiado grande"
                );
            }
        }

        return cantidades;
    }

    private String generarHuella(Map<String, Integer> cantidades) {

        StringBuilder contenido = new StringBuilder();

        cantidades.forEach((codigo, cantidad) ->
                contenido.append(codigo.length())
                        .append(":")
                        .append(codigo)
                        .append(":")
                        .append(cantidad)
                        .append(";")
        );

        try {

            MessageDigest digest =
                    MessageDigest.getInstance("SHA-256");

            byte[] hash = digest.digest(
                    contenido.toString()
                            .getBytes(StandardCharsets.UTF_8)
            );

            return HexFormat.of().formatHex(hash);

        } catch (NoSuchAlgorithmException error) {

            throw new IllegalStateException(
                    "No se pudo generar la huella del pedido",
                    error
            );
        }
    }
}