package cl.tienda.mscontacto.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cl.tienda.mscontacto.dto.ContactoRequest;
import cl.tienda.mscontacto.dto.ContactoResponse;
import cl.tienda.mscontacto.service.ContactoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/contactos")
@Tag(name = "Contacto", description = "Mensajes del formulario de contacto.")
public class ContactoController {

    private final ContactoService contactoService;

    public ContactoController(ContactoService contactoService) {
        this.contactoService = contactoService;
    }

    @PostMapping
    @Operation(summary = "Enviar mensaje de contacto", description = "Requiere sesión iniciada (cualquier rol).")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Mensaje guardado"),
            @ApiResponse(responseCode = "400", description = "Nombre o comentario vacío, correo inválido o textos muy largos"),
            @ApiResponse(responseCode = "401", description = "Falta el token o no es válido")
    })
    public ResponseEntity<ContactoResponse> registrar(@Valid @RequestBody ContactoRequest datos,
            @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.status(HttpStatus.CREATED).body(contactoService.registrar(datos, jwt.getSubject()));
    }

    @GetMapping
    @Operation(summary = "Listar mensajes", description = "Solo administrador. Del más reciente al más antiguo.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de mensajes"),
            @ApiResponse(responseCode = "403", description = "El token no tiene rol administrador")
    })
    public List<ContactoResponse> listar() {
        return contactoService.listar();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Ver un mensaje", description = "Solo administrador.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Mensaje encontrado"),
            @ApiResponse(responseCode = "404", description = "El mensaje no existe")
    })
    public ContactoResponse buscar(@PathVariable("id") Long id) {
        return contactoService.buscarPorId(id);
    }
}
