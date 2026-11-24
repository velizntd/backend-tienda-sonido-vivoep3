package cl.tienda.msusuarios.config;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import cl.tienda.msusuarios.model.Roles;
import cl.tienda.msusuarios.model.Usuario;
import cl.tienda.msusuarios.repository.UsuarioRepository;

@Component
public class AdministradorInicial implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(AdministradorInicial.class);

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final String correo;
    private final String contrasena;

    public AdministradorInicial(UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder,
            @Value("${app.admin.correo}") String correo,
            @Value("${app.admin.password}") String contrasena) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.correo = correo.trim().toLowerCase();
        this.contrasena = contrasena;
    }

    @Override
    public void run(String... args) {
        if (usuarioRepository.existsByRolAndActivoTrue(Roles.ADMINISTRADOR)) {
            return;
        }
        if (contrasena == null || contrasena.isBlank()) {
            log.warn("No hay administradores activos. Define ADMIN_PASSWORD (4 a 10 caracteres) y reinicia ms-usuarios para crear {}.", correo);
            return;
        }
        if (contrasena.length() < 4 || contrasena.length() > 10) {
            log.warn("ADMIN_PASSWORD debe tener entre 4 y 10 caracteres. No se creó el administrador inicial.");
            return;
        }
        if (usuarioRepository.existsByCorreo(correo) || usuarioRepository.existsByRut("900000014")) {
            log.warn("Ya existe un usuario con el correo o RUT del administrador inicial. Actívalo desde la base de datos.");
            return;
        }

        Usuario admin = new Usuario();
        admin.setId(UUID.randomUUID().toString());
        admin.setRut("900000014");
        admin.setNombre("Administrador");
        admin.setApellidos("Demostración");
        admin.setCorreo(correo);
        admin.setRegion("Región de Valparaíso");
        admin.setComuna("Viña del Mar");
        admin.setDireccion("Dirección ficticia 100");
        admin.setRol(Roles.ADMINISTRADOR);
        admin.setActivo(true);
        admin.setFechaRegistro(LocalDateTime.now(ZoneOffset.UTC));
        admin.setPasswordHash(passwordEncoder.encode(contrasena));
        usuarioRepository.save(admin);

        log.info("Administrador inicial creado: {}", correo);
    }
}
