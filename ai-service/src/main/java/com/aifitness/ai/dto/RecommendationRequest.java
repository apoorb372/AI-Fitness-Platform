
package com.aifitness.ai.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record RecommendationRequest(
        @NotNull
        @Min(1)
        Long userId,

        @NotNull
        @Min(13)
        @Max(100)
        Integer age,

        @NotBlank
        String gender,

        @NotNull
        @Min(50)
        @Max(250)
        Double height,

        @NotNull
        @Min(20)
        @Max(350)
        Double weight,

        @NotBlank
        String fitnessGoal,

        @NotNull
        @Min(100)
        @Max(10000)
        Integer dailyCaloriesTarget,

        @NotNull
        @Min(1)
        @Max(21)
        Integer weeklyWorkoutTarget,

        List<ActivitySummary> activities
) {
    public record ActivitySummary(
            String activityType,
            Integer duration,
            Double distance,
            Integer steps,
            Double caloriesBurned,
            String activityDate
    ) {
    }
}