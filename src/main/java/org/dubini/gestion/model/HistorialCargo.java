package org.dubini.gestion.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDate;

@Table("historial_cargos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class HistorialCargo {
    @Id
    private Long id;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private Long cargoId;
}
