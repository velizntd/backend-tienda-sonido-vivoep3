package cl.tienda.msproductos.controller;

import cl.tienda.msproductos.dto.CategoriaDTO;
import cl.tienda.msproductos.service.CategoriaService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categorias")
@RequiredArgsConstructor
public class CategoriaController {

    private final CategoriaService categoriaService;

    @GetMapping
    public List<CategoriaDTO> listar() {
        return categoriaService.listar();
    }

    @GetMapping("/{id}")
    public CategoriaDTO buscarPorId(
            @PathVariable Long id) {

        return categoriaService.buscarPorId(id);
    }

    @PostMapping
    public ResponseEntity<CategoriaDTO> crear(
            @Valid @RequestBody CategoriaDTO categoria) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(categoriaService.crear(categoria));
    }

    @PutMapping("/{id}")
    public CategoriaDTO actualizar(
            @PathVariable Long id,
            @Valid @RequestBody CategoriaDTO categoria) {

        return categoriaService.actualizar(id, categoria);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(
            @PathVariable Long id) {

        categoriaService.eliminar(id);

        return ResponseEntity.noContent().build();
    }
}