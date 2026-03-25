package com.logitrack.logitrack_api.controller;

import com.logitrack.logitrack_api.model.Envio;
import com.logitrack.logitrack_api.service.EnvioService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/envios")
public class EnvioController {
    private final EnvioService service;

    public EnvioController(EnvioService service) {
        this.service = service;
    }
    @PostMapping
    public Envio crearEnvio(@RequestBody Envio envio) {
        return service.crearEnvio(envio);
    }
    @GetMapping
    public List<Envio> obtenerTodos() {
        return service.obtenerTodos();
    }

    @GetMapping("/{trackingId}")
    public Envio getEnvioByTrackingId(@PathVariable String trackingId) {
        return service.getEnvioByTrackingId(trackingId);
    }
}
