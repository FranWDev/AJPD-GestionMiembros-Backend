package org.dubini.gestion.client;

import org.dubini.gestion.config.FrontendApiUrlProperties;
import org.dubini.gestion.dto.response.HttpResponse;
import org.dubini.gestion.security.JwtProvider;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class CacheInvalidationClient {

    private final JwtProvider jwtProvider;
    private final RestClient restClient;

    public CacheInvalidationClient(JwtProvider jwtProvider, FrontendApiUrlProperties frontendApiUrlProperties) {
        this.jwtProvider = jwtProvider;
        String baseUrl = frontendApiUrlProperties.getUrl();
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
    }

    public HttpResponse invalidateNewsCache() {
        String jwt = jwtProvider.generateShortLivedToken();

        return restClient.get()
                .uri("/api/cache/news/clear")
                .cookie("jwt", jwt)
                .retrieve()
                .onStatus(HttpStatusCode::is5xxServerError, (request, response) -> {
                    throw new RuntimeException("Error del servidor al invalidar la caché de noticias");
                })
                .body(HttpResponse.class);
    }

    public HttpResponse invalidateServiceWorkersCache() {
        String jwt = jwtProvider.generateShortLivedToken();

        return restClient.post()
                .uri("/api/service-workers/update")
                .cookie("jwt", jwt)
                .retrieve()
                .onStatus(HttpStatusCode::is5xxServerError, (request, response) -> {
                    throw new RuntimeException("Error del servidor al invalidar la caché de los service workers");
                })
                .body(HttpResponse.class);
    }
}
