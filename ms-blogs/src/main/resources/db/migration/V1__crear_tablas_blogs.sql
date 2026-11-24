-- Artículos del blog. Las secciones van en una tabla hija (1FN: sin listas dentro de una columna).
CREATE TABLE blogs (
    id                 VARCHAR(100)  NOT NULL,
    titulo             VARCHAR(150)  NOT NULL,
    resumen            VARCHAR(500)  NOT NULL,
    introduccion       VARCHAR(2000) NOT NULL,
    imagen             VARCHAR(300)  NOT NULL,
    descripcion_imagen VARCHAR(200)  NOT NULL,
    fecha_publicacion  DATETIME(6)   NOT NULL,
    CONSTRAINT pk_blogs PRIMARY KEY (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE blog_secciones (
    blog_id VARCHAR(100)  NOT NULL,
    orden   INT           NOT NULL,
    titulo  VARCHAR(150)  NOT NULL,
    texto   VARCHAR(2000) NOT NULL,
    CONSTRAINT pk_blog_secciones PRIMARY KEY (blog_id, orden),
    CONSTRAINT fk_blog_secciones_blog FOREIGN KEY (blog_id) REFERENCES blogs (id) ON DELETE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;
