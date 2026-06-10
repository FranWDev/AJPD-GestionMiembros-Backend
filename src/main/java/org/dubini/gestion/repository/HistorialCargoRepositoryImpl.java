package org.dubini.gestion.repository;

import org.dubini.gestion.dto.CargoHistorialDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Repository
public class HistorialCargoRepositoryImpl implements HistorialCargoRepositoryCustom {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public HistorialCargoRepositoryImpl(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Page<CargoHistorialDto> findCargoHistorial(
            Long cargoId,
            LocalDate fechaInicioDesde,
            LocalDate fechaInicioHasta,
            LocalDate fechaFinDesde,
            LocalDate fechaFinHasta,
            String buscar,
            Pageable pageable
    ) {
        List<String> whereClauses = new ArrayList<>();
        MapSqlParameterSource params = new MapSqlParameterSource();

        if (cargoId != null) {
            whereClauses.add("hc.CARGO_ID = :cargoId");
            params.addValue("cargoId", cargoId);
        }

        if (fechaInicioDesde != null) {
            whereClauses.add("hc.FECHA_INICIO >= :fechaInicioDesde");
            params.addValue("fechaInicioDesde", fechaInicioDesde);
        }
        if (fechaInicioHasta != null) {
            whereClauses.add("hc.FECHA_INICIO <= :fechaInicioHasta");
            params.addValue("fechaInicioHasta", fechaInicioHasta);
        }

        if (fechaFinDesde != null) {
            whereClauses.add("hc.FECHA_FIN >= :fechaFinDesde");
            params.addValue("fechaFinDesde", fechaFinDesde);
        }
        if (fechaFinHasta != null) {
            whereClauses.add("hc.FECHA_FIN <= :fechaFinHasta");
            params.addValue("fechaFinHasta", fechaFinHasta);
        }

        if (buscar != null && !buscar.trim().isEmpty()) {
            String searchPattern = "%" + buscar.trim().toLowerCase() + "%";
            whereClauses.add("(LOWER(m.NOMBRE_RAZON_SOCIAL) LIKE :buscarVal OR LOWER(m.CORREO) LIKE :buscarVal OR m.TELEFONO LIKE :buscarVal OR LOWER(m.NIF_CIF) LIKE :buscarVal)");
            params.addValue("buscarVal", searchPattern);
        }

        StringBuilder countSql = new StringBuilder(
                "SELECT COUNT(*) FROM historial_cargos hc " +
                "JOIN miembros m ON hc.miembro_id = m.ID " +
                "JOIN cargos c ON hc.CARGO_ID = c.ID"
        );
        StringBuilder querySql = new StringBuilder(
                "SELECT hc.ID AS id, hc.FECHA_INICIO AS fecha_inicio, hc.FECHA_FIN AS fecha_fin, " +
                "hc.CARGO_ID AS cargo_id, c.NOMBRE AS cargo_nombre, " +
                "m.ID AS miembro_id, m.NOMBRE_RAZON_SOCIAL AS miembro_nombre, m.NIF_CIF AS miembro_nif " +
                "FROM historial_cargos hc " +
                "JOIN miembros m ON hc.miembro_id = m.ID " +
                "JOIN cargos c ON hc.CARGO_ID = c.ID"
        );

        if (!whereClauses.isEmpty()) {
            String wherePart = " WHERE " + String.join(" AND ", whereClauses);
            countSql.append(wherePart);
            querySql.append(wherePart);
        }

        Integer total = jdbcTemplate.queryForObject(countSql.toString(), params, Integer.class);
        if (total == null || total == 0) {
            return Page.empty(pageable);
        }

        querySql.append(" ").append(getOrderByClause(pageable.getSort()));
        querySql.append(" LIMIT :limit OFFSET :offset");
        params.addValue("limit", pageable.getPageSize());
        params.addValue("offset", pageable.getOffset());

        List<CargoHistorialDto> list = jdbcTemplate.query(querySql.toString(), params, (rs, rowNum) -> {
            CargoHistorialDto dto = new CargoHistorialDto();
            dto.setId(rs.getLong("id"));
            dto.setFechaInicio(rs.getObject("fecha_inicio", LocalDate.class));
            dto.setFechaFin(rs.getObject("fecha_fin", LocalDate.class));
            dto.setCargoId(rs.getLong("cargo_id"));
            dto.setCargoNombre(rs.getString("cargo_nombre"));
            dto.setMiembroId(rs.getLong("miembro_id"));
            dto.setMiembroNombre(rs.getString("miembro_nombre"));
            dto.setMiembroNif(rs.getString("miembro_nif"));
            return dto;
        });

        return new PageImpl<>(list, pageable, total);
    }

    private String getOrderByClause(Sort sort) {
        if (sort == null || sort.isUnsorted()) {
            return "ORDER BY hc.FECHA_INICIO DESC";
        }
        List<String> orders = new ArrayList<>();
        for (Sort.Order order : sort) {
            String property = order.getProperty();
            String column;
            switch (property) {
                case "fechaInicio": column = "hc.FECHA_INICIO"; break;
                case "fechaFin": column = "hc.FECHA_FIN"; break;
                case "cargoNombre": column = "c.NOMBRE"; break;
                case "miembroNombre": column = "m.NOMBRE_RAZON_SOCIAL"; break;
                default: column = "hc.FECHA_INICIO"; break;
            }
            orders.add(column + " " + order.getDirection().name());
        }
        return "ORDER BY " + String.join(", ", orders);
    }
}
