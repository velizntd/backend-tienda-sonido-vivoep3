# Sonido Vivo · Backend

Backend de **Sonido Vivo**, tienda de instrumentos y equipos musicales. Evaluación Parcial 3 de DSY1104 Desarrollo Fullstack II, Duoc UC.

Frontend (React): https://github.com/dzzetafps/tienda-sonido-vivo

## Arquitectura

```text
Frontend React (5173)
        │  todas las peticiones van a http://localhost:8080/api/...
        ▼
api-gateway (8080) ──consulta──► eureka-server (8761)
        │                              ▲
        │  enruta por nombre           │ cada microservicio se registra al arrancar
        ▼                              │
ms-productos · ms-novedades · ms-contacto · ms-carrito · ms-usuarios · ms-registro · ms-blogs
        │
        ▼
MySQL · base de datos sonidovivo
```

Cada carpeta es un proyecto Spring Boot independiente. Los microservicios se conectan a la misma base de datos MySQL, `sonidovivo`, y cada uno trabaja con sus propias tablas.

## Proyectos

| Proyecto | Puerto | Ruta en el gateway | Métodos | Responsable |
|---|---|---|---|---|
| `eureka-server` | 8761 | — | — | Vicente |
| `api-gateway` | 8080 | — | — | Vicente |
| `ms-productos` | 8081 | `/api/productos/**` | GET, POST, PUT, DELETE | Vicente |
| `ms-novedades` | 8082 | `/api/novedades/**` | GET, POST, PUT, DELETE | Vicente |
| `ms-contacto` | 8083 | `/api/contactos/**` | GET, POST | Felipe |
| `ms-carrito` | 8084 | `/api/carrito/**` | GET, POST, PUT, DELETE | Sebastián |
| `ms-usuarios` | 8085 | `/api/usuarios/**` | GET, POST, PUT, DELETE | Felipe |
| `ms-registro` | 8086 | `/api/auth/**` | POST | Sebastián |
| `ms-blogs` | 8087 | `/api/blogs/**` | GET, POST, PUT, DELETE | Felipe |

- Panel de Eureka: http://localhost:8761
- Swagger de cada microservicio: `http://localhost:<puerto>/swagger-ui/index.html`
- `ms-usuarios` y `ms-registro` usan la misma tabla `usuarios`: `ms-usuarios` la crea y la administra; `ms-registro` registra clientes e inicia sesión.

## Tecnologías

- Java 17 o superior
- Spring Boot: Spring Web, Spring Data JPA, Validation
- Spring Cloud: Eureka Server, Eureka Discovery Client, Gateway
- MySQL
- springdoc-openapi (Swagger)
- BCrypt (`spring-security-crypto`) para cifrar contraseñas en `ms-usuarios` y `ms-registro`
- Maven

## Convenciones del equipo

- Todos los proyectos se generan en start.spring.io con la **misma versión de Spring Boot**. Así Spring Cloud queda en una versión compatible en los nueve.
- `spring.application.name` igual al nombre de la carpeta (por ejemplo `ms-usuarios`). El gateway usa ese nombre para enrutar.
- Cada microservicio incluye **Eureka Discovery Client** y se registra en `http://localhost:8761/eureka`.
- El frontend llama **solo al gateway** (`http://localhost:8080/api`). CORS se configura **solo en `api-gateway`**; si un microservicio también lo configura, el navegador rechaza la respuesta por cabeceras duplicadas.
- Capas: `model`, `repository`, `service`, `controller`.
- El JSON usa los mismos campos que los tipos del frontend (`Producto.ts`, `Usuario.ts`, `Articulo.ts`, `MensajeContacto.ts`).
- Errores con el formato `{ "mensaje": "..." }` y códigos 400, 404 o 409.
- Todos los endpoints quedan documentados en Swagger.
- Las contraseñas se guardan cifradas con BCrypt y nunca se devuelven en las respuestas.

## Requisitos

- JDK 17 o superior.
- MySQL en ejecución en `localhost:3306`.
- Maven, o el wrapper `mvnw` que trae cada proyecto.

## Cómo ejecutar

Orden de arranque:

1. MySQL.
2. `eureka-server`.
3. Los microservicios (`ms-...`). Revisar en http://localhost:8761 que aparezcan registrados.
4. `api-gateway`.
5. El frontend: `npm run dev` dentro de `frontend/` del repositorio del frontend.

Para iniciar cada proyecto, entrar a su carpeta y ejecutar:

- Windows: `.\mvnw spring-boot:run`
- Linux o macOS: `./mvnw spring-boot:run`

## Ramas

- `main`: versión entregada.
- `evaluacion-3`: integración. Todos los Pull Request van dirigidos a esta rama.
- `feature/<nombre>-<tarea>`: una rama por tarea, por ejemplo `feature/felipe-ms-usuarios-base`.

## Equipo

- Vicente: `eureka-server`, `api-gateway`, `ms-productos`, `ms-novedades`
- Sebastián: `ms-carrito`, `ms-registro`
- Felipe: `ms-usuarios`, `ms-contacto`, `ms-blogs`
