package org.dubini.gestion.repository;

import org.dubini.gestion.model.Miembro;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

@Repository
public interface MiembroRepository extends ListCrudRepository<Miembro, Long>, PagingAndSortingRepository<Miembro, Long>, MiembroRepositoryCustom {

    @Query("SELECT miembro_id FROM historial_cargos WHERE ID = :historialId")
    Optional<Long> findMiembroIdByHistorialId(@Param("historialId") Long historialId);
}
