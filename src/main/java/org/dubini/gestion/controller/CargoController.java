package org.dubini.gestion.controller;

import jakarta.validation.Valid;
import org.dubini.gestion.dto.CargoDto;
import org.dubini.gestion.dto.CargoHistorialDto;
import org.dubini.gestion.dto.CargoHistorialEditDto;
import org.dubini.gestion.service.CargoService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/cargos")
public class CargoController {

    private final CargoService service;

    public CargoController(CargoService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<Page<CargoDto>> getCargos(
            @RequestParam(required = false) String nombre,
            Pageable pageable) {
        return ResponseEntity.ok(service.getCargos(nombre, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CargoDto> getCargoById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getCargoById(id));
    }

    @PostMapping
    public ResponseEntity<CargoDto> createCargo(@Valid @RequestBody CargoDto dto) {
        return ResponseEntity.ok(service.createCargo(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CargoDto> updateCargo(@PathVariable Long id, @Valid @RequestBody CargoDto dto) {
        return ResponseEntity.ok(service.updateCargo(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCargo(@PathVariable Long id) {
        service.deleteCargo(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/historial")
    public ResponseEntity<Page<CargoHistorialDto>> getCargoHistorial(
            @RequestParam(required = false) Long cargoId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicioDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicioHasta,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFinDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFinHasta,
            @RequestParam(required = false) String buscar,
            Pageable pageable
    ) {
        return ResponseEntity.ok(service.getCargoHistorial(
                cargoId,
                fechaInicioDesde,
                fechaInicioHasta,
                fechaFinDesde,
                fechaFinHasta,
                buscar,
                pageable
        ));
    }

    @PutMapping("/historial/{id}")
    public ResponseEntity<CargoHistorialDto> updateCargoHistorial(
            @PathVariable Long id,
            @Valid @RequestBody CargoHistorialEditDto dto
    ) {
        return ResponseEntity.ok(service.updateCargoHistorial(id, dto));
    }
}
