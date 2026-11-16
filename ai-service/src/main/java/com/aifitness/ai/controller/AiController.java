
package com.aifitness.ai.controller;

import com.aifitness.ai.dto.RecommendationRequest;
import com.aifitness.ai.dto.RecommendationResponse;
import com.aifitness.ai.service.GeminiAiService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
public class AiController {

    private final GeminiAiService geminiAiService;

    public AiController(GeminiAiService geminiAiService) {
        this.geminiAiService = geminiAiService;
    }

    @PostMapping("/recommendations")
    public ResponseEntity<RecommendationResponse> getRecommendations(
            @Valid @RequestBody RecommendationRequest request) {

        return ResponseEntity.ok(
                geminiAiService.generate(request)
        );
    }
}