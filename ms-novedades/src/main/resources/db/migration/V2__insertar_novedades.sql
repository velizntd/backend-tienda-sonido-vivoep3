INSERT INTO novedades (
    titulo, descripcion, tipo, imagen,
    codigo_producto, fecha_inicio, fecha_fin, activo
) VALUES
(
    'Descubre la Yamaha F310',
    'Conoce nuestra guitarra acústica Yamaha F310.',
    'DESTACADO',
    '/img/productos/yamaha-f310-GA001.jpg',
    'GA001', CURRENT_DATE, NULL, TRUE
),
(
    'Instrumentos para comenzar',
    'Descubre la guitarra clásica Yamaha C40.',
    'NOVEDAD',
    '/img/productos/yamaha-c40-GA003.jpg',
    'GA003', CURRENT_DATE, NULL, TRUE
),
(
    'Equipamiento para grabación',
    'Conoce el micrófono Shure SM58 de nuestro catálogo.',
    'DESTACADO',
    '/img/productos/shure-sm58-MI001.jpg',
    'MI001', CURRENT_DATE, NULL, TRUE
);