package com.logitrack.logitrack_api.service;

import com.logitrack.logitrack_api.dto.EnvioRequestDTO;
import com.logitrack.logitrack_api.dto.EnvioResponseDTO;
import com.logitrack.logitrack_api.model.Envio;
import com.logitrack.logitrack_api.model.EstadoEnvio;
import com.logitrack.logitrack_api.repository.EnvioRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.UUID;

@Service
public class EnvioService {

    private final EnvioRepository repository;

    // Lee la URL desde application.properties (que a su vez lee la env var IA_SERVICE_URL)
    @Value("${ia.service.url}")
    private String iaServiceUrl;

    public EnvioService(EnvioRepository repository) {
        this.repository = repository;
    }

    public EnvioResponseDTO crearEnvio(EnvioRequestDTO dto) {

        Envio envio = new Envio();
        envio.setTrackingId(UUID.randomUUID().toString());
        envio.setEstado(EstadoEnvio.REGISTRADO);
        envio.setDni(dto.getDni());
        envio.setNombre(dto.getNombre());
        envio.setApellido(dto.getApellido());
        envio.setDireccion(dto.getDireccion());
        envio.setCodigoPostalDestino(dto.getCodigoPostalDestino());
        envio.setCodigoPostalOrigen(dto.getCodigoPostalOrigen());
        envio.setPeso(dto.getPeso());

        String prioridadResult = consultarPrioridadIA(dto);
        envio.setPrioridad(prioridadResult);

        repository.save(envio);
        return mapToResponse(envio);
    }

    private String consultarPrioridadIA(EnvioRequestDTO dto) {
        try {
            HttpClient client = HttpClient.newHttpClient();
            ObjectMapper mapper = new ObjectMapper();

            Map<String, Object> data = new HashMap<>();
            data.put("cp_origen", dto.getCodigoPostalOrigen());
            data.put("cp_destino", dto.getCodigoPostalDestino());
            data.put("peso", dto.getPeso());
            data.put("tipo_envio", "Estándar");

            String jsonBody = mapper.writeValueAsString(data);

            // ✅ Ahora usa la variable de entorno en vez de localhost hardcodeado
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(iaServiceUrl + "/predict"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            Map<String, String> resultMap = mapper.readValue(response.body(), Map.class);
            return resultMap.getOrDefault("prioridad", "BAJA");

        } catch (Exception e) {
            System.err.println("Error de conexión con la IA: " + e.getMessage());
            return "BAJA";
        }
    }

    private EnvioResponseDTO mapToResponse(Envio envio) {
        EnvioResponseDTO dto = new EnvioResponseDTO();
        dto.setTrackingId(envio.getTrackingId());
        dto.setNombre(envio.getNombre());
        dto.setApellido(envio.getApellido());
        dto.setDireccion(envio.getDireccion());
        dto.setEstado(envio.getEstado());
        dto.setPrioridad(envio.getPrioridad());
        return dto;
    }

    public List<Envio> obtenerTodos() {
        return repository.findAll();
    }

    public Envio getEnvioByTrackingId(String trackingId) {
        return repository.findByTrackingId(trackingId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Envio no encontrado"));
    }

    public Envio actualizarEstado(String trackingId, EstadoEnvio nuevoEstado) {
        Envio envio = repository.findByTrackingId(trackingId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Envio no encontrado"));
        EstadoEnvio estadoActual = envio.getEstado();
        if (!esTransicionValida(estadoActual, nuevoEstado)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Transicion de estado invalida: " + estadoActual + " -> " + nuevoEstado);
        }
        envio.setEstado(nuevoEstado);
        return repository.save(envio);
    }

    public List<Envio> buscarPorNombre(String nombre) {
        return repository.findByNombreContainingIgnoreCase(nombre);
    }

    private boolean esTransicionValida(EstadoEnvio actual, EstadoEnvio nuevo) {
        return switch (actual) {
            case REGISTRADO -> nuevo == EstadoEnvio.EN_TRANSITO;
            case EN_TRANSITO -> nuevo == EstadoEnvio.EN_SUCURSAL;
            case EN_SUCURSAL -> nuevo == EstadoEnvio.ENTREGADO;
            case ENTREGADO -> false;
        };
    }
}