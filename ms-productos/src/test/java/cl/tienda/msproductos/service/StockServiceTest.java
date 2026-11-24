package cl.tienda.msproductos.service;

import cl.tienda.msproductos.dto.DescuentoStockDTO;
import cl.tienda.msproductos.dto.RespuestaStockDTO;
import cl.tienda.msproductos.dto.StockItemDTO;
import cl.tienda.msproductos.entity.DescuentoStock;
import cl.tienda.msproductos.entity.Producto;
import cl.tienda.msproductos.repository.DescuentoStockRepository;
import cl.tienda.msproductos.repository.ProductoRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StockServiceTest {

    @Mock
    private ProductoRepository productoRepository;

    @Mock
    private DescuentoStockRepository descuentoStockRepository;

    @InjectMocks
    private StockService stockService;

    private Producto productoA;
    private Producto productoB;
    private DescuentoStock descuento;

    @BeforeEach
    void prepararDatos() {

        productoA = new Producto();
        productoA.setCodigo("GA001");
        productoA.setStock(10);

        productoB = new Producto();
        productoB.setCodigo("GA003");
        productoB.setStock(5);

        descuento = new DescuentoStock();
        descuento.setPedidoId(125L);
        descuento.setProcesado(false);
    }

    private void prepararSolicitud() {

        when(descuentoStockRepository.registrarSolicitud(
                eq(125L), anyString()))
                .thenAnswer(invocacion -> {

                    if (descuento.getHuellaSolicitud() == null) {
                        descuento.setHuellaSolicitud(
                                invocacion.getArgument(1)
                        );
                    }

                    return 1;
                });

        when(descuentoStockRepository.buscarBloqueado(125L))
                .thenReturn(Optional.of(descuento));
    }

    private DescuentoStockDTO solicitudNormal() {

        return new DescuentoStockDTO(
                125L,
                List.of(
                        new StockItemDTO("GA001", 2),
                        new StockItemDTO("GA003", 1)
                )
        );
    }

    @Test
    void descontarVariosProductos() {

        prepararSolicitud();

        when(productoRepository.buscarParaActualizar("GA001"))
                .thenReturn(Optional.of(productoA));

        when(productoRepository.buscarParaActualizar("GA003"))
                .thenReturn(Optional.of(productoB));

        RespuestaStockDTO respuesta =
                stockService.descontar(solicitudNormal());

        assertEquals("DESCONTADO", respuesta.getEstado());
        assertEquals(125L, respuesta.getPedidoId());
        assertEquals(8, productoA.getStock());
        assertEquals(4, productoB.getStock());
        assertTrue(descuento.getProcesado());

        verify(productoRepository).save(productoA);
        verify(productoRepository).save(productoB);
    }

    @Test
    void rechazarStockInsuficienteSinDescuentoParcial() {

        prepararSolicitud();

        productoB.setStock(0);

        when(productoRepository.buscarParaActualizar("GA001"))
                .thenReturn(Optional.of(productoA));

        when(productoRepository.buscarParaActualizar("GA003"))
                .thenReturn(Optional.of(productoB));

        ResponseStatusException error = assertThrows(
                ResponseStatusException.class,
                () -> stockService.descontar(solicitudNormal())
        );

        assertEquals(409, error.getStatusCode().value());
        assertEquals(10, productoA.getStock());
        assertEquals(0, productoB.getStock());
        assertFalse(descuento.getProcesado());

        verify(productoRepository, never())
                .save(any(Producto.class));
    }

    @Test
    void rechazarProductoInexistente() {

        prepararSolicitud();

        when(productoRepository.buscarParaActualizar("GA001"))
                .thenReturn(Optional.empty());

        ResponseStatusException error = assertThrows(
                ResponseStatusException.class,
                () -> stockService.descontar(solicitudNormal())
        );

        assertEquals(404, error.getStatusCode().value());

        verify(productoRepository, never())
                .save(any(Producto.class));
    }

    @Test
    void evitarDescuentoDuplicado() {

        prepararSolicitud();

        when(productoRepository.buscarParaActualizar("GA001"))
                .thenReturn(Optional.of(productoA));

        when(productoRepository.buscarParaActualizar("GA003"))
                .thenReturn(Optional.of(productoB));

        stockService.descontar(solicitudNormal());

        RespuestaStockDTO respuesta =
                stockService.descontar(solicitudNormal());

        assertEquals("DESCONTADO", respuesta.getEstado());
        assertEquals(8, productoA.getStock());
        assertEquals(4, productoB.getStock());

        verify(productoRepository, times(2))
                .save(any(Producto.class));
    }

    @Test
    void rechazarMismoPedidoConDistintasCantidades() {

        prepararSolicitud();

        when(productoRepository.buscarParaActualizar("GA001"))
                .thenReturn(Optional.of(productoA));

        when(productoRepository.buscarParaActualizar("GA003"))
                .thenReturn(Optional.of(productoB));

        stockService.descontar(solicitudNormal());

        DescuentoStockDTO modificada =
                new DescuentoStockDTO(
                        125L,
                        List.of(new StockItemDTO("GA001", 5))
                );

        ResponseStatusException error = assertThrows(
                ResponseStatusException.class,
                () -> stockService.descontar(modificada)
        );

        assertEquals(409, error.getStatusCode().value());
        assertEquals(8, productoA.getStock());
        assertEquals(4, productoB.getStock());
    }

    @Test
    void agruparProductosRepetidos() {

        prepararSolicitud();

        when(productoRepository.buscarParaActualizar("GA001"))
                .thenReturn(Optional.of(productoA));

        DescuentoStockDTO solicitud =
                new DescuentoStockDTO(
                        125L,
                        List.of(
                                new StockItemDTO("GA001", 2),
                                new StockItemDTO("GA001", 3)
                        )
                );

        stockService.descontar(solicitud);

        assertEquals(5, productoA.getStock());

        verify(productoRepository, times(1))
                .save(productoA);
    }
}