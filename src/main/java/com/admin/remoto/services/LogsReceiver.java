package com.admin.remoto.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class LogsReceiver {
    private final ObjectMapper mapper = new ObjectMapper();

    public Map<String, String> parse(String json) {
        try {
            return mapper.readValue(json, new TypeReference<>() {});
        } catch (Exception e) {
            return Map.of("type", "text", "message", json);
        }
    }
}
