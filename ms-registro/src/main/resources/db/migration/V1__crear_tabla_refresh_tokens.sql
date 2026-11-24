CREATE TABLE refresh_tokens (
    id VARCHAR(36) NOT NULL,
    usuario_id VARCHAR(36) NOT NULL,
    token_hash VARCHAR(64) NOT NULL,
    fecha_creacion DATETIME(6) NOT NULL,
    fecha_expiracion DATETIME(6) NOT NULL,
    revocado BOOLEAN NOT NULL DEFAULT FALSE,

    CONSTRAINT pk_refresh_tokens
        PRIMARY KEY (id),

    CONSTRAINT uk_refresh_tokens_hash
        UNIQUE (token_hash),

    CONSTRAINT fk_refresh_tokens_usuario
        FOREIGN KEY (usuario_id)
        REFERENCES usuarios(id)
        ON DELETE CASCADE
) ENGINE = InnoDB
DEFAULT CHARSET = utf8mb4
COLLATE = utf8mb4_unicode_ci;