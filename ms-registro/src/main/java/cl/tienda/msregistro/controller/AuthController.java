package cl.tienda.msregistro.controller;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;

import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import cl.tienda.msregistro.dto.LoginRequest;
import cl.tienda.msregistro.dto.LoginResponse;
import cl.tienda.msregistro.dto.RegistroRequest;
import cl.tienda.msregistro.dto.RegistroResponse;
import cl.tienda.msregistro.dto.UsuarioResponse;

import cl.tienda.msregistro.service.AuthService;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Autenticacion", description = "Registro y acceso de usuarios")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @Operation(summary = "Registrar un nuevo cliente")
    @ApiResponse(responseCode = "201", description = "Usuario registrado")
    @ApiResponse(responseCode = "400", description = "Datos invalidos")
    @ApiResponse(responseCode = "409", description = "Usuario duplicado")
    @PostMapping("/registro")
    public ResponseEntity<RegistroResponse> registrar(
            @Valid @RequestBody RegistroRequest request) {

        RegistroResponse respuesta =
                authService.registrar(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(respuesta);
    }

    @Operation(summary = "Iniciar sesion y obtener JWT")
    @ApiResponse(responseCode = "200", description = "Login exitoso")
    @ApiResponse(responseCode = "400", description = "Datos invalidos")
    @ApiResponse(responseCode = "401", description = "Credenciales incorrectas")
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> iniciarSesion(
            @Valid @RequestBody LoginRequest request) {

        LoginResponse respuesta =
                authService.iniciarSesion(request);

        return ResponseEntity.ok(respuesta);
    }

    @Operation(summary = "Obtener usuario autenticado")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponse(responseCode = "200", description = "Usuario autenticado")
    @ApiResponse(responseCode = "401", description = "No autorizado")
    @GetMapping("/me")
    public ResponseEntity<UsuarioResponse> obtenerUsuarioActual(
            @AuthenticationPrincipal Jwt jwt) {

        UsuarioResponse respuesta =
                authService.obtenerUsuarioActual(jwt.getSubject());

        return ResponseEntity.ok(respuesta);
    }
}