package org.dubini.gestion.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.WritingConverter;
import org.springframework.stereotype.Component;

@Component
@WritingConverter
public class PostgresJsonbWritingConverter implements Converter<String, Object> {

    @Value("${spring.datasource.driver-class-name:}")
    private String driverClassName;
    
    @Override
    public Object convert(String source) {
        if (driverClassName != null && driverClassName.contains("h2")) {
            // H2 handles standard String for JSON/VARCHAR columns directly
            return source;
        }
        try {
            Class<?> pgObjectClass = Class.forName("org.postgresql.util.PGobject");
            Object pgObject = pgObjectClass.getDeclaredConstructor().newInstance();
            
            pgObjectClass.getMethod("setType", String.class).invoke(pgObject, "jsonb");
            pgObjectClass.getMethod("setValue", String.class).invoke(pgObject, source);
            
            return pgObject;
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo convertir String a PGobject para jsonb", e);
        }
    }
}
