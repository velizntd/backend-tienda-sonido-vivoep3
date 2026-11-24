CREATE TABLE novedades (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    titulo VARCHAR(150) NOT NULL,
    descripcion TEXT NOT NULL,
    tipo VARCHAR(30) NOT NULL,
    imagen VARCHAR(500) NOT NULL,
    codigo_producto VARCHAR(30),
    fecha_inicio DATE NOT NULL,
    fecha_fin DATE,
    activo BOOLEAN NOT NULL DEFAULT TRUE,

    CONSTRAINT chk_novedad_fechas
        CHECK (fecha_fin IS NULL OR fecha_fin >= fecha_inicio),

    CONSTRAINT chk_novedad_tipo
        CHECK (tipo IN ('NOVEDAD', 'PROMOCION', 'DESTACADO'))
);