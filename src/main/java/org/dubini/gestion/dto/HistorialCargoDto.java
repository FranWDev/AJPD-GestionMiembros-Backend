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
public class HistorialCargoDto {
    private Long id;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private CargoDto cargo;
}
