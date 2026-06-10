package org.dubini.gestion.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CargoHistorialDto {
    private Long id;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private Long cargoId;
    private String cargoNombre;
    private Long miembroId;
    private String miembroNombre;
    private String miembroNif;
}
