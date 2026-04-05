package com.logitrack.logitrack_api.controller;

import com.logitrack.logitrack_api.model.EstadoEnvio;
import com.logitrack.logitrack_api.model.HistorialEstado;
import com.logitrack.logitrack_api.repository.EnvioRepository;
import com.logitrack.logitrack_api.repository.HistorialEstadoRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/dashboard")
@CrossOrigin(origins = "https://logitrack-prototype.vercel.app")
public class DashboardController {

    private final EnvioRepository envioRepository;
    private final HistorialEstadoRepository historialRepository;

    public DashboardController(EnvioRepository envioRepository, HistorialEstadoRepository historialRepository) {
        this.envioRepository = envioRepository;
        this.historialRepository = historialRepository;
    }

    @GetMapping("/resumen")
    public ResponseEntity<Map<String, Object>> resumen() {
        // Totales por estado
        Map<String, Long> porEstado = new LinkedHashMap<>();
        for (EstadoEnvio estado : EstadoEnvio.values()) {
            porEstado.put(estado.name(), envioRepository.countByEstado(estado));
        }

        // Actividad reciente (últimas 10 transiciones)
        List<HistorialEstado> reciente = historialRepository.findTop10ByOrderByFechaHoraDesc();

        // Usuarios más activos
        List<Map<String, Object>> topUsuarios = new ArrayList<>();
        for (Object[] fila : historialRepository.contarPorUsuario()) {
            topUsuarios.add(Map.of("usuario", fila[0], "acciones", fila[1]));
        }

        Map<String, Object> respuesta = new LinkedHashMap<>();
        respuesta.put("totalEnvios", envioRepository.count());
        respuesta.put("enviosPorEstado", porEstado);
        respuesta.put("actividadReciente", reciente);
        respuesta.put("usuariosMasActivos", topUsuarios);

        return ResponseEntity.ok(respuesta);
    }
}
