package com.invoice.system.invoice_system.util;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import net.logstash.logback.marker.LogstashMarker;
import net.logstash.logback.marker.Markers;

import java.util.List;

@Slf4j
public class ObjectMapperUtil {
    public static final ObjectMapper objectMapper = JsonMapper.builder()
            .addModules(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .build();

    public static <T> T readValue(String json, Class<T> className) {
        try {
            final JavaType type = objectMapper.getTypeFactory().constructType(className);
            return objectMapper.readValue(json, type);
        } catch (Exception ex) {
            LogstashMarker marker = Markers.append("json", json)
                    .and(Markers.append("className", className));
            log.error(marker, "Exception occurred in objectMapperUtil while converting the json value", ex);
            throw new RuntimeException(ex);
        }
    }

    public static <T> List<T> readValue(String json, Class<T> className, Class collectionType) {
        try {
            final JavaType type = objectMapper.getTypeFactory().constructCollectionType(collectionType, className);
            return objectMapper.readValue(json, type);
        } catch (Exception ex) {
            LogstashMarker marker = Markers.append("json", json)
                    .and(Markers.append("className", className))
                    .and(Markers.append("collectionType", collectionType));
            log.error(marker, "Exception occurred in objectMapperUtil while converting the json value to collectionType", ex);
            throw new RuntimeException(ex);
        }
    }

    public static String writeValueAsString(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (Exception ex) {
            LogstashMarker marker = Markers.append("object", object);
            log.error(marker, "Exception occurred in objectMapperUtil while writing value to string", ex);
            throw new RuntimeException(ex);
        }
    }
}
