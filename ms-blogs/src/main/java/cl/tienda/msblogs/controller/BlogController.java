package cl.tienda.msblogs.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cl.tienda.msblogs.dto.BlogRequest;
import cl.tienda.msblogs.dto.BlogResponse;
import cl.tienda.msblogs.service.BlogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/blogs")
@Tag(name = "Blogs", description = "Artículos del blog. Leer requiere sesión; crear, editar y eliminar requieren rol administrador.")
public class BlogController {

    private final BlogService blogService;

    public BlogController(BlogService blogService) {
        this.blogService = blogService;
    }

    @GetMapping
    @Operation(summary = "Listar artículos", description = "Del más reciente al más antiguo.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de artículos"),
            @ApiResponse(responseCode = "401", description = "Falta el token o no es válido")
    })
    public List<BlogResponse> listar() {
        return blogService.listar();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Ver artículo")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Artículo encontrado"),
            @ApiResponse(responseCode = "404", description = "El artículo no existe")
    })
    public BlogResponse buscar(@PathVariable("id") String id) {
        return blogService.buscarPorId(id);
    }

    @PostMapping
    @Operation(summary = "Crear artículo", description = "Solo administrador. El id se genera desde el título.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Artículo creado"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "403", description = "El token no tiene rol administrador"),
            @ApiResponse(responseCode = "409", description = "Ya existe un artículo con ese título")
    })
    public ResponseEntity<BlogResponse> crear(@Valid @RequestBody BlogRequest datos) {
        return ResponseEntity.status(HttpStatus.CREATED).body(blogService.crear(datos));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Editar artículo", description = "Solo administrador. Reemplaza los datos y las secciones.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Artículo actualizado"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "404", description = "El artículo no existe")
    })
    public BlogResponse actualizar(@PathVariable("id") String id, @Valid @RequestBody BlogRequest datos) {
        return blogService.actualizar(id, datos);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar artículo", description = "Solo administrador. Borra también sus secciones.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Artículo eliminado"),
            @ApiResponse(responseCode = "404", description = "El artículo no existe")
    })
    public ResponseEntity<Void> eliminar(@PathVariable("id") String id) {
        blogService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
