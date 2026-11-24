CREATE TABLE categorias (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL UNIQUE,
    descripcion VARCHAR(255)
);

CREATE TABLE productos (
    codigo VARCHAR(30) PRIMARY KEY,
    categoria_id BIGINT NOT NULL,
    nombre VARCHAR(150) NOT NULL,
    marca VARCHAR(100) NOT NULL,
    modelo VARCHAR(100) NOT NULL,
    stock INT NOT NULL,
    precio DECIMAL(12,2) NOT NULL,
    descripcion TEXT NOT NULL,
    imagen VARCHAR(500) NOT NULL,

    CONSTRAINT fk_producto_categoria
        FOREIGN KEY (categoria_id) REFERENCES categorias(id),

    CONSTRAINT chk_producto_stock CHECK (stock >= 0),
    CONSTRAINT chk_producto_precio CHECK (precio >= 0)
);