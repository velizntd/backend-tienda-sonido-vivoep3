package cl.tienda.msproductos.controller;

import cl.tienda.msproductos.dto.ProductoDTO;
import cl.tienda.msproductos.service.ProductoService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/productos")
@RequiredArgsConstructor
public class ProductoController {

    private final ProductoService productoService;

    @GetMapping
    public List<ProductoDTO> listar() {
        return productoService.listar();
    }

    @GetMapping("/{codigo}")
    public ProductoDTO buscarPorCodigo(
            @PathVariable String codigo) {

        return productoService.buscarPorCodigo(codigo);
    }

    @GetMapping("/categoria")
    public List<ProductoDTO> buscarPorCategoria(
            @RequestParam String nombre) {

        return productoService.buscarPorCategoria(nombre);
    }

    @GetMapping("/buscar")
    public List<ProductoDTO> buscarPorNombre(
            @RequestParam String nombre) {

        return productoService.buscarPorNombre(nombre);
    }

    @GetMapping("/marca")
    public List<ProductoDTO> buscarPorMarca(
            @RequestParam String nombre) {

        return productoService.buscarPorMarca(nombre);
    }

    @GetMapping("/disponibles")
    public List<ProductoDTO> listarDisponibles() {
        return productoService.listarDisponibles();
    }

    @PostMapping
    public ResponseEntity<ProductoDTO> crear(
            @Valid @RequestBody ProductoDTO producto) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(productoService.crear(producto));
    }

    @PutMapping("/{codigo}")
    public ProductoDTO actualizar(
            @PathVariable String codigo,
            @Valid @RequestBody ProductoDTO producto) {

        return productoService.actualizar(codigo, producto);
    }

    @DeleteMapping("/{codigo}")
    public ResponseEntity<Void> eliminar(
            @PathVariable String codigo) {

        productoService.eliminar(codigo);

        return ResponseEntity.noContent().build();
    }
}