package org.dubini.gestion.controller;

import org.dubini.gestion.dto.MiembroRequestDto;
import org.dubini.gestion.dto.MiembroResponseDto;
import org.dubini.gestion.service.MiembroService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/miembros")
public class MiembroController {

    private final MiembroService service;

    public MiembroController(MiembroService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<Page<MiembroResponseDto>> getMiembros(Pageable pageable) {
        return ResponseEntity.ok(service.getMiembros(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<MiembroResponseDto> getMiembroById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getMiembroById(id));
    }

    @PostMapping
    public ResponseEntity<MiembroResponseDto> createMiembro(@RequestBody MiembroRequestDto dto) {
        return ResponseEntity.ok(service.createMiembro(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<MiembroResponseDto> updateMiembro(@PathVariable Long id, @RequestBody MiembroRequestDto dto) {
        return ResponseEntity.ok(service.updateMiembro(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMiembro(@PathVariable Long id) {
        service.deleteMiembro(id);
        return ResponseEntity.noContent().build();
    }
}
