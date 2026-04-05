package com.logitrack.logitrack_api.dto;

import lombok.Data;

@Data
public class LoginRequestDTO {
    private String usuario;
    private String clave;
}
