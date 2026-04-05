package com.logitrack.logitrack_api.service;

import com.logitrack.logitrack_api.dto.LoginRequestDTO;
import com.logitrack.logitrack_api.dto.UsuarioResponseDTO;
import com.logitrack.logitrack_api.model.Usuario;
import com.logitrack.logitrack_api.repository.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class UsuarioService {

    private final UsuarioRepository repository;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public UsuarioService(UsuarioRepository repository) {
        this.repository = repository;
    }

    public UsuarioResponseDTO login(LoginRequestDTO dto) {
        Usuario usuario = repository.findByUsuario(dto.getUsuario())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciales incorrectas"));

        if (!encoder.matches(dto.getClave(), usuario.getClave())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciales incorrectas");
        }

        return mapToResponse(usuario);
    }

    public List<UsuarioResponseDTO> obtenerTodos() {
        return repository.findAll().stream().map(this::mapToResponse).toList();
    }

    public UsuarioResponseDTO cambiarRol(Long id, String nuevoRol) {
        if (!nuevoRol.equals("Operador") && !nuevoRol.equals("Supervisor")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Rol invalido: " + nuevoRol);
        }
        Usuario usuario = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));
        usuario.setRol(nuevoRol);
        return mapToResponse(repository.save(usuario));
    }

    private UsuarioResponseDTO mapToResponse(Usuario u) {
        UsuarioResponseDTO dto = new UsuarioResponseDTO();
        dto.setId(u.getId());
        dto.setUsuario(u.getUsuario());
        dto.setNombre(u.getNombre());
        dto.setApellido(u.getApellido());
        dto.setRol(u.getRol());
        return dto;
    }
}
