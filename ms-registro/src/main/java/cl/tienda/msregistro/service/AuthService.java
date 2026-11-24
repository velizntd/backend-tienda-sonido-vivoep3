package cl.tienda.msregistro.service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Locale;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cl.tienda.msregistro.dto.LoginRequest;
import cl.tienda.msregistro.dto.LoginResponse;
import cl.tienda.msregistro.dto.RegistroRequest;
import cl.tienda.msregistro.dto.RegistroResponse;
import cl.tienda.msregistro.exception.CredencialesInvalidasException;
import cl.tienda.msregistro.exception.DuplicadoException;
import cl.tienda.msregistro.model.Usuario;
import cl.tienda.msregistro.repository.UsuarioRepository;

@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService) {

        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public RegistroResponse registrar(RegistroRequest request) {

        String rut = RutUtil.normalizar(request.rut());

        String correo = request.correo()
                .trim()
                .toLowerCase(Locale.ROOT);

        if (usuarioRepository.existsByRut(rut)) {
            throw new DuplicadoException(
                    "El RUT ya se encuentra registrado");
        }

        if (usuarioRepository.existsByCorreo(correo)) {
            throw new DuplicadoException(
                    "El correo ya se encuentra registrado");
        }

        Usuario usuario = new Usuario();

        usuario.setId(UUID.randomUUID().toString());
        usuario.setRut(rut);
        usuario.setNombre(request.nombre().trim());
        usuario.setApellidos(request.apellidos().trim());
        usuario.setCorreo(correo);
        usuario.setFechaNacimiento(request.fechaNacimiento());
        usuario.setRegion(request.region().trim());
        usuario.setComuna(request.comuna().trim());
        usuario.setDireccion(request.direccion().trim());

        usuario.setRol("cliente");
        usuario.setActivo(true);
        usuario.setFechaRegistro(
                LocalDateTime.now(ZoneOffset.UTC));

        usuario.setPasswordHash(
                passwordEncoder.encode(request.contrasena()));

        usuarioRepository.save(usuario);

        return new RegistroResponse(
                usuario.getId(),
                usuario.getNombre(),
                usuario.getCorreo(),
                usuario.getRol(),
                "Usuario registrado correctamente");
    }

    @Transactional(readOnly = true)
    public LoginResponse iniciarSesion(LoginRequest request) {

        String correo = request.correo()
                .trim()
                .toLowerCase(Locale.ROOT);

        Usuario usuario = usuarioRepository
                .findByCorreo(correo)
                .orElseThrow(CredencialesInvalidasException::new);

        if (!usuario.isActivo()) {
            throw new CredencialesInvalidasException();
        }

        if (!passwordEncoder.matches(
                request.contrasena(),
                usuario.getPasswordHash())) {

            throw new CredencialesInvalidasException();
        }

        String token = jwtService.generarToken(
                usuario.getId(),
                usuario.getRol());

        return new LoginResponse(
                token,
                "Bearer",
                900,
                usuario.getId(),
                usuario.getRol());
    }
}