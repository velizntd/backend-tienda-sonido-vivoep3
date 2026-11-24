package cl.tienda.msnovedades.controller;

import cl.tienda.msnovedades.dto.NovedadDTO;
import cl.tienda.msnovedades.service.NovedadService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/novedades")
@RequiredArgsConstructor
public class NovedadController {

    private final NovedadService novedadService;

    @GetMapping
    public List<NovedadDTO> listar() {
        return novedadService.listar();
    }

    @GetMapping("/activas")
    public List<NovedadDTO> listarActivas() {
        return novedadService.listarActivas();
    }

    @GetMapping("/{id}")
    public NovedadDTO buscarPorId(@PathVariable Long id) {
        return novedadService.buscarPorId(id);
    }

    @PostMapping
    public ResponseEntity<NovedadDTO> crear(
            @Valid @RequestBody NovedadDTO dto) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(novedadService.crear(dto));
    }

    @PutMapping("/{id}")
    public NovedadDTO actualizar(
            @PathVariable Long id,
            @Valid @RequestBody NovedadDTO dto) {

        return novedadService.actualizar(id, dto);
    }

    @PatchMapping("/{id}/estado")
    public NovedadDTO cambiarEstado(
            @PathVariable Long id,
            @RequestParam Boolean activo) {

        return novedadService.cambiarEstado(id, activo);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(
            @PathVariable Long id) {

        novedadService.eliminar(id);

        return ResponseEntity.noContent().build();
    }
}