package cl.tienda.msproductos.service;

import cl.tienda.msproductos.dto.ProductoDTO;
import cl.tienda.msproductos.entity.Producto;
import cl.tienda.msproductos.entity.Categoria;
import cl.tienda.msproductos.repository.ProductoRepository;
import cl.tienda.msproductos.repository.CategoriaRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductoService {

    private final ProductoRepository productoRepository;
    private final CategoriaRepository categoriaRepository;

    @Transactional(readOnly = true)
    public List<ProductoDTO> listar() {
        return productoRepository.findAll()
                .stream()
                .map(this::convertirDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public ProductoDTO buscarPorCodigo(String codigo) {
        return convertirDTO(buscarEntidad(codigo));
    }

    @Transactional(readOnly = true)
    public List<ProductoDTO> buscarPorCategoria(String categoria) {
        return productoRepository
                .findByCategoriaNombreIgnoreCase(categoria)
                .stream()
                .map(this::convertirDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ProductoDTO> buscarPorNombre(String nombre) {
        return productoRepository
                .findByNombreContainingIgnoreCase(nombre)
                .stream()
                .map(this::convertirDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ProductoDTO> buscarPorMarca(String marca) {
        return productoRepository
                .findByMarcaIgnoreCase(marca)
                .stream()
                .map(this::convertirDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ProductoDTO> listarDisponibles() {
        return productoRepository
                .findByStockGreaterThan(0)
                .stream()
                .map(this::convertirDTO)
                .toList();
    }

    public ProductoDTO crear(ProductoDTO dto) {

        if (!dto.getCodigo().equals(dto.getCodigo().trim())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El código no puede contener espacios al inicio o al final"
            );
        }

        if (productoRepository.existsById(dto.getCodigo())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Ya existe un producto con ese código"
            );
        }

        Producto producto = new Producto();
        producto.setCodigo(dto.getCodigo());

        asignarDatos(producto, dto);

        return convertirDTO(productoRepository.save(producto));
    }

   public ProductoDTO actualizar(String codigo, ProductoDTO dto) {

    Producto producto = buscarEntidadBloqueada(codigo);

        if (!codigo.equals(dto.getCodigo())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "No se puede modificar el código del producto"
            );
        }

        asignarDatos(producto, dto);

        return convertirDTO(productoRepository.save(producto));
    }

    public void eliminar(String codigo) {
    Producto producto = buscarEntidadBloqueada(codigo);
    productoRepository.delete(producto);
    }

    private Producto buscarEntidad(String codigo) {
        return productoRepository.findById(codigo)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Producto no encontrado"
                ));
    }
    private Producto buscarEntidadBloqueada(String codigo) {
    return productoRepository.buscarParaActualizar(codigo)
            .orElseThrow(() -> new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Producto no encontrado"
            ));
}

    private void asignarDatos(Producto producto, ProductoDTO dto) {

        Categoria categoria = categoriaRepository
                .findByNombreIgnoreCase(dto.getCategoria())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "La categoría indicada no existe"
                ));

        producto.setCategoria(categoria);
        producto.setNombre(dto.getNombre());
        producto.setMarca(dto.getMarca());
        producto.setModelo(dto.getModelo());
        producto.setStock(dto.getStock());
        producto.setPrecio(dto.getPrecio());
        producto.setDescripcion(dto.getDescripcion());
        producto.setImagen(dto.getImagen());
    }

    private ProductoDTO convertirDTO(Producto producto) {
        return new ProductoDTO(
                producto.getCodigo(),
                producto.getCategoria().getNombre(),
                producto.getNombre(),
                producto.getMarca(),
                producto.getModelo(),
                producto.getStock(),
                producto.getPrecio(),
                producto.getDescripcion(),
                producto.getImagen()
        );
    }
}