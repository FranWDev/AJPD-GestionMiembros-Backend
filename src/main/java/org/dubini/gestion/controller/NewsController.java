package org.dubini.gestion.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.dubini.gestion.dto.PublicationDTO;
import org.dubini.gestion.service.NewsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/news")
@Tag(name = "Noticias", description = "Endpoints para la gestión de publicaciones de noticias")
public class NewsController {

    private final NewsService service;

    public NewsController(NewsService service) {
        this.service = service;
    }

    @Operation(summary = "Obtener todas las noticias")
    @GetMapping
    public ResponseEntity<List<PublicationDTO>> get() {
        return ResponseEntity.ok(service.get());
    }

    @Operation(summary = "Obtener noticia por identificador")
    @GetMapping("/{identifier}")
    public ResponseEntity<PublicationDTO> get(@PathVariable String identifier) {
        return ResponseEntity.ok(service.get(identifier));
    }

    @Operation(summary = "Crear o actualizar noticia")
    @PostMapping
    public ResponseEntity<PublicationDTO> create(@Valid @RequestBody PublicationDTO publicationDTO) {
        return ResponseEntity.ok(service.save(publicationDTO));
    }

    @Operation(summary = "Eliminar noticia por identificador")
    @DeleteMapping("/{identifier}")
    public ResponseEntity<Void> delete(@PathVariable String identifier) {
        service.delete(identifier);
        return ResponseEntity.noContent().build();
    }
}
