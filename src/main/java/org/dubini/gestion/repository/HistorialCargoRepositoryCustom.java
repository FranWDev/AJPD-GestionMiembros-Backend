package org.dubini.gestion.repository;

import org.dubini.gestion.dto.CargoHistorialDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.time.LocalDate;

public interface HistorialCargoRepositoryCustom {
    Page<CargoHistorialDto> findCargoHistorial(
            Long cargoId,
            LocalDate fechaInicioDesde,
            LocalDate fechaInicioHasta,
            LocalDate fechaFinDesde,
            LocalDate fechaFinHasta,
            String buscar,
            Pageable pageable
    );
}
