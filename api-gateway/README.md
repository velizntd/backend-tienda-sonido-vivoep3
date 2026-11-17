# api-gateway

Puerta de entrada única del backend (Spring Cloud Gateway).

- Puerto: 8080
- Responsable: Vicente
- Contenido: recibe todas las peticiones del frontend en `http://localhost:8080/api/...` y las envía al microservicio que corresponde, buscándolo en Eureka por su nombre. Aquí se configura CORS para `http://localhost:5173`.

| Ruta | Microservicio |
|---|---|
| `/api/productos/**` | `ms-productos` |
| `/api/novedades/**` | `ms-novedades` |
| `/api/contactos/**` | `ms-contacto` |
| `/api/carrito/**` | `ms-carrito` |
| `/api/usuarios/**` | `ms-usuarios` |
| `/api/auth/**` | `ms-registro` |
| `/api/blogs/**` | `ms-blogs` |
