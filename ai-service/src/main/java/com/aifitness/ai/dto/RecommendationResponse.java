
package com.aifitness.ai.dto;

public record RecommendationResponse(
        Long userId,
        String recommendation,
        String model
) {
}