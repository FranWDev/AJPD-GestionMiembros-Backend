package org.dubini.gestion.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Data
@Getter
@Setter
public class EditorJSContentDTO {
    private long time;
    private List<EditorJSBlock> blocks;
    private String version;
}
