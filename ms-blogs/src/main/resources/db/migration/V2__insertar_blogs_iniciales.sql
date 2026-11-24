-- Carga inicial: los 2 artículos que el frontend tenía fijos en src/datos/articulos.ts.
INSERT INTO blogs (id, titulo, resumen, introduccion, imagen, descripcion_imagen, fecha_publicacion) VALUES
('como-elegir-tu-primera-guitarra', 'Cómo elegir tu primera guitarra', 'Elegir una guitarra por primera vez puede ser difícil. Te contamos los aspectos importantes que debes considerar antes de comprar tu primer instrumento.', 'Comprar tu primera guitarra es una decisión importante, especialmente si estás comenzando a aprender a tocar. Existen distintos tipos de guitarras y cada una tiene características diferentes.', '/img/productos/yamaha-c40-GA003.jpg', 'Guitarra clásica Yamaha C40', '2026-11-20 12:00:00.000000');

INSERT INTO blog_secciones (blog_id, orden, titulo, texto) VALUES
('como-elegir-tu-primera-guitarra', 0, 'Define qué tipo de guitarra necesitas', 'Antes de comprar una guitarra debes considerar el estilo de música que quieres tocar. Las guitarras acústicas, clásicas y eléctricas ofrecen experiencias diferentes.'),
('como-elegir-tu-primera-guitarra', 1, 'Considera tu presupuesto', 'No es necesario comprar el instrumento más costoso para comenzar. Existen guitarras para principiantes que ofrecen una buena relación entre calidad y precio.'),
('como-elegir-tu-primera-guitarra', 2, 'Comodidad del instrumento', 'Es importante que la guitarra sea cómoda para ti. El tamaño del cuerpo, el peso y el ancho del mástil pueden influir en la facilidad con la que aprendas.'),
('como-elegir-tu-primera-guitarra', 3, 'Revisa la calidad', 'Antes de comprar, revisa el estado general del instrumento, sus cuerdas, clavijas y terminaciones. Una guitarra bien ajustada facilita mucho el aprendizaje.'),
('como-elegir-tu-primera-guitarra', 4, 'Busca asesoría', 'Si tienes dudas, puedes pedir orientación antes de realizar tu compra. Una buena recomendación puede ayudarte a elegir un instrumento adecuado para comenzar.');

INSERT INTO blogs (id, titulo, resumen, introduccion, imagen, descripcion_imagen, fecha_publicacion) VALUES
('cuidado-de-instrumentos', 'Consejos para cuidar tus instrumentos musicales', 'Mantener tus instrumentos en buenas condiciones prolonga su vida útil y conserva una buena calidad de sonido. Conoce algunos consejos para su cuidado y mantenimiento.', 'Mantener tus instrumentos musicales en buenas condiciones ayuda a prolongar su vida útil y a conservar una buena calidad de sonido.', '/img/productos/hercules-gs302b-AC009.jpg', 'Soporte de piso para apoyar una guitarra de forma estable', '2026-11-19 12:00:00.000000');

INSERT INTO blog_secciones (blog_id, orden, titulo, texto) VALUES
('cuidado-de-instrumentos', 0, 'Limpia tu instrumento regularmente', 'Después de usar tu instrumento es recomendable limpiarlo para eliminar el polvo, la suciedad y la humedad acumulada durante su uso.'),
('cuidado-de-instrumentos', 1, 'Guárdalo correctamente', 'Usa una funda o un estuche adecuado para proteger tu instrumento de golpes, polvo y cambios de temperatura.'),
('cuidado-de-instrumentos', 2, 'Evita la humedad', 'La humedad puede afectar materiales como la madera y algunas piezas metálicas. Guarda tus instrumentos en lugares secos y protegidos.'),
('cuidado-de-instrumentos', 3, 'Revisa las piezas periódicamente', 'Revisa cuerdas, clavijas, cables, conectores y otras piezas para detectar desgastes antes de que se conviertan en un problema mayor.'),
('cuidado-de-instrumentos', 4, 'Realiza mantenimiento cuando sea necesario', 'Si notas cambios en el sonido o problemas de funcionamiento, es recomendable realizar una revisión o mantenimiento del instrumento.');
