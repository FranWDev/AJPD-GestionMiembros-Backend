package org.dubini.gestion.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.dubini.gestion.dto.PublicationDTO;
import org.dubini.gestion.exception.ResourceNotFoundException;
import org.dubini.gestion.model.News;
import org.dubini.gestion.repository.NewsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class NewsService {

    private static final Logger log = LoggerFactory.getLogger(NewsService.class);

    private final NewsRepository newsRepository;
    private final ObjectMapper objectMapper;
    private final CacheInvalidatorService cacheInvalidation;
    private final org.dubini.gestion.config.PostgresJsonbWritingConverter postgresJsonbWritingConverter;

    public NewsService(NewsRepository newsRepository, ObjectMapper objectMapper, CacheInvalidatorService cacheInvalidation, org.dubini.gestion.config.PostgresJsonbWritingConverter postgresJsonbWritingConverter) {
        this.newsRepository = newsRepository;
        this.objectMapper = objectMapper;
        this.cacheInvalidation = cacheInvalidation;
        this.postgresJsonbWritingConverter = postgresJsonbWritingConverter;
    }

    public PublicationDTO get(String identifier) {
        log.debug("Retrieving news with identifier: {}", identifier);
        String safeTitle = sanitizeFileName(identifier);
        return newsRepository.findById(safeTitle)
                .map(this::parseNewsToDTO)
                .orElseThrow(() -> new ResourceNotFoundException("Noticia no encontrada"));
    }

    public List<PublicationDTO> get() {
        log.debug("Retrieving all news");
        List<News> newsList = newsRepository.findAllByOrderByCreatedAtDesc();
        List<PublicationDTO> publications = newsList.stream()
                .map(this::parseNewsToDTO)
                .filter(pub -> pub != null)
                .collect(Collectors.toList());
        log.debug("Retrieved {} news articles", publications.size());
        return publications;
    }

    @Transactional
    public PublicationDTO save(PublicationDTO publicationDTO) {
        log.debug("Saving news: {}", publicationDTO.getTitle());
        validatePublication(publicationDTO);
        String safeTitle = sanitizeFileName(publicationDTO.getTitle());

        try {
            String jsonContent = objectMapper.writeValueAsString(publicationDTO);
            LocalDateTime createdAt = newsRepository.findById(safeTitle)
                    .map(News::getCreatedAt)
                    .orElse(LocalDateTime.now());

            Object convertedContent = postgresJsonbWritingConverter.convert(jsonContent);
            if (newsRepository.existsById(safeTitle)) {
                newsRepository.updateNews(safeTitle, convertedContent, createdAt);
            } else {
                newsRepository.insertNews(safeTitle, convertedContent, createdAt);
            }
            log.info("News saved successfully: {}", publicationDTO.getTitle());
        } catch (JsonProcessingException e) {
            log.error("Error serializing news: {}", publicationDTO.getTitle(), e);
            throw new RuntimeException("Error al guardar la noticia", e);
        }

        try {
            cacheInvalidation.invalidateNewsCache();
            log.info("News cache invalidated after save");
        } catch (Exception e) {
            log.error("Error invalidating cache after save: {}", e.getMessage());
        }

        return publicationDTO;
    }

    @Transactional
    public void delete(String identifier) {
        log.debug("Deleting news: {}", identifier);
        String safeTitle = sanitizeFileName(identifier);

        if (!newsRepository.existsById(safeTitle)) {
            log.warn("News not found for deletion: {}", identifier);
            throw new ResourceNotFoundException("Noticia no encontrada: " + identifier);
        }

        newsRepository.deleteById(safeTitle);
        log.info("News deleted successfully: {}", identifier);

        try {
            cacheInvalidation.invalidateNewsCache();
            log.info("News cache invalidated after delete");
        } catch (Exception e) {
            log.error("Error invalidating cache after delete: {}", e.getMessage());
        }
    }

    private String sanitizeFileName(String filename) {
        if (filename == null || filename.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del archivo no puede estar vacío");
        }

        String sanitized = filename
                .toLowerCase()
                .trim()
                .replaceAll("\\s+", "-")
                .replaceAll("[^\\w\\-]", "")
                .replaceAll("\\-+", "-");

        if (sanitized.length() > 200) {
            sanitized = sanitized.substring(0, 200);
        }

        return sanitized;
    }

    private PublicationDTO parseNewsToDTO(News news) {
        try {
            return objectMapper.readValue(news.getContent(), PublicationDTO.class);
        } catch (JsonProcessingException e) {
            log.error("Error parsing news content for: {}", news.getTitle(), e);
            return null;
        }
    }

    private void validatePublication(PublicationDTO publicationDTO) {
        if (publicationDTO == null) {
            throw new IllegalArgumentException("La noticia no puede ser nula");
        }
        if (publicationDTO.getTitle() == null || publicationDTO.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("El título es obligatorio");
        }
    }
}
