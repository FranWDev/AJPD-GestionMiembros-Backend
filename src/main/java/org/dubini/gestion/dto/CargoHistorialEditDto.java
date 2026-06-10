package org.dubini.gestion.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CargoHistorialEditDto {
    @NotNull(message = "La fecha de inicio es obligatoria")
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    @NotNull(message = "El id del cargo es obligatorio")
    private Long cargoId;
}
