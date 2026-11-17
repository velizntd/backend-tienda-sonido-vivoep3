# eureka-server

Servidor de registro de microservicios (Spring Cloud Netflix Eureka Server).

- Puerto: 8761
- Responsable: Vicente
- Panel: http://localhost:8761
- Contenido: cada microservicio se registra aquí al arrancar con su `spring.application.name`. El `api-gateway` consulta este registro para saber dónde está cada microservicio.
- Se inicia antes que los microservicios.
