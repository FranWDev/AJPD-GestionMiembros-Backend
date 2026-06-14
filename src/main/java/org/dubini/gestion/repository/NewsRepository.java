package org.dubini.gestion.repository;

import org.dubini.gestion.model.News;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NewsRepository extends CrudRepository<News, String> {
    
    @Query("SELECT * FROM news ORDER BY created_at DESC")
    List<News> findAllByOrderByCreatedAtDesc();
    
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
}
