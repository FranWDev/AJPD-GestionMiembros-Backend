package org.dubini.gestion.repository;

import org.dubini.gestion.model.HistorialCargo;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HistorialCargoRepository extends ListCrudRepository<HistorialCargo, Long>, HistorialCargoRepositoryCustom {

    @Query("SELECT * FROM historial_cargos WHERE miembro_id = :miembroId ORDER BY FECHA_INICIO DESC")
    List<HistorialCargo> findByMiembroIdOrderByFechaInicioDesc(@Param("miembroId") Long miembroId);

    @Query("SELECT * FROM historial_cargos WHERE miembro_id IN (:miembroIds)")
    List<HistorialCargo> findByMiembroIdIn(@Param("miembroIds") java.util.Collection<Long> miembroIds);
}
