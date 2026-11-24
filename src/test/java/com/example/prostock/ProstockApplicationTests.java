package com.example.prostock;

import com.example.prostock.model.Cliente;
import com.example.prostock.repository.ClienteRepository;
import com.example.prostock.security.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Confirma que el contexto completo de Spring Boot pueda iniciar en el entorno de pruebas. */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ProstockApplicationTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ClienteRepository clienteRepository;

	@Autowired
	private JwtService jwtService;

	@Test
	void contextLoads() {
	}

	@Test
	void documentacionOpenApiIncluyeLasRutasV1() throws Exception {
		mockMvc.perform(get("/v3/api-docs/v1"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.info.version").value("v1"))
				.andExpect(jsonPath("$.paths['/api/v1/productos/ofertas']").exists());
	}

	@Test
	void loginEmiteJwtYMigraLaContrasenaAnterior() throws Exception {
		String email = "jwt-" + System.nanoTime() + "@example.com";
		Cliente cliente = new Cliente();
		cliente.setNombreCompleto("Cliente JWT");
		cliente.setEmail(email);
		cliente.setContrasena("1234");
		clienteRepository.save(cliente);

		var login = mockMvc.perform(post("/api/clientes/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"email\":\"" + email + "\",\"contrasena\":\"1234\"}"))
				.andExpect(status().isOk())
				.andExpect(header().exists(HttpHeaders.AUTHORIZATION))
				.andExpect(header().string("X-Auth-Role", "CLIENTE"))
				.andExpect(jsonPath("$.email").value(email))
				.andReturn();

		String token = login.getResponse().getHeader(HttpHeaders.AUTHORIZATION);
		assertTrue(token != null && token.startsWith("Bearer "));
		assertTrue(clienteRepository.findByEmail(email).orElseThrow().getContrasena().startsWith("$2"));
	}

	@Test
	void registroGuardaLaContrasenaConHashBcrypt() throws Exception {
		String email = "registro-" + System.nanoTime() + "@example.com";
		mockMvc.perform(post("/api/v1/clientes")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"nombreCompleto\":\"Registro JWT\",\"email\":\"" + email
								+ "\",\"contrasena\":\"clave\"}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.contrasena").doesNotExist());

		assertTrue(clienteRepository.findByEmail(email).orElseThrow().getContrasena().startsWith("$2"));
	}

	@Test
	void carritoRequiereJwtYRechazaElAccesoAlCarritoDeOtroCliente() throws Exception {
		Cliente cliente = new Cliente();
		cliente.setNombreCompleto("Cliente protegido");
		cliente.setEmail("owner-" + System.nanoTime() + "@example.com");
		cliente.setContrasena("1234");
		cliente = clienteRepository.save(cliente);
		String token = "Bearer " + jwtService.crearToken(cliente, false);

		mockMvc.perform(get("/api/v1/clientes/" + cliente.getId() + "/carrito"))
				.andExpect(status().isUnauthorized());
		mockMvc.perform(get("/api/v1/clientes/" + cliente.getId() + "/carrito")
						.header(HttpHeaders.AUTHORIZATION, token))
				.andExpect(status().isOk());
		mockMvc.perform(get("/api/v1/clientes/" + cliente.getId() + "/carrito")
						.header(HttpHeaders.AUTHORIZATION, "Bearer token-malformado"))
				.andExpect(status().isUnauthorized());
		mockMvc.perform(get("/api/clientes/" + (cliente.getId() + 1) + "/carrito")
						.header(HttpHeaders.AUTHORIZATION, token))
				.andExpect(status().isForbidden());
	}

	@Test
	void catalogoEsPublicoYListadoAdministrativoRequiereRolAdmin() throws Exception {
		mockMvc.perform(get("/api/v1/productos"))
				.andExpect(status().isOk());
		mockMvc.perform(get("/api/boletas"))
				.andExpect(status().isUnauthorized());

		Cliente admin = new Cliente();
		admin.setId(987654L);
		admin.setEmail("admin@example.com");
		String tokenAdminGoogle = "Bearer " + jwtService.crearToken(admin, true);
		mockMvc.perform(get("/api/v1/boletas")
						.header(HttpHeaders.AUTHORIZATION, tokenAdminGoogle))
				.andExpect(status().isOk());
		mockMvc.perform(get("/api/v1/categorias")
						.header(HttpHeaders.AUTHORIZATION, tokenAdminGoogle))
				.andExpect(status().isOk());
	}

	@Test
	void vendedorPuedeGestionarProductosYConsultarBoletasPeroNoAdministrarCategorias() throws Exception {
		Cliente vendedor = new Cliente();
		vendedor.setId(987655L);
		vendedor.setEmail("seller@example.com");
		String tokenVendedor = "Bearer " + jwtService.crearToken(vendedor, true);

		mockMvc.perform(get("/api/v1/boletas")
						.header(HttpHeaders.AUTHORIZATION, tokenVendedor))
				.andExpect(status().isOk());
		mockMvc.perform(post("/api/v1/productos")
						.header(HttpHeaders.AUTHORIZATION, tokenVendedor)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"nombreModelo\":\"Producto vendedor\",\"precio\":100,\"stock\":2}"))
				.andExpect(status().isCreated());
		mockMvc.perform(post("/api/v1/categorias")
						.header(HttpHeaders.AUTHORIZATION, tokenVendedor)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"nombre\":\"No autorizado\"}"))
				.andExpect(status().isForbidden());
	}

	@Test
	void cuentaAdministrativaNoObtieneRolPrivilegiadoConLoginDeContrasena() throws Exception {
		Cliente admin = new Cliente();
		admin.setId(987656L);
		admin.setEmail("admin@example.com");
		assertTrue("CLIENTE".equals(jwtService.obtenerRol(admin, false)));
		assertTrue("ADMIN".equals(jwtService.obtenerRol(admin, true)));
	}

}
