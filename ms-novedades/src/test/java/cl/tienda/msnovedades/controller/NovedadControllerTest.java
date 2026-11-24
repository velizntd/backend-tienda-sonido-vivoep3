package cl.tienda.msnovedades.controller;

import cl.tienda.msnovedades.dto.NovedadDTO;
import cl.tienda.msnovedades.service.NovedadService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NovedadControllerTest {

    @Mock
    private NovedadService novedadService;

    @InjectMocks
    private NovedadController novedadController;

    private NovedadDTO dto;

    @BeforeEach
    void prepararDatos() {

        dto = new NovedadDTO(
                1L,
                "Novedad de prueba",
                "Descripcion de prueba",
                "DESTACADO",
                "/img/prueba.jpg",
                "GA001",
                LocalDate.now(),
                LocalDate.now().plusDays(30),
                true
        );
    }

    @Test
    void listarNovedades() {

        when(novedadService.listar())
                .thenReturn(List.of(dto));

        assertEquals(1, novedadController.listar().size());
    }

    @Test
    void listarNovedadesActivas() {

        when(novedadService.listarActivas())
                .thenReturn(List.of(dto));

        assertEquals(1, novedadController.listarActivas().size());
    }

    @Test
    void buscarNovedadPorId() {

        when(novedadService.buscarPorId(1L))
                .thenReturn(dto);

        assertEquals(1L, novedadController.buscarPorId(1L).getId());
    }

    @Test
    void crearNovedad() {

        when(novedadService.crear(dto))
                .thenReturn(dto);

        ResponseEntity<NovedadDTO> respuesta =
                novedadController.crear(dto);

        assertEquals(201, respuesta.getStatusCode().value());
        assertNotNull(respuesta.getBody());
    }

    @Test
    void actualizarNovedad() {

        dto.setTitulo("Novedad actualizada");

        when(novedadService.actualizar(1L, dto))
                .thenReturn(dto);

        assertEquals(
                "Novedad actualizada",
                novedadController.actualizar(1L, dto).getTitulo()
        );
    }

    @Test
    void desactivarNovedad() {

        dto.setActivo(false);

        when(novedadService.cambiarEstado(1L, false))
                .thenReturn(dto);

        assertFalse(
                novedadController.cambiarEstado(1L, false).getActivo()
        );
    }

    @Test
    void eliminarNovedad() {

        ResponseEntity<Void> respuesta =
                novedadController.eliminar(1L);

        assertEquals(204, respuesta.getStatusCode().value());

        verify(novedadService).eliminar(1L);
    }
}