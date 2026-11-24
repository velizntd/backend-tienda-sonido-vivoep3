package cl.tienda.mscontacto.service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cl.tienda.mscontacto.dto.ContactoRequest;
import cl.tienda.mscontacto.dto.ContactoResponse;
import cl.tienda.mscontacto.exception.RecursoNoEncontradoException;
import cl.tienda.mscontacto.model.Contacto;
import cl.tienda.mscontacto.repository.ContactoRepository;

@Service
public class ContactoService {

    private final ContactoRepository contactoRepository;

    public ContactoService(ContactoRepository contactoRepository) {
        this.contactoRepository = contactoRepository;
    }

    @Transactional
    public ContactoResponse registrar(ContactoRequest datos, String usuarioId) {
        Contacto contacto = new Contacto();
        contacto.setUsuarioId(usuarioId);
        contacto.setNombre(datos.nombre().trim());
        contacto.setCorreo(datos.correo() == null || datos.correo().isBlank()
                ? null
                : datos.correo().trim().toLowerCase(Locale.ROOT));
        contacto.setComentario(datos.comentario().trim());
        contacto.setFechaEnvio(LocalDateTime.now(ZoneOffset.UTC));
        return ContactoResponse.desde(contactoRepository.save(contacto));
    }

    @Transactional(readOnly = true)
    public List<ContactoResponse> listar() {
        return contactoRepository.findAllByOrderByFechaEnvioDesc().stream()
                .map(ContactoResponse::desde)
                .toList();
    }

    @Transactional(readOnly = true)
    public ContactoResponse buscarPorId(Long id) {
        return contactoRepository.findById(id)
                .map(ContactoResponse::desde)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró el mensaje solicitado."));
    }
}
