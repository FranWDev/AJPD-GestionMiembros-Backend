package org.dubini.gestion.service;

import org.dubini.gestion.config.SupabaseStorageProperties;
import org.dubini.gestion.dto.response.ImageResponseDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Set;
import java.util.UUID;

@Service
public class ImageService {

    private static final Logger log = LoggerFactory.getLogger(ImageService.class);

    private final RestClient restClient;
    private final SupabaseStorageProperties supabaseStorageProperties;

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg",
            "image/jpg",
            "image/png",
            "image/webp",
            "image/gif");

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB

    public ImageService(SupabaseStorageProperties supabaseStorageProperties) {
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

        log.info("ImageService configurado con Supabase URL: {}", baseUrl);
    }

    public ImageResponseDTO saveImage(MultipartFile file) throws IOException {
        log.debug("Subiendo imagen a Supabase: {}", file.getOriginalFilename());

        validateImage(file);

        byte[] imageBytes = file.getBytes();
        String fileName = generateUniqueFileName(file.getOriginalFilename());
        String contentType = file.getContentType() != null ? file.getContentType() : "image/jpeg";

        try {
            String storagePath = "/storage/v1/object/%s/%s".formatted(
                    supabaseStorageProperties.getBucket(),
                    fileName);

            restClient.post()
                    .uri(storagePath)
                    .header("Content-Type", contentType)
                    .body(imageBytes)
                    .retrieve()
                    .toBodilessEntity();

            String imageUrl = "%s/storage/v1/object/public/%s/%s".formatted(
                    supabaseStorageProperties.getApi(),
                    supabaseStorageProperties.getBucket(),
                    fileName);

            log.info("Imagen subida correctamente a Supabase: {}", imageUrl);

            return new ImageResponseDTO(fileName, imageUrl, imageBytes.length);

        } catch (Exception e) {
            log.error("Falló la subida a Supabase: {}", e.getMessage(), e);
            throw new IOException("Error al subir la imagen a Supabase Storage", e);
        }
    }

    private void validateImage(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new IOException("El archivo está vacío");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IOException("El archivo excede el tamaño máximo permitido de 10MB");
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
            throw new IOException("Tipo de archivo no permitido. Solo se aceptan imágenes JPEG, PNG, WEBP y GIF");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename != null) {
            String extension = getFileExtension(originalFilename).toLowerCase();
            if (!Set.of("jpg", "jpeg", "png", "webp", "gif").contains(extension)) {
                throw new IOException("Extensión de archivo no válida");
            }
        }
    }

    private String getFileExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf('.');
        return lastDotIndex > 0 ? filename.substring(lastDotIndex + 1) : "";
    }

    private String generateUniqueFileName(String originalFilename) {
        String timestamp = String.valueOf(System.currentTimeMillis());
        String uuid = UUID.randomUUID().toString().substring(0, 8);
        String extension = getFileExtension(originalFilename);

        if (extension.isEmpty() || !Set.of("jpg", "jpeg", "png", "webp", "gif").contains(extension.toLowerCase())) {
            extension = "jpg";
        }

        return "%s_%s.%s".formatted(timestamp, uuid, extension);
    }

    public void deleteImage(String fileName) throws IOException {
        try {
            String storagePath = "/storage/v1/object/%s/%s".formatted(
                    supabaseStorageProperties.getBucket(),
                    fileName);

            restClient.delete()
                    .uri(storagePath)
                    .retrieve()
                    .toBodilessEntity();

            log.info("Imagen eliminada correctamente: {}", fileName);

        } catch (Exception e) {
            log.error("Error al eliminar imagen de Supabase: {}", e.getMessage(), e);
            throw new IOException("Error al eliminar la imagen de Supabase Storage", e);
        }
    }
}
