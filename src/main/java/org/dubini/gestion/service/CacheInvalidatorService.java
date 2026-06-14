package org.dubini.gestion.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dubini.gestion.client.CacheInvalidationClient;
import org.dubini.gestion.dto.response.HttpResponse;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class CacheInvalidatorService {

    private final CacheInvalidationClient cacheInvalidation;

    public HttpResponse invalidateNewsCache() {
        try {
            cacheInvalidation.invalidateNewsCache();
            return new HttpResponse("News cache invalidated");
        } catch (Exception err) {
            log.error("Error invalidating news cache", err);
            return new HttpResponse("Error invalidating news cache");
        }
    }

    public HttpResponse invalidateServiceWorkersCache() {
        try {
            cacheInvalidation.invalidateServiceWorkersCache();
            return new HttpResponse("Service worker cache invalidated");
        } catch (Exception err) {
            log.error("Error invalidating sw cache", err);
            return new HttpResponse("Error invalidating sw cache");
        }
    }
}
