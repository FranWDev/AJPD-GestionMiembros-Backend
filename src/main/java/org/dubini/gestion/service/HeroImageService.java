package org.dubini.gestion.service;

import org.dubini.gestion.config.SupabaseStorageProperties;
import org.dubini.gestion.dto.response.ImageResponseDTO;
import org.dubini.gestion.controller.HeroImageController.HeroImageUrlResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Set;

@Service
public class HeroImageService {

    private static final Logger log = LoggerFactory.getLogger(HeroImageService.class);

    private final RestClient restClient;
    private final SupabaseStorageProperties supabaseStorageProperties;

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg",
            "image/jpg",
            "image/png",
            "image/webp",
            "image/gif");

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB
    private static final String HERO_DIRECTORY = "hero";
    private static final Set<String> ALLOWED_HERO_NAMES = Set.of("hero1", "hero2", "hero3");

    public HeroImageService(SupabaseStorageProperties supabaseStorageProperties) {
        this.supabaseStorageProperties = supabaseStorageProperties;

        String baseUrl = supabaseStorageProperties.getApi();
        if (baseUrl != null && !baseUrl.startsWith("http://") && !baseUrl.startsWith("https://")) {
            baseUrl = "https://" + baseUrl;
        }

        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("Authorization", "Bearer " + supabaseStorageProperties.getKey())
                .defaultHeader("apikey", supabaseStorageProperties.getKey())
                .build();

        log.info("HeroImageService configurado con Supabase URL: {}", baseUrl);
    }

    public ImageResponseDTO updateHeroImage(String heroName, MultipartFile file) throws IOException {
        log.debug("Actualizando imagen hero: {}", heroName);

        if (!ALLOWED_HERO_NAMES.contains(heroName)) {
            throw new IllegalArgumentException("Nombre de imagen hero no válido. Permitidos: hero1, hero2, hero3");
        }

        validateImage(file);

        byte[] imageBytes = file.getBytes();
        String fileName = heroName + ".webp";
        String contentType = file.getContentType() != null ? file.getContentType() : "image/webp";

        try {
            String storagePath = "/storage/v1/object/%s/%s/%s".formatted(
                    supabaseStorageProperties.getBucket(),
                    HERO_DIRECTORY,
                    fileName);

            log.debug("Subiendo imagen hero a: {}", storagePath);

            // Supabase API uses POST to create and PUT to update/overwrite. Since we want to overwrite, PUT is correct.
            restClient.put()
                    .uri(storagePath)
                    .header("Content-Type", contentType)
                    .body(imageBytes)
                    .retrieve()
                    .toBodilessEntity();

            String imageUrl = "%s/storage/v1/object/public/%s/%s/%s".formatted(
                    supabaseStorageProperties.getApi(),
                    supabaseStorageProperties.getBucket(),
                    HERO_DIRECTORY,
                    fileName);

            log.info("Imagen hero {} actualizada correctamente: {}", heroName, imageUrl);

            return new ImageResponseDTO(fileName, imageUrl, imageBytes.length);

        } catch (Exception e) {
            log.error("Falló la actualización de imagen hero en Supabase: {}", e.getMessage(), e);
            throw new IOException("Error al actualizar la imagen hero en Supabase Storage", e);
        }
    }

    public List<HeroImageUrlResponse> getAllHeroImages() throws IOException {
        return List.of(
                new HeroImageUrlResponse("hero1", getHeroImageUrl("hero1")),
                new HeroImageUrlResponse("hero2", getHeroImageUrl("hero2")),
                new HeroImageUrlResponse("hero3", getHeroImageUrl("hero3"))
        );
    }

    public String getHeroImageUrl(String heroName) throws IOException {
        if (!ALLOWED_HERO_NAMES.contains(heroName)) {
            throw new IllegalArgumentException("Nombre de imagen hero no válido. Permitidos: hero1, hero2, hero3");
        }

        String fileName = heroName + ".webp";
        return "%s/storage/v1/object/public/%s/%s/%s".formatted(
                supabaseStorageProperties.getApi(),
                supabaseStorageProperties.getBucket(),
                HERO_DIRECTORY,
                fileName);
    }

    public void deleteHeroImage(String heroName) throws IOException {
        if (!ALLOWED_HERO_NAMES.contains(heroName)) {
            throw new IllegalArgumentException("Nombre de imagen hero no válido. Permitidos: hero1, hero2, hero3");
        }

        String fileName = heroName + ".webp";

        try {
            String storagePath = "/storage/v1/object/%s/%s/%s".formatted(
                    supabaseStorageProperties.getBucket(),
                    HERO_DIRECTORY,
                    fileName);

            restClient.delete()
                    .uri(storagePath)
                    .retrieve()
                    .toBodilessEntity();

            log.info("Imagen hero {} eliminada correctamente", heroName);

        } catch (Exception e) {
            log.error("Error al eliminar imagen hero de Supabase: {}", e.getMessage(), e);
            throw new IOException("Error al eliminar la imagen hero de Supabase Storage", e);
        }
    }

    private void validateImage(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("El archivo está vacío");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("El archivo excede el tamaño máximo permitido de 10MB");
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
            throw new IllegalArgumentException("Tipo de archivo no permitido. Solo se aceptan imágenes JPEG, PNG, WEBP y GIF");
        }
    }
}
