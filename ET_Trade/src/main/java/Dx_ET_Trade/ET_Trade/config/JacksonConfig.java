package Dx_ET_Trade.ET_Trade.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;

@Configuration
public class JacksonConfig {

    private static final String[] DATE_FORMATS = {
        "M/d/yyyy",
        "yyyy-MM-dd'T'HH:mm:ss",
        "yyyy-MM-dd"
    };

    @Bean
    public ObjectMapper objectMapper() {
        JavaTimeModule module = new JavaTimeModule();
        
        // Create a custom deserializer that tries multiple date formats
        module.addDeserializer(LocalDate.class, new LocalDateDeserializer(DateTimeFormatter.ofPattern(DATE_FORMATS[0])) {
            @Override
            public LocalDate deserialize(com.fasterxml.jackson.core.JsonParser parser, com.fasterxml.jackson.databind.DeserializationContext context) throws java.io.IOException {
                String dateStr = parser.getText().trim();
                if (dateStr == null || dateStr.isEmpty()) {
                    return null;
                }

                // Try each format until one works
                for (String format : DATE_FORMATS) {
                    try {
                        return LocalDate.parse(dateStr, DateTimeFormatter.ofPattern(format));
                    } catch (DateTimeParseException e) {
                        // Continue to next format
                    }
                }

                // If all formats fail, try parsing the ISO format
                try {
                    return LocalDate.parse(dateStr);
                } catch (DateTimeParseException e) {
                    throw new DateTimeParseException("Could not parse date: " + dateStr, dateStr, 0);
                }
            }
        });

        // Use standard serializer
        module.addSerializer(LocalDate.class, new LocalDateSerializer(DateTimeFormatter.ofPattern(DATE_FORMATS[0])));

        return Jackson2ObjectMapperBuilder.json()
                .modules(module)
                .featuresToDisable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .build();
    }
} 