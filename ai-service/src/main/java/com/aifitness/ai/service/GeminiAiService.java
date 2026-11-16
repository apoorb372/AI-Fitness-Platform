
package com.aifitness.ai.service;

import com.aifitness.ai.dto.RecommendationRequest;
import com.aifitness.ai.dto.RecommendationResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@Service
public class GeminiAiService {

    private static final Logger logger =
            LoggerFactory.getLogger(GeminiAiService.class);

    private static final String GROQ_PROVIDER = "Groq";
    private static final String GEMINI_PROVIDER = "Gemini";

    private final RestClient groqClient;
    private final RestClient geminiClient;
    private final ObjectMapper objectMapper;

    private final String groqApiKey;
    private final String groqModel;
    private final String geminiApiKey;
    private final String geminiModel;

    public GeminiAiService(
            ObjectMapper objectMapper,

            @Value("${groq.api.url:https://api.groq.com/openai/v1}")
            String groqApiUrl,

            @Value("${groq.api.model:openai/gpt-oss-120b}")
            String groqModel,

            @Value("${groq.api.key:}")
            String groqApiKey,

            @Value("${gemini.api.url:https://generativelanguage.googleapis.com/v1beta}")
            String geminiApiUrl,

            @Value("${gemini.api.model:gemini-3.7-flash}")
            String geminiModel,

            @Value("${gemini.api.key:}")
            String geminiApiKey
    ) {
        this.objectMapper = objectMapper;
        this.groqModel = groqModel;
        this.groqApiKey = groqApiKey;
        this.geminiModel = geminiModel;
        this.geminiApiKey = geminiApiKey;

        SimpleClientHttpRequestFactory groqFactory =
                new SimpleClientHttpRequestFactory();

        groqFactory.setConnectTimeout(15000);
        groqFactory.setReadTimeout(45000);

        RestClient.Builder groqBuilder = RestClient.builder()
                .baseUrl(groqApiUrl)
                .requestFactory(groqFactory)
                .defaultHeader("Content-Type", "application/json");

        if (groqApiKey != null && !groqApiKey.isBlank()) {
            groqBuilder.defaultHeader(
                    "Authorization",
                    "Bearer " + groqApiKey
            );
        } else {
            logger.error("GROQ_API_KEY is missing.");
        }

        this.groqClient = groqBuilder.build();

        SimpleClientHttpRequestFactory geminiFactory =
                new SimpleClientHttpRequestFactory();

        geminiFactory.setConnectTimeout(10000);
        geminiFactory.setReadTimeout(25000);

        RestClient.Builder geminiBuilder = RestClient.builder()
                .baseUrl(geminiApiUrl)
                .requestFactory(geminiFactory)
                .defaultHeader("Content-Type", "application/json");

        if (geminiApiKey != null && !geminiApiKey.isBlank()) {
            geminiBuilder.defaultHeader(
                    "x-goog-api-key",
                    geminiApiKey
            );
        }

        this.geminiClient = geminiBuilder.build();
    }

    public RecommendationResponse generate(RecommendationRequest request) {

        if (request == null) {
            throw new IllegalArgumentException(
                    "Recommendation request cannot be empty."
            );
        }

        String prompt = buildPrompt(request);

        // PRIMARY PROVIDER: GROQ
        if (groqApiKey != null && !groqApiKey.isBlank()) {
            try {
                logger.info(
                        "Generating recommendation using Groq model: {}",
                        groqModel
                );

                String recommendation = callGroq(prompt);

                logger.info("Groq recommendation generated successfully.");

                return new RecommendationResponse(
                        request.userId(),
                        recommendation,
                        GROQ_PROVIDER + " - " + groqModel
                );

            } catch (Exception exception) {
                logger.warn(
                        "Groq request failed. Trying Gemini backup. Reason: {}",
                        exception.getMessage()
                );
            }
        } else {
            logger.warn("Groq API key is unavailable. Trying Gemini.");
        }

        // SECONDARY PROVIDER: GEMINI
        if (geminiApiKey != null && !geminiApiKey.isBlank()) {
            try {
                logger.info(
                        "Generating recommendation using Gemini backup: {}",
                        geminiModel
                );

                String recommendation = callGemini(prompt);

                logger.info("Gemini backup generated recommendation successfully.");

                return new RecommendationResponse(
                        request.userId(),
                        recommendation,
                        GEMINI_PROVIDER + " - " + geminiModel
                );

            } catch (Exception exception) {
                logger.error(
                        "Gemini backup also failed. Reason: {}",
                        exception.getMessage()
                );
            }
        } else {
            logger.warn("Gemini API key is unavailable.");
        }

        // FINAL PROVIDER: LOCAL FALLBACK
        logger.warn("Both AI providers failed. Using local fallback.");

        return new RecommendationResponse(
                request.userId(),
                buildLocalRecommendations(request),
                "Local Fallback - AI Providers Unavailable"
        );
    }

    private String callGroq(String prompt) throws IOException {

        Map<String, Object> requestBody = Map.of(
                "model", groqModel,
                "messages", List.of(
                        Map.of(
                                "role", "system",
                                "content",
                                "You are an expert, safe and practical AI fitness coach."
                        ),
                        Map.of(
                                "role", "user",
                                "content", prompt
                        )
                ),
                "max_completion_tokens", 1800
        );

        String responseBody = groqClient.post()
                .uri("/chat/completions")
                .body(requestBody)
                .retrieve()
                .onStatus(HttpStatusCode::isError, (request, response) -> {
                    String errorBody;

                    try {
                        errorBody = new String(
                                response.getBody().readAllBytes(),
                                StandardCharsets.UTF_8
                        );
                    } catch (IOException exception) {
                        errorBody = "Unable to read Groq error response.";
                    }

                    throw new ProviderApiException(
                            GROQ_PROVIDER,
                            response.getStatusCode().value(),
                            errorBody
                    );
                })
                .body(String.class);

        if (responseBody == null || responseBody.isBlank()) {
            throw new IllegalStateException("Groq returned an empty response.");
        }

        JsonNode response = objectMapper.readTree(responseBody);

        JsonNode textNode = response
                .path("choices")
                .path(0)
                .path("message")
                .path("content");

        if (textNode.isMissingNode() || textNode.isNull()
                || textNode.asString().isBlank()) {
            throw new IllegalStateException(
                    "Groq did not return recommendation text."
            );
        }

        return textNode.asString();
    }

    private String callGemini(String prompt) throws IOException {

        Map<String, Object> requestBody = Map.of(
                "contents", List.of(
                        Map.of(
                                "parts", List.of(
                                        Map.of("text", prompt)
                                )
                        )
                ),
                "generationConfig", Map.of(
                        "temperature", 0.7,
                        "maxOutputTokens", 1800
                )
        );

        String responseBody = geminiClient.post()
                .uri("/models/{model}:generateContent", geminiModel)
                .body(requestBody)
                .retrieve()
                .onStatus(HttpStatusCode::isError, (request, response) -> {
                    String errorBody;

                    try {
                        errorBody = new String(
                                response.getBody().readAllBytes(),
                                StandardCharsets.UTF_8
                        );
                    } catch (IOException exception) {
                        errorBody = "Unable to read Gemini error response.";
                    }

                    throw new ProviderApiException(
                            GEMINI_PROVIDER,
                            response.getStatusCode().value(),
                            errorBody
                    );
                })
                .body(String.class);

        if (responseBody == null || responseBody.isBlank()) {
            throw new IllegalStateException("Gemini returned an empty response.");
        }

        JsonNode response = objectMapper.readTree(responseBody);

        JsonNode textNode = response
                .path("candidates")
                .path(0)
                .path("content")
                .path("parts")
                .path(0)
                .path("text");

        if (textNode.isMissingNode() || textNode.isNull()
                || textNode.asString().isBlank()) {
            throw new IllegalStateException(
                    "Gemini did not return recommendation text."
            );
        }

        return textNode.asString();
    }

    private String buildPrompt(RecommendationRequest request) {

        return """
                You are an expert AI fitness coach.

                Generate personalized, safe and practical fitness recommendations.

                User details:
                - Age: %s
                - Gender: %s
                - Height: %s cm
                - Weight: %s kg
                - Fitness Goal: %s
                - Daily Calories Target: %s
                - Weekly Workout Target: %s
                - Recent Activities: %s

                Include:
                1. Fitness assessment based on the provided profile.
                2. Workout recommendations.
                3. Nutrition guidance.
                4. Hydration, sleep and recovery.
                5. A practical seven-day fitness plan.

                Use clear headings and bullet points.
                Give realistic recommendations.
                Avoid medical diagnoses or unsafe extreme diets.
                Remind users to adjust exercise intensity to their abilities.
                """.formatted(
                valueOrUnknown(request.age()),
                valueOrUnknown(request.gender()),
                valueOrUnknown(request.height()),
                valueOrUnknown(request.weight()),
                valueOrUnknown(request.fitnessGoal()),
                valueOrUnknown(request.dailyCaloriesTarget()),
                valueOrUnknown(request.weeklyWorkoutTarget()),
                valueOrUnknown(request.activities())
        );
    }

    private String buildLocalRecommendations(RecommendationRequest request) {

        return """
                YOUR PERSONALIZED FITNESS RECOMMENDATIONS

                Note: Both external AI providers are temporarily unavailable.
                These recommendations were generated by the platform's local fallback system.

                YOUR FITNESS PROFILE
                - Age: %s
                - Gender: %s
                - Height: %s cm
                - Weight: %s kg
                - Fitness Goal: %s

                1. WORKOUT PLAN

                - Monday: Full-body strength training.
                - Tuesday: 25-30 minutes of walking or light cardio.
                - Wednesday: Upper-body strength training.
                - Thursday: Active recovery and stretching.
                - Friday: Lower-body strength training.
                - Saturday: Cardio and core exercises.
                - Sunday: Rest and recovery.

                Adjust exercise intensity to your current fitness level.

                2. NUTRITION

                - Include protein-rich foods in your meals.
                - Eat vegetables, fruits and whole grains.
                - Maintain balanced portion sizes.
                - Avoid excessive processed food.
                - Follow your prescribed calorie target if appropriate.

                3. HYDRATION

                - Drink water regularly throughout the day.
                - Increase fluid intake when exercising or sweating.
                - Avoid waiting until you feel very thirsty.

                4. RECOVERY

                - Aim for 7-9 hours of sleep.
                - Allow muscles time to recover.
                - Include stretching and light movement.
                - Avoid training through pain.

                5. WEEKLY GOAL

                - Follow a consistent workout schedule.
                - Track your activities.
                - Monitor your progress weekly.
                - Increase exercise intensity gradually.

                Remember: This is general fitness guidance, not medical advice.
                """.formatted(
                valueOrUnknown(request.age()),
                valueOrUnknown(request.gender()),
                valueOrUnknown(request.height()),
                valueOrUnknown(request.weight()),
                valueOrUnknown(request.fitnessGoal())
        );
    }

    private String valueOrUnknown(Object value) {
        return value == null ? "Not provided" : value.toString();
    }

    private static class ProviderApiException extends RuntimeException {

        public ProviderApiException(
                String provider,
                int statusCode,
                String message
        ) {
            super(provider + " API returned HTTP "
                    + statusCode + ": " + message);
        }
    }
}
