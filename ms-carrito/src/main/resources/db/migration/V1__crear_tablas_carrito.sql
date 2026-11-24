CREATE TABLE carritos (
    id VARCHAR(36) NOT NULL,
    usuario_id VARCHAR(36) NOT NULL,
    fecha_creacion DATETIME(6) NOT NULL,
    fecha_actualizacion DATETIME(6) NOT NULL,

    CONSTRAINT pk_carritos
        PRIMARY KEY (id),

    CONSTRAINT uk_carritos_usuario
        UNIQUE (usuario_id),

    CONSTRAINT fk_carritos_usuario
        FOREIGN KEY (usuario_id)
        REFERENCES usuarios(id)
        ON DELETE CASCADE

) ENGINE = InnoDB
DEFAULT CHARSET = utf8mb4
COLLATE = utf8mb4_unicode_ci;


CREATE TABLE detalle_carrito (
    id VARCHAR(36) NOT NULL,
    carrito_id VARCHAR(36) NOT NULL,
    codigo_producto VARCHAR(50) NOT NULL,
    cantidad INT NOT NULL,

    CONSTRAINT pk_detalle_carrito
        PRIMARY KEY (id),

    CONSTRAINT fk_detalle_carrito_carrito
        FOREIGN KEY (carrito_id)
        REFERENCES carritos(id)
        ON DELETE CASCADE,

    CONSTRAINT uk_carrito_producto
        UNIQUE (carrito_id, codigo_producto),

    CONSTRAINT chk_carrito_cantidad
        CHECK (cantidad > 0)

) ENGINE = InnoDB
DEFAULT CHARSET = utf8mb4
COLLATE = utf8mb4_unicode_ci;