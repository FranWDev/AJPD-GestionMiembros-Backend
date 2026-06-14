package org.dubini.gestion.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.dubini.gestion.dto.response.HttpResponse;
import org.dubini.gestion.service.CacheInvalidatorService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/cache")
@Tag(name = "Cache Invalidation", description = "Endpoints para invalidar la caché del frontend")
public class CacheInvalidatorController {

    private final CacheInvalidatorService cacheInvalidatorService;

    public CacheInvalidatorController(CacheInvalidatorService cacheInvalidatorService) {
        this.cacheInvalidatorService = cacheInvalidatorService;
    }

    @Operation(summary = "Invalidar la caché de noticias")
    @GetMapping("/invalidate/news")
    public HttpResponse invalidateNewsCache() {
        return cacheInvalidatorService.invalidateNewsCache();
    }

    @Operation(summary = "Invalidar la caché de service workers")
    @GetMapping("/invalidate/service-workers")
    public HttpResponse invalidateServiceWorkersCache() {
        return cacheInvalidatorService.invalidateServiceWorkersCache();
    }
}
