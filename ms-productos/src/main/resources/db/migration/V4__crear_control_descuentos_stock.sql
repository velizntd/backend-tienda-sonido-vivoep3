CREATE TABLE descuentos_stock (
    pedido_id BIGINT PRIMARY KEY,
    huella_solicitud CHAR(64) NOT NULL,
    procesado BOOLEAN NOT NULL DEFAULT FALSE,
    fecha_registro TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;