package cl.tienda.msnovedades.service;

import cl.tienda.msnovedades.dto.NovedadDTO;
import cl.tienda.msnovedades.entity.Novedad;
import cl.tienda.msnovedades.repository.NovedadRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NovedadServiceTest {

    @Mock
    private NovedadRepository novedadRepository;

    @InjectMocks
    private NovedadService novedadService;

    private Novedad novedad;
    private NovedadDTO dto;

    @BeforeEach
    void prepararDatos() {

        novedad = new Novedad(
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

        when(novedadRepository.findAll())
                .thenReturn(List.of(novedad));

        List<NovedadDTO> resultado = novedadService.listar();

        assertEquals(1, resultado.size());
        assertEquals("Novedad de prueba", resultado.get(0).getTitulo());
    }

    @Test
    void buscarNovedadPorId() {

        when(novedadRepository.findById(1L))
                .thenReturn(Optional.of(novedad));

        assertEquals(1L, novedadService.buscarPorId(1L).getId());
    }

    @Test
    void buscarNovedadInexistente() {

        when(novedadRepository.findById(99L))
                .thenReturn(Optional.empty());

        ResponseStatusException error = assertThrows(
                ResponseStatusException.class,
                () -> novedadService.buscarPorId(99L)
        );

        assertEquals(404, error.getStatusCode().value());
    }

    @Test
    void crearNovedad() {

        when(novedadRepository.save(any(Novedad.class)))
                .thenReturn(novedad);

        NovedadDTO resultado = novedadService.crear(dto);

        assertEquals("Novedad de prueba", resultado.getTitulo());

        verify(novedadRepository).save(any(Novedad.class));
    }

    @Test
    void rechazarFechasInvalidas() {

        dto.setFechaFin(LocalDate.now().minusDays(1));

        ResponseStatusException error = assertThrows(
                ResponseStatusException.class,
                () -> novedadService.crear(dto)
        );

        assertEquals(400, error.getStatusCode().value());

        verify(novedadRepository, never()).save(any(Novedad.class));
    }

    @Test
    void actualizarNovedad() {

        dto.setTitulo("Novedad actualizada");

        when(novedadRepository.findById(1L))
                .thenReturn(Optional.of(novedad));

        when(novedadRepository.save(any(Novedad.class)))
                .thenAnswer(invocacion -> invocacion.getArgument(0));

        NovedadDTO resultado = novedadService.actualizar(1L, dto);

        assertEquals("Novedad actualizada", resultado.getTitulo());
    }

    @Test
    void desactivarNovedad() {

        when(novedadRepository.findById(1L))
                .thenReturn(Optional.of(novedad));

        when(novedadRepository.save(any(Novedad.class)))
                .thenAnswer(invocacion -> invocacion.getArgument(0));

        NovedadDTO resultado = novedadService.cambiarEstado(1L, false);

        assertFalse(resultado.getActivo());
    }

    @Test
    void eliminarNovedad() {

        when(novedadRepository.findById(1L))
                .thenReturn(Optional.of(novedad));

        novedadService.eliminar(1L);

        verify(novedadRepository).delete(novedad);
    }

    @Test
    void listarNovedadesActivas() {

        when(novedadRepository.findByActivoTrue())
                .thenReturn(List.of(novedad));

        assertEquals(1, novedadService.listarActivas().size());
    }

    @Test
    void excluirNovedadVencida() {

        novedad.setFechaInicio(LocalDate.now().minusDays(30));
        novedad.setFechaFin(LocalDate.now().minusDays(1));

        when(novedadRepository.findByActivoTrue())
                .thenReturn(List.of(novedad));

        assertTrue(novedadService.listarActivas().isEmpty());
    }

    @Test
    void excluirNovedadFutura() {

        novedad.setFechaInicio(LocalDate.now().plusDays(5));

        when(novedadRepository.findByActivoTrue())
                .thenReturn(List.of(novedad));

        assertTrue(novedadService.listarActivas().isEmpty());
    }

    @Test
    void incluirNovedadSinFechaFin() {

        novedad.setFechaFin(null);

        when(novedadRepository.findByActivoTrue())
                .thenReturn(List.of(novedad));

        assertEquals(1, novedadService.listarActivas().size());
    }
}