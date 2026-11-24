# Sonido Vivo - Backend

Evaluación Parcial 3 - DSY1104 Desarrollo Fullstack II - Duoc UC.

Backend de Sonido Vivo, tienda de instrumentos y equipos musicales,
implementado mediante una arquitectura de microservicios.

## Repositorios

- Frontend: https://github.com/dzzetafps/tienda-sonido-vivo
- Backend: https://github.com/velizntd/backend-tienda-sonido-vivoep3

## Tecnologías

- Java 21 LTS.
- Spring Boot 4.0.10.
- Spring Cloud 2025.1.5.
- Maven Wrapper.
- Spring Web MVC.
- Spring Data JPA.
- Spring Security.
- OAuth2 Resource Server.
- JWT HS256.
- BCrypt.
- MySQL 8.
- Flyway.
- Swagger / Springdoc 3.0.0.
- Eureka Server.
- Reactive API Gateway.
- React y Vite.

## Arquitectura

```text
React (5173)
    |
API Gateway (8080) <--> Eureka Server (8761)
    |
    +-- ms-productos (8081)
    +-- ms-novedades (8082)
    +-- ms-contacto  (8083)
    +-- ms-carrito   (8084)
    +-- ms-usuarios  (8085)
    +-- ms-registro  (8086)
    +-- ms-blogs     (8087)
              |
        MySQL (3306)
          sonidovivo
```

El frontend consume las APIs mediante el Gateway.
Eureka permite descubrir las instancias de los microservicios.
Cada microservicio administra sus propias tablas.

## Microservicios y responsables

| Proyecto | Puerto | Ruta | Responsable |
|---|---|---|---|
| eureka-server | 8761 | Panel Eureka | Vicente |
| api-gateway | 8080 | /api | Vicente |
| ms-productos | 8081 | /api/productos, /api/categorias | Vicente |
| ms-novedades | 8082 | /api/novedades | Vicente |
| ms-contacto | 8083 | /api/contactos | Felipe |
| ms-carrito | 8084 | /api/carrito, /api/pedidos | Sebastián |
| ms-usuarios | 8085 | /api/usuarios | Felipe |
| ms-registro | 8086 | /api/auth | Sebastián |
| ms-blogs | 8087 | /api/blogs | Felipe |

## Requisitos

- JDK 21.
- Git.
- MySQL 8 en localhost:3306.
- Node.js y npm para el frontend.
- Visual Studio Code.
- No es necesario instalar Maven globalmente.

## Instalación

Clonar el repositorio:

```bash
git clone https://github.com/velizntd/backend-tienda-sonido-vivoep3.git
cd backend-tienda-sonido-vivoep3
git switch evaluacion-3
git pull origin evaluacion-3
```

## Configuración MySQL

Ingresar a MySQL como administrador y ejecutar:

```sql
CREATE DATABASE IF NOT EXISTS sonidovivo
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;

CREATE USER 'sonidovivo_app'@'localhost'
IDENTIFIED BY 'REEMPLAZAR_PASSWORD';

GRANT ALL PRIVILEGES ON sonidovivo.*
TO 'sonidovivo_app'@'localhost';
```

Reemplazar REEMPLAZAR_PASSWORD por una contraseña local segura.

No crear las tablas de negocio manualmente.
Las migraciones se administran mediante Flyway.

## Variables de entorno

Desde PowerShell ejecutar:

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\configurar-entorno.ps1
```

El script configura:

- DB_PASSWORD: contraseña de MySQL.
- JWT_SECRET: clave JWT local en Base64.

Todos los servicios del mismo entorno deben utilizar la misma clave JWT.
No subir credenciales a GitHub.
## Administrador inicial

Al iniciar, ms-usuarios crea un administrador si no existe ninguno activo. La contraseña se define en la variable ADMIN_PASSWORD (4 a 10 caracteres) y solo se necesita antes del primer arranque:

```powershell
$env:ADMIN_PASSWORD = "REEMPLAZAR_CLAVE"
powershell -ExecutionPolicy Bypass -File .\scripts\iniciar-backend.ps1
```

| Dato | Valor |
|---|---|
| Correo | admin@sonidovivo.example |
| Rol | administrador |
| Contraseña | la definida en ADMIN_PASSWORD |

Si la variable no está definida, ms-usuarios no crea la cuenta y lo avisa en su consola.

ms-usuarios debe iniciar antes que ms-registro y ms-carrito, porque sus migraciones Flyway crean la tabla usuarios y refresh_tokens, carritos y pedidos tienen claves foráneas hacia ella. Si ms-registro o ms-carrito parten primero, sus migraciones fallan.

## Ejecutar el backend

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\iniciar-backend.ps1
```

Orden de inicio:

1. MySQL.
2. Eureka Server.
3. API Gateway.
4. ms-usuarios.
5. ms-registro.
6. ms-productos.
7. ms-carrito.
8. ms-contacto.
9. ms-novedades.
10. ms-blogs.

También se puede iniciar cada servicio individualmente desde su carpeta:

```powershell
.\mvnw.cmd spring-boot:run
```

En cada terminal individual deben estar disponibles DB_PASSWORD
y JWT_SECRET cuando corresponda.

## Verificar el backend

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\verificar-backend.ps1
```

Los nueve proyectos deben estar activos.
Los siete microservicios REST deben responder HTTP 200 en OpenAPI.

Panel Eureka:

http://localhost:8761

## Swagger

| Microservicio | URL |
|---|---|
| Productos | http://localhost:8081/swagger-ui/index.html |
| Novedades | http://localhost:8082/swagger-ui/index.html |
| Contacto | http://localhost:8083/swagger-ui/index.html |
| Carrito | http://localhost:8084/swagger-ui/index.html |
| Usuarios | http://localhost:8085/swagger-ui/index.html |
| Registro | http://localhost:8086/swagger-ui/index.html |
| Blogs | http://localhost:8087/swagger-ui/index.html |

Swagger mostrará las operaciones cuando se implementen los controladores.

## Persistencia y Flyway

Base de datos: sonidovivo.

Cada servicio dispone de un historial de migraciones independiente.

| Servicio | Historial Flyway | Tablas de negocio propuestas |
|---|---|---|
| Productos | flyway_productos | productos, categorias |
| Novedades | flyway_novedades | novedades |
| Contacto | flyway_contacto | contactos |
| Carrito | flyway_carrito | carritos, detalle_carrito, pedidos, detalle_pedido |
| Usuarios | flyway_usuarios | usuarios |
| Registro | flyway_registro | refresh_tokens |
| Blogs | flyway_blogs | blogs |

Reglas:

- Las migraciones se guardan en src/main/resources/db/migration.
- Comenzar con V1__crear_tablas.sql.
- Los cambios posteriores utilizan V2, V3, etc.
- No modificar migraciones que ya se hayan aplicado.
- Hibernate utiliza ddl-auto=validate.
- No utilizar ddl-auto=create ni ddl-auto=update.
- Solo ms-usuarios administra las migraciones de la tabla usuarios.
- ms-registro debe respetar la estructura definida por ms-usuarios.

## Seguridad JWT

| Parámetro | Valor |
|---|---|
| Algoritmo | HS256 |
| Secreto | JWT_SECRET |
| Formato | Base64 |
| Longitud mínima de clave | 32 bytes |
| Emisor iss | sonidovivo |
| Identificador sub | ID del usuario |
| Claim de rol | rol |
| Rol cliente | cliente |
| Rol administrador | administrador |
| Access token | 15 minutos |
| Refresh token propuesto | 7 días |

Las peticiones autenticadas deben incluir:

Authorization: Bearer TOKEN

Reglas de seguridad:

- Las contraseñas se almacenan mediante hash BCrypt.
- Las contraseñas nunca se devuelven en las respuestas.
- El registro público solamente permite crear clientes.
- Las operaciones administrativas requieren rol administrador.
- Los clientes solamente pueden consultar y modificar sus propios recursos.
- Gateway y microservicios validan los tokens.
- Login y emisión de tokens corresponden a ms-registro.
- Refresh y logout están pendientes de implementación segura.
- Si se utilizan cookies para refresh, implementar protección CSRF.

## Convenciones de desarrollo

Estructura de cada microservicio:

```text
src/main/java/cl/tienda/nombreservicio/
    config/
    controller/
    dto/
    exception/
    model/
    repository/
    service/
```

- Mantener los nombres y puertos establecidos.
- Respetar los tipos y campos del frontend existente.
- Usar codigo como identificador String de productos.
- Usar id como identificador String de usuarios y pedidos.
- Utilizar correo como campo de correo electrónico.
- Validar los datos de entrada.
- Utilizar DTO para proteger datos sensibles.
- Documentar las operaciones mediante Swagger.
- CORS se administra desde API Gateway.
- Frontend utiliza http://localhost:8080/api.
- Formato común propuesto para errores de negocio: {"mensaje":"Descripción"}.

## Códigos HTTP

- 200: operación exitosa.
- 201: recurso creado.
- 204: operación exitosa sin contenido.
- 400: datos incorrectos.
- 401: autenticación requerida.
- 403: permisos insuficientes.
- 404: recurso no encontrado.
- 409: conflicto de datos.
- 500: error interno.
- 503: servicio no disponible.

## Compilación y pruebas

Desde la carpeta de cada microservicio:

```powershell
.\mvnw.cmd clean compile
.\mvnw.cmd test
.\mvnw.cmd clean package
```

Las pruebas de integración pueden requerir MySQL y las variables
de entorno. Cada integrante debe implementar pruebas de sus
funcionalidades.

Los servicios con JaCoCo (ms-productos, ms-novedades, ms-usuarios,
ms-contacto y ms-blogs) generan el reporte de cobertura al ejecutar
`.\mvnw.cmd test`, en `target/site/jacoco/index.html`.

## Ramas Git

- main: versión entregada.
- evaluacion-3: integración del equipo.
- chore/configuracion-base: preparación de infraestructura.
- feature/vicente-productos
- feature/sebastian-registro-carrito
- feature/felipe-usuarios-contacto-blogs

Todos los Pull Requests deben dirigirse a evaluacion-3.

No trabajar directamente sobre main.

## Estado de la infraestructura

Validado el 24 de noviembre de 2026:

- 9 de 9 proyectos iniciados correctamente.
- 7 de 7 microservicios compilados.
- 7 de 7 endpoints OpenAPI respondiendo HTTP 200.
- 7 historiales Flyway creados.
- 8 aplicaciones registradas en Eureka.
- Seguridad base JWT configurada.
- Acceso público y autenticación requerida comprobados en Gateway.
- Scripts PowerShell sin errores de sintaxis.
- Script de verificación ejecutado correctamente.

## Estado de funcionalidades

Actualizado el 24 de noviembre de 2026:

| Servicio | Estado |
|---|---|
| ms-usuarios | CRUD de usuarios, roles, estado, administrador inicial, Swagger y pruebas. Integrado en evaluacion-3. |
| ms-contacto | Envío de mensajes y consulta para administrador, Swagger y pruebas. Integrado en evaluacion-3. |
| ms-blogs | CRUD de artículos con secciones, Swagger y pruebas. Integrado en evaluacion-3. |
| ms-productos | CRUD de productos y categorías, Swagger y pruebas. Integrado en evaluacion-3. |
| ms-novedades | Controlador, servicio y migraciones. Integrado en evaluacion-3. |
| ms-registro | Registro, login, refresh con rotación, cookies HttpOnly con CSRF y logout. En feature/sebastian-registro-carrito, pendiente de PR. |
| ms-carrito | Migraciones, entidades y repositorios listos. Endpoints en desarrollo. |

## Funcionalidades pendientes

- Endpoints de carrito, pedidos y checkout en ms-carrito.
- Descuento interno de stock con X-Internal-Service-Key.
- Integración React de carrito, checkout, historial y administración de pedidos.
- Indicadores de AdminInicio conectados a las API.
- Evidencias y documentación final.

## Equipo

- Vicente: infraestructura, productos, categorías y novedades.
- Sebastián: registro, autenticación, carrito y pedidos.
- Felipe: usuarios, contacto y blogs.

Cada integrante desarrolla y prueba sus funcionalidades
en backend y frontend.
