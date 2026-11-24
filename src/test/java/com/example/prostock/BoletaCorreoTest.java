package com.example.prostock;

import com.example.prostock.model.Boleta;
import com.example.prostock.model.Cliente;
import com.example.prostock.model.ItemCarrito;
import com.example.prostock.repository.BoletaRepository;
import com.example.prostock.repository.CarritoRepository;
import com.example.prostock.repository.ClienteRepository;
import com.example.prostock.repository.ProductoRepository;
import com.example.prostock.service.BoletaService;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.Test;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Properties;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Verifica que el servicio genere el correo de confirmación usando los datos de la boleta. */
class BoletaCorreoTest {

    @Test
    void enviaCorreoUsandoDatosCopiadosDeLaBoleta() {
        JavaMailSender mailSender = mock(JavaMailSender.class);
        when(mailSender.createMimeMessage())
                .thenReturn(new MimeMessage(Session.getInstance(new Properties())));

        BoletaService service = new BoletaService(
                mock(BoletaRepository.class),
                mock(CarritoRepository.class),
                mock(ClienteRepository.class),
                mock(ProductoRepository.class),
                mailSender);
        ReflectionTestUtils.setField(service, "mailRemitente", "tienda@example.com");

        Cliente cliente = new Cliente();
        cliente.setEmail("cliente@example.com");
        Boleta boleta = new Boleta();
        boleta.setId(42L);
        boleta.setCliente(cliente);
        boleta.setFecha(LocalDateTime.now());
        boleta.setItems(List.of(new ItemCarrito(1L, 2L, "Producto", 1, 100.0)));
        boleta.setTotal(100.0);

        service.enviarBoletaPorEmailAsync(boleta).join();

        verify(mailSender).send(any(MimeMessage.class));
    }
}
