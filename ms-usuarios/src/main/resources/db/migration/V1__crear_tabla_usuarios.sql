-- ms-usuarios es el dueño de la tabla usuarios.
-- ms-registro la usa para registrar clientes e iniciar sesión, sin crearla de nuevo.
CREATE TABLE usuarios (
    id               VARCHAR(36)  NOT NULL,
    rut              VARCHAR(9)   NOT NULL,
    nombre           VARCHAR(50)  NOT NULL,
    apellidos        VARCHAR(100) NOT NULL,
    correo           VARCHAR(100) NOT NULL,
    fecha_nacimiento DATE         NULL,
    region           VARCHAR(100) NOT NULL,
    comuna           VARCHAR(100) NOT NULL,
    direccion        VARCHAR(300) NOT NULL,
    rol              VARCHAR(20)  NOT NULL,
    activo           BOOLEAN      NOT NULL DEFAULT TRUE,
    fecha_registro   DATETIME(6)  NOT NULL,
    password_hash    VARCHAR(100) NOT NULL,
    CONSTRAINT pk_usuarios PRIMARY KEY (id),
    CONSTRAINT uk_usuarios_rut UNIQUE (rut),
    CONSTRAINT uk_usuarios_correo UNIQUE (correo),
    CONSTRAINT ck_usuarios_rol CHECK (rol IN ('cliente', 'vendedor', 'administrador'))
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;
