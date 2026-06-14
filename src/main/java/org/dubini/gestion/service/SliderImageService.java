package org.dubini.gestion.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.dubini.gestion.config.SupabaseStorageProperties;
import org.dubini.gestion.dto.response.ImageResponseDTO;
import org.dubini.gestion.controller.SliderImageController.SliderInfoResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class SliderImageService {

    private static final Logger log = LoggerFactory.getLogger(SliderImageService.class);

    private final RestClient restClient;
    private final SupabaseStorageProperties supabaseStorageProperties;
    private final ObjectMapper objectMapper;

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg",
            "image/jpg",
            "image/png",
            "image/webp",
            "image/gif");

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB
    private static final String SLIDER_DIRECTORY = "slider";
    private static final Set<String> ALLOWED_SLIDE_NAMES = Set.of("slide1", "slide2", "slide3", "slide4", "slide5", "slide6");

    public SliderImageService(SupabaseStorageProperties supabaseStorageProperties) {
        this.supabaseStorageProperties = supabaseStorageProperties;
        this.objectMapper = new ObjectMapper();

        String baseUrl = supabaseStorageProperties.getApi();
        if (baseUrl != null && !baseUrl.startsWith("http://") && !baseUrl.startsWith("https://")) {
            baseUrl = "https://" + baseUrl;
        }

        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("Authorization", "Bearer " + supabaseStorageProperties.getKey())
                .defaultHeader("apikey", supabaseStorageProperties.getKey())
                .build();

        log.info("SliderImageService configurado con Supabase URL: {}", baseUrl);
    }

    public ImageResponseDTO updateSliderImage(String slideName, MultipartFile file) throws IOException {
        log.debug("Actualizando imagen slider: {}", slideName);

        if (!ALLOWED_SLIDE_NAMES.contains(slideName)) {
            throw new IllegalArgumentException("Nombre de slide no válido. Permitidos: slide1 a slide6");
        }

        validateImage(file);

        byte[] imageBytes = file.getBytes();
        String fileName = slideName + ".webp";
        String contentType = file.getContentType() != null ? file.getContentType() : "image/webp";

        try {
            String storagePath = "/storage/v1/object/%s/%s/%s".formatted(
                    supabaseStorageProperties.getBucket(),
                    SLIDER_DIRECTORY,
                    fileName);

            log.debug("Subiendo imagen slider a: {}", storagePath);

            restClient.put()
                    .uri(storagePath)
                    .header("Content-Type", contentType)
                    .body(imageBytes)
                    .retrieve()
                    .toBodilessEntity();

            String imageUrl = "%s/storage/v1/object/public/%s/%s/%s".formatted(
                    supabaseStorageProperties.getApi(),
                    supabaseStorageProperties.getBucket(),
                    SLIDER_DIRECTORY,
                    fileName);

            log.info("Imagen slider {} actualizada correctamente: {}", slideName, imageUrl);

            return new ImageResponseDTO(fileName, imageUrl, imageBytes.length);

        } catch (Exception e) {
            log.error("Falló la actualización de imagen slider en Supabase: {}", e.getMessage(), e);
            throw new IOException("Error al actualizar la imagen slider en Supabase Storage", e);
        }
    }

    public void updateSliderCaption(String slideName, String caption) throws IOException {
        log.debug("Actualizando caption del slide: {}", slideName);

        if (!ALLOWED_SLIDE_NAMES.contains(slideName)) {
            throw new IllegalArgumentException("Nombre de slide no válido. Permitidos: slide1 a slide6");
        }

        if (caption == null || caption.trim().isEmpty()) {
            throw new IllegalArgumentException("El caption no puede estar vacío");
        }

        if (caption.length() > 150) {
            throw new IllegalArgumentException("El caption no puede exceder 150 caracteres");
        }

        try {
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("caption", caption.trim());

            byte[] metadataBytes = objectMapper.writeValueAsBytes(metadata);

            String storagePath = "/storage/v1/object/%s/%s/%s.json".formatted(
                    supabaseStorageProperties.getBucket(),
                    SLIDER_DIRECTORY,
                    slideName);

            log.debug("Guardando caption en: {}", storagePath);

            restClient.put()
                    .uri(storagePath)
                    .header("Content-Type", "application/json")
                    .body(metadataBytes)
                    .retrieve()
                    .toBodilessEntity();

            log.info("Caption del slide {} actualizado correctamente", slideName);

        } catch (Exception e) {
            log.error("Falló la actualización del caption en Supabase: {}", e.getMessage(), e);
            throw new IOException("Error al actualizar el caption en Supabase Storage", e);
        }
    }

    public String getSliderCaption(String slideName) throws IOException {
        if (!ALLOWED_SLIDE_NAMES.contains(slideName)) {
            throw new IllegalArgumentException("Nombre de slide no válido. Permitidos: slide1 a slide6");
        }

        try {
            String storagePath = "/storage/v1/object/%s/%s/%s.json".formatted(
                    supabaseStorageProperties.getBucket(),
                    SLIDER_DIRECTORY,
                    slideName);

            String response = restClient.get()
                    .uri(storagePath)
                    .retrieve()
                    .body(String.class);

            if (response == null || response.trim().isEmpty()) {
                return "";
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> metadata = objectMapper.readValue(response, Map.class);
            return (String) metadata.getOrDefault("caption", "");

        } catch (Exception e) {
            log.warn("Caption no encontrado o error al obtener para slide {}: {}", slideName, e.getMessage());
            return "";
        }
    }

    public List<SliderInfoResponse> getAllSliderInfo() throws IOException {
        return List.of(
                new SliderInfoResponse("slide1", getSliderImageUrl("slide1"), getSliderCaption("slide1")),
                new SliderInfoResponse("slide2", getSliderImageUrl("slide2"), getSliderCaption("slide2")),
                new SliderInfoResponse("slide3", getSliderImageUrl("slide3"), getSliderCaption("slide3")),
                new SliderInfoResponse("slide4", getSliderImageUrl("slide4"), getSliderCaption("slide4")),
                new SliderInfoResponse("slide5", getSliderImageUrl("slide5"), getSliderCaption("slide5")),
                new SliderInfoResponse("slide6", getSliderImageUrl("slide6"), getSliderCaption("slide6"))
        );
    }

    public String getSliderImageUrl(String slideName) throws IOException {
        if (!ALLOWED_SLIDE_NAMES.contains(slideName)) {
            throw new IllegalArgumentException("Nombre de slide no válido. Permitidos: slide1 a slide6");
        }

        String fileName = slideName + ".webp";
        return "%s/storage/v1/object/public/%s/%s/%s".formatted(
                supabaseStorageProperties.getApi(),
                supabaseStorageProperties.getBucket(),
                SLIDER_DIRECTORY,
                fileName);
    }

    public void deleteSliderImage(String slideName) throws IOException {
        if (!ALLOWED_SLIDE_NAMES.contains(slideName)) {
            throw new IllegalArgumentException("Nombre de slide no válido. Permitidos: slide1 a slide6");
        }

        String fileName = slideName + ".webp";

        try {
            String storagePath = "/storage/v1/object/%s/%s/%s".formatted(
                    supabaseStorageProperties.getBucket(),
                    SLIDER_DIRECTORY,
                    fileName);

            restClient.delete()
                    .uri(storagePath)
                    .retrieve()
                    .toBodilessEntity();

            log.info("Imagen slide {} eliminada correctamente", slideName);

        } catch (Exception e) {
            log.error("Error al eliminar imagen slide de Supabase: {}", e.getMessage(), e);
            throw new IOException("Error al eliminar la imagen slide de Supabase Storage", e);
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
