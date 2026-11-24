package com.example.prostock;

import com.example.prostock.controller.ProductoController;
import com.example.prostock.model.Boleta;
import com.example.prostock.model.Carrito;
import com.example.prostock.model.Cliente;
import com.example.prostock.model.ItemCarrito;
import com.example.prostock.model.Producto;
import com.example.prostock.repository.BoletaRepository;
import com.example.prostock.repository.CarritoRepository;
import com.example.prostock.repository.ClienteRepository;
import com.example.prostock.repository.ProductoRepository;
import com.example.prostock.repository.TipoCategoriaRepository;
import com.example.prostock.service.BoletaService;
import com.example.prostock.service.CarritoService;
import com.example.prostock.service.ProductoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Comprueba cálculo de descuentos, búsqueda de ofertas y precios usados por carrito y boleta. */
class OfertasTest {

    private ProductoRepository productoRepository;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        productoRepository = mock(ProductoRepository.class);
        ProductoController controller = new ProductoController(new ProductoService(productoRepository, mock(TipoCategoriaRepository.class)));
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    private Producto producto(Long id, int precio, int descuento, int stock) {
        Producto p = new Producto(precio, "Producto " + id, "Descripcion", "http://img/" + id, null, stock);
        p.setId(id);
        p.setDescuento(descuento);
        return p;
    }

    @Test
    void precioOfertaSeCalculaConElDescuento() {
        assertEquals(2700, producto(1L, 3000, 10, 5).getPrecioOferta());
        assertEquals(2117, producto(2L, 2490, 15, 5).getPrecioOferta());
        assertEquals(3000, producto(3L, 3000, 0, 5).getPrecioOferta());
        assertEquals(0, producto(4L, 3000, 100, 5).getPrecioOferta());
    }

    @Test
    void productoNuevoTieneDescuentoCero() {
        assertEquals(0, new Producto().getDescuento());
    }

    @Test
    void crearProductoConDescuentoFueraDeRangoDevuelve400() throws Exception {
        mockMvc.perform(post("/api/productos").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombreModelo\":\"X\",\"precio\":1000,\"stock\":5,\"descuento\":150}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("El descuento debe estar entre 0 y 100."));
        verify(productoRepository, never()).save(any());
    }

    @Test
    void actualizarProductoConDescuentoNegativoDevuelve400() throws Exception {
        mockMvc.perform(put("/api/productos/1").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombreModelo\":\"X\",\"precio\":1000,\"stock\":5,\"descuento\":-5}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("El descuento debe estar entre 0 y 100."));
    }

    @Test
    void crearProductoSinDescuentoQuedaEnCero() throws Exception {
        when(productoRepository.save(any(Producto.class))).thenAnswer(inv -> inv.getArgument(0));
        mockMvc.perform(post("/api/productos").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombreModelo\":\"X\",\"precio\":1000,\"stock\":5}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.descuento").value(0))
                .andExpect(jsonPath("$.precioOferta").value(1000));
    }

    @Test
    void ofertasDevuelveSoloProductosConDescuento() throws Exception {
        when(productoRepository.findByDescuentoGreaterThan(0))
                .thenReturn(List.of(producto(1L, 3290, 10, 55), producto(2L, 4690, 15, 45)));

        mockMvc.perform(get("/api/productos/ofertas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].descuento").value(10))
                .andExpect(jsonPath("$[0].precio").value(3290))
                .andExpect(jsonPath("$[0].precioOferta").value(2961))
                .andExpect(jsonPath("$[1].precioOferta").value(3987));
    }

    @Test
    void ofertasSinResultadosDevuelveListaVacia() throws Exception {
        when(productoRepository.findByDescuentoGreaterThan(0)).thenReturn(List.of());
        mockMvc.perform(get("/api/productos/ofertas"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    @Test
    void rutaOfertasNoChocaConLaBusquedaPorId() throws Exception {
        when(productoRepository.findById(7L)).thenReturn(Optional.of(producto(7L, 1000, 20, 3)));
        mockMvc.perform(get("/api/productos/7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(7))
                .andExpect(jsonPath("$.precioOferta").value(800));
    }

    @Test
    void carritoUsaElPrecioConDescuento() {
        CarritoRepository carritoRepository = mock(CarritoRepository.class);
        ClienteRepository clienteRepository = mock(ClienteRepository.class);
        Cliente cliente = new Cliente();
        cliente.setId(1L);
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(productoRepository.findById(5L)).thenReturn(Optional.of(producto(5L, 1000, 20, 10)));
        when(carritoRepository.findByClienteId(1L)).thenReturn(Optional.empty());
        when(carritoRepository.save(any(Carrito.class))).thenAnswer(inv -> inv.getArgument(0));

        Carrito carrito = new CarritoService(carritoRepository, clienteRepository, productoRepository)
                .agregarProducto(1L, 5L, 2);

        assertEquals(800.0, carrito.getItems().get(0).getPrecioUnitario());
        assertEquals(1600.0, carrito.getSubtotal());
        assertEquals(1600.0, carrito.getTotal());
    }

    @Test
    void boletaUsaElPrecioConDescuento() {
        BoletaRepository boletaRepository = mock(BoletaRepository.class);
        CarritoRepository carritoRepository = mock(CarritoRepository.class);
        ClienteRepository clienteRepository = mock(ClienteRepository.class);
        Cliente cliente = new Cliente();
        cliente.setId(1L);
        Producto producto = producto(5L, 1000, 20, 10);

        Carrito carrito = new Carrito();
        carrito.setImpuestos(0.0);
        // Carrito antiguo guardado con el precio sin descuento
        carrito.setItems(new ArrayList<>(List.of(new ItemCarrito(1L, 5L, "Producto 5", 2, 1000.0))));

        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(carritoRepository.findByClienteId(1L)).thenReturn(Optional.of(carrito));
        when(productoRepository.findById(5L)).thenReturn(Optional.of(producto));
        when(boletaRepository.save(any(Boleta.class))).thenAnswer(inv -> inv.getArgument(0));

        Boleta boleta = new BoletaService(boletaRepository, carritoRepository, clienteRepository,
                productoRepository, mock(JavaMailSender.class)).crearDesdeCarrito(1L, "TARJETA", "Calle 1");

        assertEquals(800.0, boleta.getItems().get(0).getPrecioUnitario());
        assertEquals(1600.0, boleta.getSubtotal());
        assertEquals(1600.0, boleta.getTotal());
        assertEquals(8, producto.getStock());
    }
}
