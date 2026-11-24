package com.example.prostock.service;

import com.example.prostock.model.Boleta;
import com.example.prostock.model.Carrito;
import com.example.prostock.model.Cliente;
import com.example.prostock.model.ItemCarrito;
import com.example.prostock.model.Producto;
import com.example.prostock.repository.BoletaRepository;
import com.example.prostock.repository.CarritoRepository;
import com.example.prostock.repository.ClienteRepository;
import com.example.prostock.repository.ProductoRepository;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/** Convierte carritos en boletas, actualiza inventario y coordina las notificaciones por correo. */
@Service
public class BoletaService {

    private static final Logger logger = LoggerFactory.getLogger(BoletaService.class);

    private final BoletaRepository boletaRepository;
    private final CarritoRepository carritoRepository;
    private final ClienteRepository clienteRepository;
    private final ProductoRepository productoRepository;
    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String mailRemitente;

    public BoletaService(BoletaRepository boletaRepository,
                         CarritoRepository carritoRepository,
                         ClienteRepository clienteRepository,
                         ProductoRepository productoRepository,
                         JavaMailSender mailSender) {
        this.boletaRepository = boletaRepository;
        this.carritoRepository = carritoRepository;
        this.clienteRepository = clienteRepository;
        this.productoRepository = productoRepository;
        this.mailSender = mailSender;
    }

    /**
     * Crea la boleta dentro de una transacción: vuelve a validar y descontar stock, aplica
     * el precio ofertado vigente, copia los artículos y vacía el carrito al confirmar.
     */
    @Transactional
    public Boleta crearDesdeCarrito(Long clienteId, String metodoPago, String direccion) {
        Cliente cliente = clienteRepository.findById(clienteId)
                .orElseThrow(() -> new IllegalArgumentException("Cliente no encontrado."));

        Carrito carrito = carritoRepository.findByClienteId(clienteId)
                .orElseThrow(() -> new IllegalArgumentException("Carrito no encontrado para el cliente."));

        if (carrito.getItems() == null || carrito.getItems().isEmpty()) {
            throw new IllegalArgumentException("El carrito estÃ¡ vacÃ­o.");
        }

        // Descontar stock de cada producto
        for (ItemCarrito item : carrito.getItems()) {
            Producto producto = productoRepository.findById(item.getProductoId())
                    .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado ID: " + item.getProductoId()));

            if (producto.getStock() == null || producto.getStock() < item.getCantidad()) {
                throw new IllegalArgumentException("Stock insuficiente para el producto: " + producto.getNombreModelo());
            }

            producto.setStock(producto.getStock() - item.getCantidad());
            productoRepository.save(producto);

            // El precio de la boleta lo define el servidor (con descuento vigente)
            item.setPrecioUnitario(producto.getPrecioOferta() != null ? producto.getPrecioOferta().doubleValue() : 0.0);
        }

        Boleta boleta = new Boleta();
        boleta.setCliente(cliente);
        List<ItemCarrito> itemsBoleta = new ArrayList<>();
        carrito.getItems().forEach(item -> itemsBoleta.add(item.copy()));
        boleta.setItems(itemsBoleta);
        boleta.setImpuestos(carrito.getImpuestos());
        boleta.setMetodoPago(metodoPago);
        boleta.setDireccion(direccion);
        boleta.setFecha(LocalDateTime.now());
        boleta.setEstado("CREADA");
        boleta.calcularTotal();

        Boleta guardada = boletaRepository.save(boleta);

        // Enviar boleta por email de forma asÃ­ncrona si existe email
        if (guardada.getCliente() != null && guardada.getCliente().getEmail() != null && !guardada.getCliente().getEmail().isBlank()) {
            enviarBoletaPorEmailAsync(crearDatosCorreo(guardada));
        }

        // Vaciar carrito despuÃ©s de crear boleta
        carrito.getItems().clear();
        carrito.calcularTotal();
        carritoRepository.save(carrito);

        return guardada;
    }

    /** Inicia el envío sin bloquear la respuesta de compra y devuelve el resultado asíncrono. */
    public CompletableFuture<Void> enviarBoletaPorEmailAsync(Boleta boleta) {
        return enviarBoletaPorEmailAsync(crearDatosCorreo(boleta));
    }

    // Ejecuta el envío fuera del flujo de compra; el fallo se registra sin revertir la boleta.
    private CompletableFuture<Void> enviarBoletaPorEmailAsync(DatosCorreoBoleta datos) {
        return CompletableFuture.runAsync(() -> {
            try {
                enviarBoletaPorEmail(datos);
                logger.info("Correo de confirmación enviado para la boleta {}", datos.id());
            } catch (Exception ex) {
                logger.error("No se pudo enviar el correo de confirmación para la boleta {}",
                        datos.id(), ex);
            }
        });
    }

    // Copia los valores de la boleta para que el correo no dependa de entidades JPA ya modificadas.
    private DatosCorreoBoleta crearDatosCorreo(Boleta boleta) {
        String destino = boleta.getCliente().getEmail();
        List<ItemCorreoBoleta> items = boleta.getItems().stream()
                .map(item -> new ItemCorreoBoleta(
                        item.getNombreProducto(),
                        item.getCantidad(),
                        item.getPrecioUnitario(),
                        item.getSubtotal()))
                .collect(Collectors.toUnmodifiableList());
        return new DatosCorreoBoleta(boleta.getId(), destino, boleta.getFecha(), items, boleta.getTotal());
    }

    /** Construye y envía el mensaje HTML con el detalle y total de la compra. */
    private void enviarBoletaPorEmail(DatosCorreoBoleta datos) throws Exception {
        MimeMessage msg = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(msg, true, "UTF-8");
        helper.setTo(datos.destino());
        helper.setFrom(mailRemitente);
        helper.setSubject("Tu boleta - Pedido #" + datos.id());

        StringBuilder html = new StringBuilder();
        html.append("<h2>Gracias por tu compra</h2>");
        html.append("<p>Boleta #").append(datos.id()).append(" - ").append(datos.fecha()).append("</p>");
        html.append("<table border='1' cellpadding='6' cellspacing='0'>");
        html.append("<tr><th>Producto</th><th>Cantidad</th><th>Precio</th><th>Subtotal</th></tr>");
        datos.items().forEach(i -> {
            html.append("<tr>");
            html.append("<td>").append(i.nombreProducto()).append("</td>");
            html.append("<td>").append(i.cantidad()).append("</td>");
            html.append("<td>").append(String.format("%.2f", i.precioUnitario())).append("</td>");
            html.append("<td>").append(String.format("%.2f", i.subtotal())).append("</td>");
            html.append("</tr>");
        });
        html.append("<tr><td colspan='3' align='right'><b>Total</b></td><td><b>")
                .append(String.format("%.2f", datos.total()))
                .append("</b></td></tr>");
        html.append("</table>");

        helper.setText(html.toString(), true);
        mailSender.send(msg);
    }

    private record DatosCorreoBoleta(
            Long id,
            String destino,
            LocalDateTime fecha,
            List<ItemCorreoBoleta> items,
            Double total) {
    }

    private record ItemCorreoBoleta(
            String nombreProducto,
            Integer cantidad,
            Double precioUnitario,
            Double subtotal) {
    }

    /** Recupera todas las boletas vinculadas al cliente indicado. */
    public List<Boleta> listarPorCliente(Long clienteId) {
        return boletaRepository.findByClienteId(clienteId);
    }

    /** Recupera las boletas en orden cronológico descendente para administración. */
    public List<Boleta> listarTodas() {
        return boletaRepository.findAllByOrderByFechaDesc();
    }

    /** Busca una boleta y verifica que esté asociada al cliente solicitante. */
    public Boleta obtener(Long clienteId, Long boletaId) {
        return boletaRepository.findById(boletaId)
                .filter(b -> b.getCliente() != null && b.getCliente().getId().equals(clienteId))
                .orElseThrow(() -> new IllegalArgumentException("Boleta no encontrada para ese cliente."));
    }
}
