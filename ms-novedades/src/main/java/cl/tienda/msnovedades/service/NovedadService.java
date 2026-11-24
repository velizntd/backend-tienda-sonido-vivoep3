package cl.tienda.msnovedades.service;

import cl.tienda.msnovedades.dto.NovedadDTO;
import cl.tienda.msnovedades.entity.Novedad;
import cl.tienda.msnovedades.repository.NovedadRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class NovedadService {

    private final NovedadRepository novedadRepository;

    @Transactional(readOnly = true)
    public List<NovedadDTO> listar() {

        return novedadRepository.findAll()
                .stream()
                .map(this::convertirDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<NovedadDTO> listarActivas() {

        LocalDate hoy = LocalDate.now();

        return novedadRepository.findByActivoTrue()
                .stream()
                .filter(n -> !n.getFechaInicio().isAfter(hoy))
                .filter(n -> n.getFechaFin() == null
                        || !n.getFechaFin().isBefore(hoy))
                .map(this::convertirDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public NovedadDTO buscarPorId(Long id) {
        return convertirDTO(buscarEntidad(id));
    }

    public NovedadDTO crear(NovedadDTO dto) {

        validarFechas(dto);

        Novedad novedad = new Novedad();

        asignarDatos(novedad, dto);

        return convertirDTO(novedadRepository.save(novedad));
    }

    public NovedadDTO actualizar(Long id, NovedadDTO dto) {

        Novedad novedad = buscarEntidad(id);

        validarFechas(dto);
        asignarDatos(novedad, dto);

        return convertirDTO(novedadRepository.save(novedad));
    }

    public NovedadDTO cambiarEstado(Long id, Boolean activo) {

        Novedad novedad = buscarEntidad(id);

        novedad.setActivo(activo);

        return convertirDTO(novedadRepository.save(novedad));
    }

    public void eliminar(Long id) {

        Novedad novedad = buscarEntidad(id);

        novedadRepository.delete(novedad);
    }

    private Novedad buscarEntidad(Long id) {

        return novedadRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Novedad no encontrada"
                ));
    }

    private void validarFechas(NovedadDTO dto) {

        if (dto.getFechaFin() != null
                && dto.getFechaFin().isBefore(dto.getFechaInicio())) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "La fecha de fin no puede ser anterior a la fecha de inicio"
            );
        }
    }

    private void asignarDatos(Novedad novedad, NovedadDTO dto) {

        novedad.setTitulo(dto.getTitulo());
        novedad.setDescripcion(dto.getDescripcion());
        novedad.setTipo(dto.getTipo());
        novedad.setImagen(dto.getImagen());
        novedad.setCodigoProducto(dto.getCodigoProducto());
        novedad.setFechaInicio(dto.getFechaInicio());
        novedad.setFechaFin(dto.getFechaFin());
        novedad.setActivo(dto.getActivo());
    }

    private NovedadDTO convertirDTO(Novedad novedad) {

        return new NovedadDTO(
                novedad.getId(),
                novedad.getTitulo(),
                novedad.getDescripcion(),
                novedad.getTipo(),
                novedad.getImagen(),
                novedad.getCodigoProducto(),
                novedad.getFechaInicio(),
                novedad.getFechaFin(),
                novedad.getActivo()
        );
    }
}