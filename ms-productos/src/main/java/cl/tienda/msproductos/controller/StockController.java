package cl.tienda.msproductos.controller;

import cl.tienda.msproductos.dto.DescuentoStockDTO;
import cl.tienda.msproductos.dto.RespuestaStockDTO;
import cl.tienda.msproductos.service.StockService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/productos/stock")
@RequiredArgsConstructor
public class StockController {

    private final StockService stockService;

    @PostMapping("/descontar")
    public RespuestaStockDTO descontar(
            @Valid @RequestBody DescuentoStockDTO solicitud) {

        return stockService.descontar(solicitud);
    }
}