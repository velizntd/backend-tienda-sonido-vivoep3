-- Mensajes del formulario de contacto.
-- usuario_id guarda el claim sub del JWT de quien envió el mensaje.
-- No tiene clave foránea hacia usuarios para no acoplar las migraciones de dos microservicios.
CREATE TABLE contactos (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    usuario_id  VARCHAR(36)  NULL,
    nombre      VARCHAR(100) NOT NULL,
    correo      VARCHAR(100) NULL,
    comentario  VARCHAR(500) NOT NULL,
    fecha_envio DATETIME(6)  NOT NULL,
    CONSTRAINT pk_contactos PRIMARY KEY (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;
