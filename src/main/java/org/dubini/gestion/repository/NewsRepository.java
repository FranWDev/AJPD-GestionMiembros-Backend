package org.dubini.gestion.repository;

import org.dubini.gestion.model.News;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NewsRepository extends ListCrudRepository<News, String>, PagingAndSortingRepository<News, String> {
    
    @Query("SELECT * FROM news ORDER BY created_at DESC")
    List<News> findAllByOrderByCreatedAtDesc();
    
    @Query("SELECT * FROM news WHERE :searchPattern IS NULL OR LOWER(title) LIKE LOWER(:searchPattern) OR LOWER(CAST(content AS VARCHAR)) LIKE LOWER(:searchPattern) ORDER BY created_at DESC LIMIT :limit OFFSET :offset")
    List<News> findBySearchPatternPage(@Param("searchPattern") String searchPattern, 
                                       @Param("limit") int limit, 
                                       @Param("offset") long offset);
    
    @Query("SELECT COUNT(*) FROM news WHERE :searchPattern IS NULL OR LOWER(title) LIKE LOWER(:searchPattern) OR LOWER(CAST(content AS VARCHAR)) LIKE LOWER(:searchPattern)")
    long countBySearchPatternPage(@Param("searchPattern") String searchPattern);

    @Modifying
    @Query("INSERT INTO news (title, content, created_at) VALUES (:title, :content, :createdAt)")
    void insertNews(@Param("title") String title, 
                    @Param("content") Object content, 
                    @Param("createdAt") LocalDateTime createdAt);

    @Modifying
    @Query("UPDATE news SET content = :content, created_at = :createdAt WHERE title = :title")
    void updateNews(@Param("title") String title, 
                    @Param("content") Object content, 
                    @Param("createdAt") LocalDateTime createdAt);

    @Modifying
    @Query("UPDATE news SET title = :newTitle, content = :content, created_at = :createdAt WHERE title = :oldTitle")
    void updateNewsTitle(@Param("oldTitle") String oldTitle,
                         @Param("newTitle") String newTitle,
                         @Param("content") Object content,
                         @Param("createdAt") LocalDateTime createdAt);
}

