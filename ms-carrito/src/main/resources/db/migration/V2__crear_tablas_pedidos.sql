CREATE TABLE pedidos (
    id VARCHAR(36) NOT NULL,
    usuario_id VARCHAR(36) NOT NULL,
    fecha DATETIME(6) NOT NULL,

    estado VARCHAR(20) NOT NULL,
    tipo_entrega VARCHAR(20) NOT NULL,

    nombre_comprador VARCHAR(50) NOT NULL,
    apellido_comprador VARCHAR(100) NOT NULL,
    correo_comprador VARCHAR(100) NOT NULL,

    calle VARCHAR(200),
    numero VARCHAR(20),
    complemento VARCHAR(100),
    region VARCHAR(100),
    comuna VARCHAR(100),
    indicaciones VARCHAR(300),

    total DECIMAL(14,2) NOT NULL,

    CONSTRAINT pk_pedidos
        PRIMARY KEY (id),

    CONSTRAINT fk_pedidos_usuario
        FOREIGN KEY (usuario_id)
        REFERENCES usuarios(id),

    CONSTRAINT chk_pedidos_estado
        CHECK (
            estado IN (
                'pagado',
                'enviado',
                'entregado',
                'cancelado'
            )
        ),

    CONSTRAINT chk_pedidos_entrega
        CHECK (
            tipo_entrega IN (
                'despacho',
                'retiro'
            )
        ),

    CONSTRAINT chk_pedidos_total
        CHECK (total >= 0)

) ENGINE = InnoDB
DEFAULT CHARSET = utf8mb4
COLLATE = utf8mb4_unicode_ci;


CREATE TABLE detalle_pedido (
    id VARCHAR(36) NOT NULL,
    pedido_id VARCHAR(36) NOT NULL,

    codigo_producto VARCHAR(50) NOT NULL,
    nombre_producto VARCHAR(255) NOT NULL,

    cantidad INT NOT NULL,
    precio_unitario DECIMAL(12,2) NOT NULL,

    CONSTRAINT pk_detalle_pedido
        PRIMARY KEY (id),

    CONSTRAINT fk_detalle_pedido_pedido
        FOREIGN KEY (pedido_id)
        REFERENCES pedidos(id)
        ON DELETE CASCADE,

    CONSTRAINT uk_pedido_producto
        UNIQUE (pedido_id, codigo_producto),

    CONSTRAINT chk_pedido_cantidad
        CHECK (cantidad > 0),

    CONSTRAINT chk_pedido_precio
        CHECK (precio_unitario >= 0)

) ENGINE = InnoDB
DEFAULT CHARSET = utf8mb4
COLLATE = utf8mb4_unicode_ci;


CREATE INDEX idx_pedidos_usuario_fecha
ON pedidos (usuario_id, fecha);