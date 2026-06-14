package org.dubini.gestion.dto;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class PublicationDTO {

    public String title;
    public String description;
    public String imageUrl;
    public String createdAt;
    public EditorJSContentDTO editorContent;
    private OffsetDateTime createdAtDateTime;
    public String oldTitle;

    public PublicationDTO() {
    }

    public PublicationDTO(String title, String description, String imageUrl, EditorJSContentDTO editorContent) {
        this.title = title;
        this.description = description;
        this.imageUrl = imageUrl;
        this.editorContent = editorContent;
    }

    public OffsetDateTime getCreatedAtDateTime() {
        if (createdAtDateTime == null && createdAt != null) {
            try {
                createdAtDateTime = OffsetDateTime.parse(createdAt, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
            } catch (Exception e1) {
                try {
                    createdAtDateTime = OffsetDateTime.of(
                            java.time.LocalDateTime.parse(createdAt, DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                            java.time.ZoneOffset.UTC);
                } catch (Exception e2) {
                    createdAtDateTime = null;
                }
            }
        }
        return createdAtDateTime;
    }
}
