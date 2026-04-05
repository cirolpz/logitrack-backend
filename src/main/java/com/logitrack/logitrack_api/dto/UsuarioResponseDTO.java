package com.logitrack.logitrack_api.dto;

import lombok.Data;

@Data
public class UsuarioResponseDTO {
    private Long id;
    private String usuario;
    private String nombre;
    private String apellido;
    private String rol;
}
