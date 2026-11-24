package cl.tienda.msusuarios.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cl.tienda.msusuarios.dto.EstadoRequest;
import cl.tienda.msusuarios.dto.RolRequest;
import cl.tienda.msusuarios.dto.UsuarioRequest;
import cl.tienda.msusuarios.dto.UsuarioResponse;
import cl.tienda.msusuarios.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/usuarios")
@Tag(name = "Usuarios", description = "Administración de usuarios del panel. Requiere rol administrador.")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping
    @Operation(summary = "Listar usuarios", description = "Devuelve todos los usuarios, del más reciente al más antiguo, sin contraseñas.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de usuarios"),
            @ApiResponse(responseCode = "401", description = "Falta el token o no es válido"),
            @ApiResponse(responseCode = "403", description = "El token no tiene rol administrador")
    })
    public List<UsuarioResponse> listar() {
        return usuarioService.listar();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar usuario por id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuario encontrado"),
            @ApiResponse(responseCode = "404", description = "El usuario no existe")
    })
    public UsuarioResponse buscar(@PathVariable("id") String id) {
        return usuarioService.buscarPorId(id);
    }

    @PostMapping
    @Operation(summary = "Crear usuario", description = "Crea un usuario con cualquier rol y contraseña temporal cifrada con BCrypt.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Usuario creado"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos (RUT, correo, contraseña, rol)"),
            @ApiResponse(responseCode = "409", description = "RUT o correo ya registrado")
    })
    public ResponseEntity<UsuarioResponse> crear(@Valid @RequestBody UsuarioRequest datos) {
        return ResponseEntity.status(HttpStatus.CREATED).body(usuarioService.crear(datos));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Editar usuario", description = "Actualiza los datos. El RUT no cambia; la contraseña vacía se mantiene.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuario actualizado"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "404", description = "El usuario no existe"),
            @ApiResponse(responseCode = "409", description = "Correo duplicado o intento de quitarse el rol propio")
    })
    public UsuarioResponse actualizar(@PathVariable("id") String id,
            @Valid @RequestBody UsuarioRequest datos,
            @AuthenticationPrincipal Jwt jwt) {
        return usuarioService.actualizar(id, datos, jwt.getSubject());
    }

    @PatchMapping("/{id}/estado")
    @Operation(summary = "Activar o desactivar usuario", description = "Desactivación lógica: un usuario inactivo no puede iniciar sesión.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Estado actualizado"),
            @ApiResponse(responseCode = "404", description = "El usuario no existe"),
            @ApiResponse(responseCode = "409", description = "No se puede desactivar la cuenta propia")
    })
    public UsuarioResponse cambiarEstado(@PathVariable("id") String id,
            @Valid @RequestBody EstadoRequest datos,
            @AuthenticationPrincipal Jwt jwt) {
        return usuarioService.cambiarEstado(id, datos.activo(), jwt.getSubject());
    }

    @PatchMapping("/{id}/rol")
    @Operation(summary = "Asignar rol", description = "Asigna cliente, vendedor o administrador.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Rol actualizado"),
            @ApiResponse(responseCode = "400", description = "Rol inválido"),
            @ApiResponse(responseCode = "404", description = "El usuario no existe"),
            @ApiResponse(responseCode = "409", description = "No se puede quitar el rol administrador propio")
    })
    public UsuarioResponse cambiarRol(@PathVariable("id") String id,
            @Valid @RequestBody RolRequest datos,
            @AuthenticationPrincipal Jwt jwt) {
        return usuarioService.cambiarRol(id, datos.rol(), jwt.getSubject());
    }
}
