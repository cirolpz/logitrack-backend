package com.logitrack.logitrack_api.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Envio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String trackingId;
    @NotBlank
    private String dni;
    @NotBlank
    private String nombre;
    private String apellido;
    @NotBlank
    private String direccion;
    @NotBlank
    private String codigoPostal;
    @NotNull
    private Double peso;
    @Enumerated(EnumType.STRING)
    private EstadoEnvio estado;}
