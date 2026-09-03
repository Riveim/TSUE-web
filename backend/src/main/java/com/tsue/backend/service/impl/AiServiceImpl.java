package com.tsue.backend.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tsue.backend.service.AiService;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.util.Map;

@Service
public class AiServiceImpl implements AiService {

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    public AiServiceImpl(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;

        this.restClient = RestClient.builder()
                .baseUrl("http://localhost:11434")
                .requestFactory(createRequestFactory())
                .build();
    }

    private org.springframework.http.client.ClientHttpRequestFactory createRequestFactory() {
        var factory = new org.springframework.http.client.SimpleClientHttpRequestFactory();

        factory.setConnectTimeout(Duration.ofSeconds(10));
        factory.setReadTimeout(Duration.ofMinutes(5));

        return factory;
    }

    @Override
    public String ask(String prompt) {

        Map<String, Object> request = Map.of(
                "model", "qwen3:4b",
                "prompt", prompt,
                "stream", false
        );

        String response = restClient.post()
                .uri("/api/generate")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(String.class);

        try {
            JsonNode json = objectMapper.readTree(response);

            return json.get("response").asText();

        } catch (Exception e) {
            throw new RuntimeException(
                    "Failed to parse Ollama response",
                    e
            );
        }
    }
}