package com.example.prostock.service;

import com.example.prostock.model.Producto;
import com.example.prostock.model.TipoCategoria;
import com.example.prostock.repository.ProductoRepository;
import com.example.prostock.repository.TipoCategoriaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/** Aplica las reglas del catálogo antes de consultar y persistir productos. */
@Service
public class ProductoService {

    private final ProductoRepository productoRepository;
    private final TipoCategoriaRepository categoriaRepository;

    @Autowired
    public ProductoService(ProductoRepository productoRepository, TipoCategoriaRepository categoriaRepository) {
        this.productoRepository = productoRepository;
        this.categoriaRepository = categoriaRepository;
    }

    // Obtener todos los productos
    /** Lista todos los productos registrados. */
    public List<Producto> obtenerTodos() {
        return productoRepository.findAll();
    }

    // Obtener productos filtrados por el nombre de la categorÃ­a
    /** Filtra los productos por nombre de categoría sin distinguir mayúsculas. */
    public List<Producto> obtenerPorCategoria(String categoria) {
        return productoRepository.findByTipoCategoria_NombreIgnoreCase(categoria);
    }

    // Obtener productos con descuento
    /** Devuelve los productos cuyo descuento es mayor que cero. */
    public List<Producto> obtenerOfertas() {
        return productoRepository.findByDescuentoGreaterThan(0);
    }

    // Obtener un producto por ID
    /** Busca un producto por identificador. */
    public Optional<Producto> obtenerPorId(Long id) {
        return productoRepository.findById(id);
    }

    // Guardar un nuevo producto
    /** Normaliza el descuento y asocia una categoría ya persistida antes de guardar. */
    public Producto guardarProducto(Producto producto) {
        normalizarDescuento(producto);
        resolverCategoria(producto);
        return productoRepository.save(producto);
    }

    // Actualizar un producto existente
    /** Actualiza los campos editables después de aplicar las reglas del catálogo. */
    public Optional<Producto> actualizarProducto(Long id, Producto productoDetalles) {
        normalizarDescuento(productoDetalles);
        resolverCategoria(productoDetalles);
        return productoRepository.findById(id).map(producto -> {
            producto.setNombreModelo(productoDetalles.getNombreModelo());
            producto.setPrecio(productoDetalles.getPrecio());
            producto.setDescripcion(productoDetalles.getDescripcion());
            producto.setLinkImagen(productoDetalles.getLinkImagen());
            producto.setTipoCategoria(productoDetalles.getTipoCategoria());
            producto.setStock(productoDetalles.getStock());
            producto.setDescuento(productoDetalles.getDescuento());
            return productoRepository.save(producto);
        });
    }

    // Asocia una categoría existente (por id o nombre); nunca crea categorías implícitamente.
    // Resuelve la categoría por ID o nombre; no crea una categoría implícitamente.
    private void resolverCategoria(Producto producto) {
        TipoCategoria indicada = producto.getTipoCategoria();
        if (indicada == null) {
            return;
        }
        TipoCategoria existente = null;
        if (indicada.getId() != null) {
            existente = categoriaRepository.findById(indicada.getId()).orElse(null);
        } else if (indicada.getNombre() != null && !indicada.getNombre().isBlank()) {
            existente = categoriaRepository.findByNombreIgnoreCase(indicada.getNombre().trim()).orElse(null);
        }
        if (existente == null) {
            throw new IllegalArgumentException("La categoría indicada no existe. Créala antes de asociarla al producto.");
        }
        producto.setTipoCategoria(existente);
    }

    // El descuento ausente equivale a cero y los valores válidos van de 0 a 100.
    private void normalizarDescuento(Producto producto) {
        if (producto.getDescuento() == null) {
            producto.setDescuento(0);
        }
        if (producto.getDescuento() < 0 || producto.getDescuento() > 100) {
            throw new IllegalArgumentException("El descuento debe estar entre 0 y 100.");
        }
    }

    // Eliminar un producto
    /** Elimina un producto y devuelve si existía antes de la operación. */
    public boolean eliminarProducto(Long id) {
        if (productoRepository.existsById(id)) {
            productoRepository.deleteById(id);
            return true;
        }
        return false;
    }
}
