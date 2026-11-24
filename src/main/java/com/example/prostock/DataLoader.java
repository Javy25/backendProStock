package com.example.prostock;

import com.example.prostock.model.Blogs;
import com.example.prostock.model.Producto;
import com.example.prostock.model.TipoCategoria;
import com.example.prostock.repository.BlogsRepository;
import com.example.prostock.repository.ProductoRepository;
import com.example.prostock.repository.TipoCategoriaRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/** Carga datos de ejemplo al iniciar, únicamente cuando las tablas correspondientes están vacías. */
@Configuration
public class DataLoader {

    /** Registra una publicación de muestra y crea el catálogo inicial si aún no hay productos. */
    @Bean
    CommandLineRunner initDatabase(
            TipoCategoriaRepository tipoCategoriaRepository,
            ProductoRepository productoRepository,
            BlogsRepository blogsRepository) {

        return args -> {
            if (blogsRepository.count() == 0) {
                blogsRepository.save(new Blogs(
                        null,
                        "5 Tips para Organizar el Inventario de tu Oficina",
                        "Aprende a gestionar el stock de papelería e insumos para evitar pérdidas y desabastecimiento.",
                        "Un inventario ordenado reduce quiebres de stock, compras urgentes y pérdidas de materiales. "
                                + "Estas prácticas permiten mantener el control sin complejidad.\n\n"
                                + "1. Clasifica los insumos\n"
                                + "Ordena los productos según su frecuencia de uso y define responsables para los artículos críticos.\n\n"
                                + "2. Registra entradas y salidas\n"
                                + "Actualiza el inventario cada vez que recibas o entregues materiales. El registro oportuno evita diferencias con el stock físico.\n\n"
                                + "3. Define un stock mínimo\n"
                                + "Establece un límite de reposición por producto para anticipar las compras antes de quedarte sin unidades.\n\n"
                                + "4. Asigna una ubicación\n"
                                + "Etiqueta estantes y zonas de almacenamiento. Un lugar fijo facilita el recuento y reduce el tiempo de búsqueda.\n\n"
                                + "5. Realiza revisiones periódicas\n"
                                + "Contrasta el inventario registrado con el físico de forma mensual para detectar diferencias a tiempo.",
                        "https://images.unsplash.com/photo-1497215728101-856f4ea42174?w=1200&q=80"
                ));
            }

            if (productoRepository.count() > 0) {
                return;
            }

            TipoCategoria papeleriaOficina = tipoCategoriaRepository.save(
                    new TipoCategoria("Papelería y Oficina", "Productos de oficina, papelería y útiles de trabajo")
            );
            TipoCategoria escolar = tipoCategoriaRepository.save(
                    new TipoCategoria("Escolar", "Útiles y materiales para estudio y colegio")
            );
            TipoCategoria insumos = tipoCategoriaRepository.save(
                    new TipoCategoria("Insumos", "Insumos y consumibles para oficina y hogar")
            );
            TipoCategoria aseoLimpieza = tipoCategoriaRepository.save(
                    new TipoCategoria("Aseo y Limpieza", "Productos de higiene, limpieza y cuidado del entorno")
            );

            List<Producto> productos = List.of(
                    new Producto(3290, "Resma de papel PAPELAWELA tamaño carta (500 hojas, 75g)", "Resma de papel bond tamaño carta (500 hojas, 75g)", "https://media.falabella.com/falabellaCL/155704368_01/w=1500,h=1500,fit=cover", papeleriaOficina, 55),
                    new Producto(2490, "Carpeta Colgante Isofit Termolaminada Azul", "Carpeta Colgante Isofit Termolaminada Azul", "https://media.falabella.com/falabellaCL/157094835_01/w=1500,h=1500,fit=cover", papeleriaOficina, 60),
                    new Producto(4690, "Calculadora de Doble Pantalla Electrónica De 12 dígitos", "Calculadora de Doble Pantalla Electrónica De 12 dígitos", "https://media.falabella.com/falabellaCL/116536989_01/w=1500,h=1500,fit=cover", papeleriaOficina, 45),
                    new Producto(4190, "Perforadora de papel de dos orificios de uso pesado", "Perforadora de papel de dos orificios de uso pesado", "https://media.falabella.com/falabellaCL/148506226_01/w=1500,h=1500,fit=cover", papeleriaOficina, 70),
                    new Producto(4490, "Sello Automático Pagado Tinta Negra Timbre De Oficina", "Sello Automático Pagado Tinta Negra Timbre De Oficina", "https://media.falabella.com/falabellaCL/143200098_01/w=1500,h=1500,fit=cover", papeleriaOficina, 30),
                    new Producto(1790, "Cuaderno Universitario Paint 100hj 7mm Proarte Surtido", "Cuaderno Universitario Paint 100hj 7mm Proarte Surtido", "https://media.falabella.com/falabellaCL/151011056_01/w=1500,h=1500,fit=cover", escolar, 80),
                    new Producto(2890, "Set de 18 Lápices de Madera para Colorear Stabilo", "Set de 18 Lápices de Madera para Colorear Stabilo", "https://media.falabella.com/falabellaCL/141244708_01/w=1500,h=1500,fit=cover", escolar, 85),
                    new Producto(4590, "Estuche Escolar Doble Cierre Azul 24x14cm Adix", "Estuche Escolar Doble Cierre Azul 24x14cm Adix", "https://media.falabella.com/falabellaCL/146764213_01/w=1500,h=1500,fit=cover", escolar, 90),
                    new Producto(1200, "Tijeras Punta Roma de 5 Pulgadas Pacific", "Tijeras Punta Roma de 5 Pulgadas Pacific", "https://media.falabella.com/falabellaCL/127893931_01/w=1500,h=1500,fit=cover", escolar, 95),
                    new Producto(1890, "Compás De Precisión Metálico Con Adaptador Y Brazo Extensor", "Compás De Precisión Metálico Con Adaptador Y Brazo Extensor", "https://media.falabella.com/falabellaCL/142192300_01/w=1500,h=1500,fit=cover", escolar, 100),
                    new Producto(3500, "Cinta para embalaje uso general 100m x 48mm", "Cinta para embalaje uso general 100m x 48mm", "https://media.falabella.com/sodimacCL/4040953_01/w=1500,h=1500,fit=cover", insumos, 105),
                    new Producto(6990, "Toner G&G TN1060 - HL 1110 /HL 1112 / DCP 1512", "Toner G&G TN1060 - HL 1110 /HL 1112 / DCP 1512", "https://media.falabella.com/falabellaCL/139865154_01/w=1500,h=1500,fit=cover", insumos, 75),
                    new Producto(3690, "Grapadora Trabajo Pesado T50 - 8 Mm - 1000 unidades", "Grapadora Trabajo Pesado T50 - 8 Mm - 1000 unidades", "https://media.falabella.com/sodimacCL/687022_01/w=1500,h=1500,fit=cover", insumos, 24),
                    new Producto(1250, "Notas Adhesivas Amarillo 7.5x7.5cm Torre 100Hj", "Notas Adhesivas Amarillo 7.5x7.5cm Torre 100Hj", "https://media.falabella.com/falabellaCL/147949015_01/w=1500,h=1500,fit=cover", insumos, 40),
                    new Producto(2490, "Blister Pilas Alcalinas AAA X 8 Power One", "Blister Pilas Alcalinas AAA X 8 Power One", "https://media.falabella.com/falabellaCL/16979902_1/w=1500,h=1500,fit=cover", insumos, 50),
                    new Producto(3490, "Limpia pisos Líquido Kleine Wolke 5 Litros para Todo Tipo de Piso", "Limpia pisos Líquido Kleine Wolke 5 Litros para Todo Tipo de Piso", "https://media.falabella.com/sodimacCL/4022173_01/w=1500,h=1500,fit=cover", aseoLimpieza, 30),
                    new Producto(5090, "Papel Higiénico Industrial 300 mt x 6", "Papel Higiénico Industrial 300 mt x 6", "https://media.falabella.com/falabellaCL/140806052_01/w=1500,h=1500,fit=cover", aseoLimpieza, 55),
                    new Producto(5900, "Alcohol gel Antibacterial al 70% 5L Con Registro ISP", "Alcohol gel Antibacterial al 70% 5L Con Registro ISP", "https://media.falabella.com/falabellaCL/144687626_02/w=1500,h=1500,fit=cover", aseoLimpieza, 90),
                    new Producto(3690, "Bolsa De Basura Industrial 10un 110×120 Cm", "Bolsa De Basura Industrial 10un 110×120 Cm", "https://media.falabella.com/falabellaCL/140821751_01/w=1500,h=1500,fit=cover", aseoLimpieza, 45),
                    new Producto(1990, "Pack 3 Paños Limpieza Microfibra 40x40 Cm Suave Absorbente", "Pack 3 Paños Limpieza Microfibra 40x40 Cm Suave Absorbente", "https://media.falabella.com/falabellaCL/146767916_01/w=1500,h=1500,fit=cover", aseoLimpieza, 150)
            );

            // Descuentos de ejemplo solo para la carga inicial
            productos.get(0).setDescuento(10);
            productos.get(2).setDescuento(15);
            productos.get(6).setDescuento(20);
            productos.get(11).setDescuento(10);
            productos.get(16).setDescuento(15);

            productoRepository.saveAll(productos);
        };
    }
}
