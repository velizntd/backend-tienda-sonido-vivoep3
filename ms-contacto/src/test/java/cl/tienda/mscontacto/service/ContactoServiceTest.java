package cl.tienda.mscontacto.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import cl.tienda.mscontacto.dto.ContactoRequest;
import cl.tienda.mscontacto.dto.ContactoResponse;
import cl.tienda.mscontacto.exception.RecursoNoEncontradoException;
import cl.tienda.mscontacto.model.Contacto;
import cl.tienda.mscontacto.repository.ContactoRepository;

@ExtendWith(MockitoExtension.class)
class ContactoServiceTest {

    @Mock
    private ContactoRepository contactoRepository;

    private ContactoService contactoService;

    @BeforeEach
    void preparar() {
        contactoService = new ContactoService(contactoRepository);
    }

    private Contacto contacto(Long id, String correo, LocalDateTime fecha) {
        Contacto contacto = new Contacto();
        contacto.setId(id);
        contacto.setUsuarioId("cliente-1");
        contacto.setNombre("Camila Rojas");
        contacto.setCorreo(correo);
        contacto.setComentario("¿Tienen stock?");
        contacto.setFechaEnvio(fecha);
        return contacto;
    }

    @Test
    void registrarLimpiaLosDatosYGuardaElUsuario() {
        when(contactoRepository.save(any(Contacto.class))).thenAnswer(invocacion -> {
            Contacto guardado = invocacion.getArgument(0);
            guardado.setId(10L);
            return guardado;
        });

        ContactoResponse respuesta = contactoService.registrar(
                new ContactoRequest("  Camila Rojas  ", "  Camila@Correo.CL ", "  ¿Tienen stock?  "), "cliente-1");

        ArgumentCaptor<Contacto> captor = ArgumentCaptor.forClass(Contacto.class);
        verify(contactoRepository).save(captor.capture());
        Contacto guardado = captor.getValue();

        assertThat(guardado.getUsuarioId()).isEqualTo("cliente-1");
        assertThat(guardado.getNombre()).isEqualTo("Camila Rojas");
        assertThat(guardado.getCorreo()).isEqualTo("camila@correo.cl");
        assertThat(guardado.getComentario()).isEqualTo("¿Tienen stock?");
        assertThat(guardado.getFechaEnvio()).isNotNull();
        assertThat(respuesta.id()).isEqualTo(10L);
        assertThat(respuesta.correo()).isEqualTo("camila@correo.cl");
        assertThat(respuesta.fechaEnvio()).endsWith("Z");
    }

    @Test
    void registrarSinCorreoLoGuardaComoNuloYLoDevuelveVacio() {
        when(contactoRepository.save(any(Contacto.class))).thenAnswer(invocacion -> invocacion.getArgument(0));

        ContactoResponse respuesta = contactoService.registrar(
                new ContactoRequest("Camila", "   ", "Hola"), null);

        ArgumentCaptor<Contacto> captor = ArgumentCaptor.forClass(Contacto.class);
        verify(contactoRepository).save(captor.capture());

        assertThat(captor.getValue().getCorreo()).isNull();
        assertThat(captor.getValue().getUsuarioId()).isNull();
        assertThat(respuesta.correo()).isEmpty();
    }

    @Test
    void registrarConCorreoNuloLoGuardaComoNulo() {
        when(contactoRepository.save(any(Contacto.class))).thenAnswer(invocacion -> invocacion.getArgument(0));

        contactoService.registrar(new ContactoRequest("Camila", null, "Hola"), "cliente-1");

        ArgumentCaptor<Contacto> captor = ArgumentCaptor.forClass(Contacto.class);
        verify(contactoRepository).save(captor.capture());
        assertThat(captor.getValue().getCorreo()).isNull();
    }

    @Test
    void listarDevuelveLosMensajesEnElOrdenDelRepositorio() {
        when(contactoRepository.findAllByOrderByFechaEnvioDesc()).thenReturn(List.of(
                contacto(2L, "b@correo.cl", LocalDateTime.of(2026, 11, 24, 10, 0)),
                contacto(1L, "a@correo.cl", LocalDateTime.of(2026, 11, 23, 10, 0))));

        List<ContactoResponse> mensajes = contactoService.listar();

        assertThat(mensajes).extracting(ContactoResponse::id).containsExactly(2L, 1L);
        assertThat(mensajes.get(0).fechaEnvio()).isEqualTo("2026-11-24T10:00:00Z");
    }

    @Test
    void buscarPorIdDevuelveElMensaje() {
        when(contactoRepository.findById(5L))
                .thenReturn(Optional.of(contacto(5L, null, LocalDateTime.of(2026, 11, 24, 8, 30))));

        ContactoResponse respuesta = contactoService.buscarPorId(5L);

        assertThat(respuesta.id()).isEqualTo(5L);
        assertThat(respuesta.correo()).isEmpty();
        assertThat(respuesta.nombre()).isEqualTo("Camila Rojas");
    }

    @Test
    void buscarPorIdInexistenteLanzaRecursoNoEncontrado() {
        when(contactoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> contactoService.buscarPorId(99L))
                .isInstanceOf(RecursoNoEncontradoException.class)
                .hasMessage("No se encontró el mensaje solicitado.");
    }
}
