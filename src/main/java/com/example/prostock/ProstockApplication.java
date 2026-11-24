package com.example.prostock;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/** Esta clase configura y pone en marcha la aplicación Spring Boot. */
@SpringBootApplication
public class ProstockApplication {

	/** Inicia el servidor y carga el contexto de la aplicación. */
	public static void main(String[] args) {
		SpringApplication.run(ProstockApplication.class, args);
	}

}
