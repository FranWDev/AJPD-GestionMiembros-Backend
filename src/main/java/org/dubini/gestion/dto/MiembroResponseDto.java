package org.dubini.gestion.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MiembroResponseDto {
    private Long id;
    private String nombreRazonSocial;
    private CentroDto centro;
    private String telefono;
    private String correo;
    private CargoDto cargo;
    private LocalDate fechaCargo;
    private String enlaceWhatsapp;
    private Set<HistorialCargoDto> historialCargos;
}
