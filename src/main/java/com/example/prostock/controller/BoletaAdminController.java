package com.example.prostock.controller;

import com.example.prostock.model.Boleta;
import com.example.prostock.service.BoletaService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Proporciona la vista administrativa para consultar las boletas de todos los clientes. */
@RestController
@RequestMapping({"/api/boletas", "/api/v1/boletas"})
@SecurityRequirement(name = "BearerAuth")
public class BoletaAdminController {

    private final BoletaService boletaService;

    public BoletaAdminController(BoletaService boletaService) {
        this.boletaService = boletaService;
    }

    /** Lista todas las boletas ordenadas desde la más reciente. */
    @GetMapping
    public ResponseEntity<List<Boleta>> listarTodas() {
        return ResponseEntity.ok(boletaService.listarTodas());
    }
}
