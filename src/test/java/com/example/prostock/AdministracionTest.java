package com.example.prostock;

import com.example.prostock.repository.TipoCategoriaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Comprueba los flujos administrativos de categorías, catálogo, perfiles y consulta de boletas. */
@SpringBootTest
@Transactional
class AdministracionTest {

    @Autowired
    private WebApplicationContext contexto;

    @Autowired
    private TipoCategoriaRepository categoriaRepository;

    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.webAppContextSetup(contexto).build();
    }

    private String json(String cuerpo) {
        return cuerpo;
    }

    private long crearCategoria(String nombre) throws Exception {
        MvcResult r = mvc.perform(post("/api/categorias").contentType(MediaType.APPLICATION_JSON)
                        .content(json("{\"nombre\":\"" + nombre + "\",\"descripcion\":\"Desc\"}")))
                .andExpect(status().isCreated())
                .andReturn();
        String cuerpo = r.getResponse().getContentAsString();
        return Long.parseLong(cuerpo.replaceAll(".*\"id\":(\\d+).*", "$1"));
    }

    // ---------- Categorías ----------

    @Test
    void listaLasCategoriasExistentes() throws Exception {
        mvc.perform(get("/api/categorias"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(4))
                .andExpect(jsonPath("$[0].id").exists())
                .andExpect(jsonPath("$[0].nombre").value("Aseo y Limpieza"))
                .andExpect(jsonPath("$[1].nombre").value("Escolar"))
                .andExpect(jsonPath("$[0].descripcion").exists());
    }

    @Test
    void creaObtieneActualizaYEliminaUnaCategoria() throws Exception {
        long id = crearCategoria("Tecnologia");

        mvc.perform(get("/api/categorias/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Tecnologia"))
                .andExpect(jsonPath("$.descripcion").value("Desc"));

        mvc.perform(put("/api/categorias/" + id).contentType(MediaType.APPLICATION_JSON)
                        .content(json("{\"nombre\":\"Tecnologia y Oficina\",\"descripcion\":\"Nueva\"}")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Tecnologia y Oficina"))
                .andExpect(jsonPath("$.descripcion").value("Nueva"));

        mvc.perform(delete("/api/categorias/" + id)).andExpect(status().isNoContent());
        mvc.perform(get("/api/categorias/" + id)).andExpect(status().isNotFound());
    }

    @Test
    void categoriaConNombreVacioDevuelve400() throws Exception {
        mvc.perform(post("/api/categorias").contentType(MediaType.APPLICATION_JSON)
                        .content(json("{\"nombre\":\"   \"}")))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("El nombre de la categoría es obligatorio."));
    }

    @Test
    void categoriaDuplicadaDevuelve409SinImportarMayusculas() throws Exception {
        mvc.perform(post("/api/categorias").contentType(MediaType.APPLICATION_JSON)
                        .content(json("{\"nombre\":\"ESCOLAR\"}")))
                .andExpect(status().isConflict())
                .andExpect(content().string("Ya existe una categoría con ese nombre"));

        long id = crearCategoria("Libreria");
        mvc.perform(put("/api/categorias/" + id).contentType(MediaType.APPLICATION_JSON)
                        .content(json("{\"nombre\":\"escolar\"}")))
                .andExpect(status().isConflict())
                .andExpect(content().string("Ya existe una categoría con ese nombre"));
        // Conservar el mismo nombre al editar no es un conflicto
        mvc.perform(put("/api/categorias/" + id).contentType(MediaType.APPLICATION_JSON)
                        .content(json("{\"nombre\":\"Libreria\",\"descripcion\":\"Otra\"}")))
                .andExpect(status().isOk());
    }

    @Test
    void categoriaInexistenteDevuelve404() throws Exception {
        mvc.perform(get("/api/categorias/99999")).andExpect(status().isNotFound());
        mvc.perform(put("/api/categorias/99999").contentType(MediaType.APPLICATION_JSON)
                        .content(json("{\"nombre\":\"X\"}")))
                .andExpect(status().isNotFound());
        mvc.perform(delete("/api/categorias/99999")).andExpect(status().isNotFound());
    }

    @Test
    void noPermiteEliminarCategoriaConProductos() throws Exception {
        long escolar = categoriaRepository.findByNombreIgnoreCase("Escolar").orElseThrow().getId();
        mvc.perform(delete("/api/categorias/" + escolar))
                .andExpect(status().isConflict())
                .andExpect(content().string("No se puede eliminar la categoría porque tiene productos asociados"));
        mvc.perform(get("/api/categorias/" + escolar)).andExpect(status().isOk());
    }

    // ---------- Productos y categoría ----------

    @Test
    void productoSeAsociaAUnaCategoriaExistentePorId() throws Exception {
        long escolar = categoriaRepository.findByNombreIgnoreCase("Escolar").orElseThrow().getId();
        long antes = categoriaRepository.count();

        mvc.perform(post("/api/productos").contentType(MediaType.APPLICATION_JSON)
                        .content(json("{\"nombreModelo\":\"Regla\",\"precio\":500,\"stock\":9,"
                                + "\"tipoCategoria\":{\"id\":" + escolar + ",\"nombre\":\"cualquiera\"}}")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tipoCategoria.id").value(escolar))
                .andExpect(jsonPath("$.tipoCategoria.nombre").value("Escolar"))
                .andExpect(jsonPath("$.tipoCategoria.descripcion").exists());

        assertEquals(antes, categoriaRepository.count());
    }

    @Test
    void productoResuelveLaCategoriaPorNombreSiExiste() throws Exception {
        long antes = categoriaRepository.count();
        mvc.perform(post("/api/productos").contentType(MediaType.APPLICATION_JSON)
                        .content(json("{\"nombreModelo\":\"Lapiz\",\"precio\":300,\"stock\":4,"
                                + "\"tipoCategoria\":{\"nombre\":\"escolar\"}}")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tipoCategoria.nombre").value("Escolar"));
        assertEquals(antes, categoriaRepository.count());
    }

    @Test
    void productoConCategoriaInexistenteDevuelve400SinCrearla() throws Exception {
        long antes = categoriaRepository.count();
        mvc.perform(post("/api/productos").contentType(MediaType.APPLICATION_JSON)
                        .content(json("{\"nombreModelo\":\"Raro\",\"precio\":300,\"stock\":4,"
                                + "\"tipoCategoria\":{\"nombre\":\"No existe\"}}")))
                .andExpect(status().isBadRequest());
        mvc.perform(post("/api/productos").contentType(MediaType.APPLICATION_JSON)
                        .content(json("{\"nombreModelo\":\"Raro\",\"precio\":300,\"stock\":4,"
                                + "\"tipoCategoria\":{\"id\":99999}}")))
                .andExpect(status().isBadRequest());
        mvc.perform(put("/api/productos/1").contentType(MediaType.APPLICATION_JSON)
                        .content(json("{\"nombreModelo\":\"Raro\",\"precio\":300,\"stock\":4,"
                                + "\"tipoCategoria\":{\"nombre\":\"No existe\"}}")))
                .andExpect(status().isBadRequest());
        assertEquals(antes, categoriaRepository.count());
    }

    @Test
    void productosDevuelvenStockYCategoria() throws Exception {
        mvc.perform(get("/api/productos/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stock").isNumber())
                .andExpect(jsonPath("$.tipoCategoria.id").exists())
                .andExpect(jsonPath("$.tipoCategoria.nombre").exists())
                .andExpect(jsonPath("$.tipoCategoria.descripcion").exists());
    }

    // ---------- Clientes y boletas ----------

    private long crearCliente() throws Exception {
        MvcResult r = mvc.perform(post("/api/clientes").contentType(MediaType.APPLICATION_JSON)
                        .content(json("{\"nombreCompleto\":\"Ana Prueba\",\"email\":\"\",\"contrasena\":\"1234\","
                                + "\"numero\":\"912345678\",\"direccion\":\"Calle 1\",\"region\":\"RM\",\"comuna\":\"Santiago\"}")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contrasena").doesNotExist())
                .andReturn();
        return Long.parseLong(r.getResponse().getContentAsString().replaceAll(".*\"id\":(\\d+).*", "$1"));
    }

    @Test
    void losClientesNuncaDevuelvenLaContrasena() throws Exception {
        long id = crearCliente();

        mvc.perform(get("/api/clientes/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contrasena").doesNotExist())
                .andExpect(jsonPath("$.nombreCompleto").value("Ana Prueba"))
                .andExpect(jsonPath("$.numero").value("912345678"))
                .andExpect(jsonPath("$.direccion").value("Calle 1"))
                .andExpect(jsonPath("$.region").value("RM"))
                .andExpect(jsonPath("$.comuna").value("Santiago"));

        mvc.perform(get("/api/clientes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].contrasena").doesNotExist());
    }

    @Test
    void actualizarPerfilConservaLaContrasenaYNoLaDevuelve() throws Exception {
        long id = crearCliente();

        mvc.perform(put("/api/clientes/" + id).contentType(MediaType.APPLICATION_JSON)
                        .content(json("{\"nombreCompleto\":\"Ana Nueva\",\"email\":\"ana@prueba.cl\","
                                + "\"numero\":\"900000000\",\"direccion\":\"Calle 2\",\"region\":\"Valparaiso\",\"comuna\":\"Vina\"}")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contrasena").doesNotExist())
                .andExpect(jsonPath("$.nombreCompleto").value("Ana Nueva"))
                .andExpect(jsonPath("$.region").value("Valparaiso"))
                .andExpect(jsonPath("$.comuna").value("Vina"));

        mvc.perform(post("/api/clientes/login").contentType(MediaType.APPLICATION_JSON)
                        .content(json("{\"email\":\"ana@prueba.cl\",\"contrasena\":\"1234\"}")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.contrasena").doesNotExist());
    }

    @Test
    void laBoletaDevuelveLosDatosParaElDetalleAdministrativo() throws Exception {
        long clienteId = crearCliente();

        mvc.perform(post("/api/clientes/" + clienteId + "/carrito/items").contentType(MediaType.APPLICATION_JSON)
                        .content(json("{\"productoId\":2,\"cantidad\":3}")))
                .andExpect(status().isCreated());

        MvcResult creada = mvc.perform(post("/api/clientes/" + clienteId + "/boletas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("{\"metodoPago\":\"TARJETA\",\"direccion\":\"Calle 1, Santiago\"}")))
                .andExpect(status().isCreated())
                .andReturn();
        long boletaId = Long.parseLong(creada.getResponse().getContentAsString().replaceAll("^\\{\"id\":(\\d+).*", "$1"));

        String[] rutas = {"/api/clientes/" + clienteId + "/boletas/" + boletaId};
        for (String ruta : rutas) {
            mvc.perform(get(ruta))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(boletaId))
                    .andExpect(jsonPath("$.fecha").exists())
                    .andExpect(jsonPath("$.estado").value("CREADA"))
                    .andExpect(jsonPath("$.subtotal").value(7470.0))
                    .andExpect(jsonPath("$.impuestos").exists())
                    .andExpect(jsonPath("$.total").value(7470.0))
                    .andExpect(jsonPath("$.direccion").value("Calle 1, Santiago"))
                    .andExpect(jsonPath("$.metodoPago").value("TARJETA"))
                    .andExpect(jsonPath("$.cliente.id").value(clienteId))
                    .andExpect(jsonPath("$.cliente.nombreCompleto").value("Ana Prueba"))
                    .andExpect(jsonPath("$.cliente.email").exists())
                    .andExpect(jsonPath("$.cliente.contrasena").doesNotExist())
                    .andExpect(jsonPath("$.items.length()").value(1))
                    .andExpect(jsonPath("$.items[0].nombreProducto").exists())
                    .andExpect(jsonPath("$.items[0].cantidad").value(3))
                    .andExpect(jsonPath("$.items[0].precioUnitario").value(2490.0))
                    .andExpect(jsonPath("$.items[0].subtotal").value(7470.0));
        }

        mvc.perform(get("/api/clientes/" + clienteId + "/boletas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].items[0].subtotal").value(7470.0))
                .andExpect(jsonPath("$[0].cliente.contrasena").doesNotExist());

        mvc.perform(get("/api/boletas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(boletaId))
                .andExpect(jsonPath("$[0].fecha").exists())
                .andExpect(jsonPath("$[0].total").value(7470.0))
                .andExpect(jsonPath("$[0].direccion").value("Calle 1, Santiago"))
                .andExpect(jsonPath("$[0].cliente.id").value(clienteId))
                .andExpect(jsonPath("$[0].cliente.contrasena").doesNotExist())
                .andExpect(jsonPath("$[0].items[0].nombreProducto").exists())
                .andExpect(jsonPath("$[0].items[0].cantidad").value(3))
                .andExpect(jsonPath("$[0].items[0].subtotal").value(7470.0));
    }
}
