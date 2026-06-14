package org.dubini.gestion.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.dubini.gestion.dto.response.HttpResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/heartbeat")
@Tag(name = "Heartbeat", description = "Endpoints para verificar el estado de salud del servicio")
public class HeartbeatController {

    @Operation(summary = "Verificar el estado del servicio", description = "Devuelve un estado exitoso indicando que el servicio está activo")
    @GetMapping
    public ResponseEntity<HttpResponse> heartbeat() {
        return ResponseEntity.ok(new HttpResponse("Heartbeat OK - service is active"));
    }
}
