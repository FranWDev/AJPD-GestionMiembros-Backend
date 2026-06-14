package org.dubini.gestion.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jdbc.repository.config.AbstractJdbcConfiguration;

import java.util.Arrays;
import java.util.List;

@Configuration
public class JdbcConfiguration extends AbstractJdbcConfiguration {

    @Bean
    public PGobjectToStringConverter pgobjectToStringConverter() {
        return new PGobjectToStringConverter();
    }

    @Bean
    public PostgresJsonbWritingConverter postgresJsonbWritingConverter() {
        return new PostgresJsonbWritingConverter();
    }

    @Override
    protected List<?> userConverters() {
        return Arrays.asList(
            pgobjectToStringConverter(),
            postgresJsonbWritingConverter()
        );
    }
}
