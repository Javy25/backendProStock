package com.example.prostock.service;

import com.example.prostock.DTO.CategoriaRequest;
import com.example.prostock.DTO.CategoriaResponse;
import com.example.prostock.exception.ConflictoException;
import com.example.prostock.exception.RecursoNoEncontradoException;
import com.example.prostock.model.TipoCategoria;
import com.example.prostock.repository.ProductoRepository;
import com.example.prostock.repository.TipoCategoriaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Aplica las validaciones de negocio y administra las categorías del catálogo. */
@Service
public class CategoriaService {

    private static final int LARGO_MAXIMO = 255;

    private final TipoCategoriaRepository categoriaRepository;
    private final ProductoRepository productoRepository;

    public CategoriaService(TipoCategoriaRepository categoriaRepository, ProductoRepository productoRepository) {
        this.categoriaRepository = categoriaRepository;
        this.productoRepository = productoRepository;
    }

    /** Lista categorías ordenadas por nombre y las convierte al DTO público. */
    public List<CategoriaResponse> listar() {
        return categoriaRepository.findAllByOrderByNombreAsc().stream().map(CategoriaResponse::desde).toList();
    }

    /** Busca una categoría y devuelve su representación externa. */
    public CategoriaResponse obtener(Long id) {
        return CategoriaResponse.desde(buscar(id));
    }

    /** Valida nombre y descripción, evita duplicados y persiste la categoría. */
    @Transactional
    public CategoriaResponse crear(CategoriaRequest request) {
        String nombre = validarNombre(request);
        String descripcion = validarDescripcion(request);
        verificarNombreLibre(nombre, null);
        return CategoriaResponse.desde(categoriaRepository.save(new TipoCategoria(nombre, descripcion)));
    }

    /** Actualiza una categoría existente manteniendo única la comparación del nombre. */
    @Transactional
    public CategoriaResponse actualizar(Long id, CategoriaRequest request) {
        TipoCategoria categoria = buscar(id);
        String nombre = validarNombre(request);
        String descripcion = validarDescripcion(request);
        verificarNombreLibre(nombre, id);
        categoria.setNombre(nombre);
        categoria.setDescripcion(descripcion);
        return CategoriaResponse.desde(categoriaRepository.save(categoria));
    }

    /** Impide borrar categorías que todavía están referenciadas por productos. */
    @Transactional
    public void eliminar(Long id) {
        TipoCategoria categoria = buscar(id);
        if (productoRepository.existsByTipoCategoria_Id(id)) {
            throw new ConflictoException("No se puede eliminar la categoría porque tiene productos asociados");
        }
        categoriaRepository.delete(categoria);
    }

    private TipoCategoria buscar(Long id) {
        return categoriaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Categoría no encontrada."));
    }

    private String validarNombre(CategoriaRequest request) {
        if (request == null || request.getNombre() == null || request.getNombre().isBlank()) {
            throw new IllegalArgumentException("El nombre de la categoría es obligatorio.");
        }
        String nombre = request.getNombre().trim();
        if (nombre.length() > LARGO_MAXIMO) {
            throw new IllegalArgumentException("El nombre no puede superar los " + LARGO_MAXIMO + " caracteres.");
        }
        return nombre;
    }

    private String validarDescripcion(CategoriaRequest request) {
        String descripcion = request.getDescripcion() == null ? null : request.getDescripcion().trim();
        if (descripcion != null && descripcion.length() > LARGO_MAXIMO) {
            throw new IllegalArgumentException("La descripción no puede superar los " + LARGO_MAXIMO + " caracteres.");
        }
        return descripcion == null || descripcion.isEmpty() ? null : descripcion;
    }

    private void verificarNombreLibre(String nombre, Long idActual) {
        categoriaRepository.findByNombreIgnoreCase(nombre).ifPresent(existente -> {
            if (!existente.getId().equals(idActual)) {
                throw new ConflictoException("Ya existe una categoría con ese nombre");
            }
        });
    }
}