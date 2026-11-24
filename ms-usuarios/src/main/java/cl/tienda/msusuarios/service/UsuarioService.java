package cl.tienda.msusuarios.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cl.tienda.msusuarios.dto.UsuarioRequest;
import cl.tienda.msusuarios.dto.UsuarioResponse;
import cl.tienda.msusuarios.exception.ConflictoException;
import cl.tienda.msusuarios.exception.DatosInvalidosException;
import cl.tienda.msusuarios.exception.RecursoNoEncontradoException;
import cl.tienda.msusuarios.model.Roles;
import cl.tienda.msusuarios.model.Usuario;
import cl.tienda.msusuarios.repository.UsuarioRepository;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<UsuarioResponse> listar() {
        return usuarioRepository.findAllByOrderByFechaRegistroDesc().stream()
                .map(UsuarioResponse::desde)
                .toList();
    }

    @Transactional(readOnly = true)
    public UsuarioResponse buscarPorId(String id) {
        return UsuarioResponse.desde(obtener(id));
    }

    @Transactional
    public UsuarioResponse crear(UsuarioRequest datos) {
        String rut = RutUtil.normalizar(datos.rut());
        String correo = normalizarCorreo(datos.correo());

        if (usuarioRepository.existsByRut(rut)) {
            throw new ConflictoException("Ya existe un usuario con ese RUT.");
        }
        if (usuarioRepository.existsByCorreo(correo)) {
            throw new ConflictoException("Ya existe un usuario con ese correo.");
        }
        validarContrasena(datos.contrasena());

        Usuario usuario = new Usuario();
        usuario.setId(UUID.randomUUID().toString());
        usuario.setRut(rut);
        copiarDatos(datos, correo, usuario);
        usuario.setActivo(true);
        usuario.setFechaRegistro(LocalDateTime.now(ZoneOffset.UTC));
        usuario.setPasswordHash(passwordEncoder.encode(datos.contrasena()));

        return UsuarioResponse.desde(usuarioRepository.save(usuario));
    }

    @Transactional
    public UsuarioResponse actualizar(String id, UsuarioRequest datos, String idAutenticado) {
        Usuario usuario = obtener(id);

        if (!usuario.getRut().equals(RutUtil.normalizar(datos.rut()))) {
            throw new DatosInvalidosException("El RUT no se puede modificar.");
        }
        String correo = normalizarCorreo(datos.correo());
        if (usuarioRepository.existsByCorreoAndIdNot(correo, id)) {
            throw new ConflictoException("Ya existe un usuario con ese correo.");
        }
        validarNoQuitarseAdministrador(id, datos.rol(), idAutenticado);

        copiarDatos(datos, correo, usuario);
        if (datos.contrasena() != null && !datos.contrasena().isEmpty()) {
            validarContrasena(datos.contrasena());
            usuario.setPasswordHash(passwordEncoder.encode(datos.contrasena()));
        }
        return UsuarioResponse.desde(usuarioRepository.save(usuario));
    }

    @Transactional
    public UsuarioResponse cambiarEstado(String id, boolean activo, String idAutenticado) {
        Usuario usuario = obtener(id);
        if (!activo && id.equals(idAutenticado)) {
            throw new ConflictoException("No puedes desactivar tu propia cuenta mientras tienes la sesión iniciada.");
        }
        usuario.setActivo(activo);
        return UsuarioResponse.desde(usuarioRepository.save(usuario));
    }

    @Transactional
    public UsuarioResponse cambiarRol(String id, String rol, String idAutenticado) {
        Usuario usuario = obtener(id);
        validarRol(rol);
        validarNoQuitarseAdministrador(id, rol, idAutenticado);
        usuario.setRol(rol);
        return UsuarioResponse.desde(usuarioRepository.save(usuario));
    }

    private Usuario obtener(String id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró el usuario solicitado."));
    }

    private void copiarDatos(UsuarioRequest datos, String correo, Usuario usuario) {
        validarRol(datos.rol());
        usuario.setNombre(datos.nombre().trim());
        usuario.setApellidos(datos.apellidos().trim());
        usuario.setCorreo(correo);
        usuario.setFechaNacimiento(convertirFecha(datos.fechaNacimiento()));
        usuario.setRegion(datos.region().trim());
        usuario.setComuna(datos.comuna().trim());
        usuario.setDireccion(datos.direccion().trim());
        usuario.setRol(datos.rol());
    }

    private void validarNoQuitarseAdministrador(String id, String rolNuevo, String idAutenticado) {
        if (id.equals(idAutenticado) && !Roles.ADMINISTRADOR.equals(rolNuevo)) {
            throw new ConflictoException("No puedes quitarte el rol de administrador mientras tienes la sesión iniciada.");
        }
    }

    private void validarRol(String rol) {
        if (!Roles.VALIDOS.contains(rol)) {
            throw new DatosInvalidosException("El rol debe ser cliente, vendedor o administrador.");
        }
    }

    private void validarContrasena(String contrasena) {
        if (contrasena == null || contrasena.isBlank() || contrasena.length() < 4 || contrasena.length() > 10) {
            throw new DatosInvalidosException("La contraseña debe tener entre 4 y 10 caracteres.");
        }
    }

    private String normalizarCorreo(String correo) {
        return correo.trim().toLowerCase(Locale.ROOT);
    }

    private LocalDate convertirFecha(String fecha) {
        if (fecha == null || fecha.isBlank()) {
            return null;
        }
        try {
            LocalDate convertida = LocalDate.parse(fecha);
            if (convertida.isAfter(LocalDate.now())) {
                throw new DatosInvalidosException("La fecha de nacimiento no puede ser futura.");
            }
            return convertida;
        } catch (DateTimeParseException error) {
            throw new DatosInvalidosException("La fecha de nacimiento no es válida. Usa el formato yyyy-MM-dd.");
        }
    }
}
