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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageImpl;
import org.dubini.gestion.config.PostgresJsonbWritingConverter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.Cookie;

@Service
@Transactional(readOnly = true)
public class NewsService {

    private static final Logger log = LoggerFactory.getLogger(NewsService.class);

    private final NewsRepository newsRepository;
    private final ObjectMapper objectMapper;
    private final CacheInvalidatorService cacheInvalidation;
    private final PostgresJsonbWritingConverter postgresJsonbWritingConverter;

    public NewsService(NewsRepository newsRepository, ObjectMapper objectMapper, CacheInvalidatorService cacheInvalidation, PostgresJsonbWritingConverter postgresJsonbWritingConverter) {
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

    public List<PublicationDTO> getAll(String search) {
        log.debug("Retrieving all news with search: {}", search);
        String searchPattern = (search == null || search.trim().isEmpty()) ? null : "%" + search.trim() + "%";
        
        List<News> newsList;
        if (searchPattern == null) {
            newsList = newsRepository.findAllByOrderByCreatedAtDesc();
        } else {
            newsList = newsRepository.findBySearchPatternPage(searchPattern, Integer.MAX_VALUE, 0);
        }
        
        List<PublicationDTO> publications = newsList.stream()
                .map(this::parseNewsToDTO)
                .filter(pub -> pub != null)
                .collect(Collectors.toList());
        log.debug("Retrieved {} news articles", publications.size());
        return publications;
    }

    public List<PublicationDTO> getAll() {
        return getAll(null);
    }

    public Object getNews(String search, Pageable pageable, Integer page, Integer size, HttpServletRequest request) {
        boolean isPublicRequest = false;
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("jwt".equals(cookie.getName())) {
                    isPublicRequest = true;
                    break;
                }
            }
        }
        
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            isPublicRequest = true;
        }

        if (isPublicRequest) {
            return getAll(search);
        }

        if (page == null || size == null) {
            return getAll(search);
        }
        return getPaginated(search, pageable);
    }

    public Page<PublicationDTO> getPaginated(String search, Pageable pageable) {
        log.debug("Retrieving page {} of size {} with search: {}", pageable.getPageNumber(), pageable.getPageSize(), search);
        String searchPattern = (search == null || search.trim().isEmpty()) ? null : "%" + search.trim() + "%";
        
        long total = newsRepository.countBySearchPatternPage(searchPattern);
        if (total == 0) {
            return Page.empty(pageable);
        }
        
        List<News> newsList = newsRepository.findBySearchPatternPage(searchPattern, pageable.getPageSize(), pageable.getOffset());
        List<PublicationDTO> publications = newsList.stream()
                .map(this::parseNewsToDTO)
                .filter(pub -> pub != null)
                .collect(Collectors.toList());
                
        return new PageImpl<>(publications, pageable, total);
    }

    @Transactional
    public PublicationDTO save(PublicationDTO publicationDTO) {
        log.debug("Saving news: {}", publicationDTO.getTitle());
        validatePublication(publicationDTO);
        String safeTitle = sanitizeFileName(publicationDTO.getTitle());

        try {
            LocalDateTime createdAt = LocalDateTime.now();
            String safeOldTitle = null;
            if (publicationDTO.oldTitle != null && !publicationDTO.oldTitle.trim().isEmpty()) {
                safeOldTitle = sanitizeFileName(publicationDTO.oldTitle);
            }

            if (safeOldTitle != null && !safeOldTitle.equals(safeTitle)) {
                createdAt = newsRepository.findById(safeOldTitle)
                        .map(News::getCreatedAt)
                        .orElse(LocalDateTime.now());

                if (publicationDTO.getCreatedAt() == null) {
                    publicationDTO.setCreatedAt(createdAt.atOffset(java.time.ZoneOffset.UTC).format(java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME));
                }

                String jsonContent = objectMapper.writeValueAsString(publicationDTO);
                Object convertedContent = postgresJsonbWritingConverter.convert(jsonContent);

                if (newsRepository.existsById(safeOldTitle)) {
                    if (newsRepository.existsById(safeTitle)) {
                        throw new IllegalArgumentException("Ya existe otra noticia con el nuevo título especificado");
                    }
                    newsRepository.updateNewsTitle(safeOldTitle, safeTitle, convertedContent, createdAt);
                } else {
                    newsRepository.insertNews(safeTitle, convertedContent, createdAt);
                }
            } else {
                createdAt = newsRepository.findById(safeTitle)
                        .map(News::getCreatedAt)
                        .orElse(LocalDateTime.now());

                if (publicationDTO.getCreatedAt() == null) {
                    publicationDTO.setCreatedAt(createdAt.atOffset(java.time.ZoneOffset.UTC).format(java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME));
                }

                String jsonContent = objectMapper.writeValueAsString(publicationDTO);
                Object convertedContent = postgresJsonbWritingConverter.convert(jsonContent);

                if (newsRepository.existsById(safeTitle)) {
                    newsRepository.updateNews(safeTitle, convertedContent, createdAt);
                } else {
                    newsRepository.insertNews(safeTitle, convertedContent, createdAt);
                }
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
