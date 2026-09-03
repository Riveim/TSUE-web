package com.tsue.backend.controller;

import com.tsue.backend.dto.AiRequest;
import com.tsue.backend.dto.AiResponse;
import com.tsue.backend.service.AiService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
public class AiController {

    private final AiService aiService;

    public AiController(AiService aiService) {
        this.aiService = aiService;
    }

    @PostMapping("/ask")
    public ResponseEntity<AiResponse> ask(@RequestBody AiRequest request) {

        String response = aiService.ask(request.prompt());

        return ResponseEntity.ok(
                new AiResponse(response)
        );
    }
}