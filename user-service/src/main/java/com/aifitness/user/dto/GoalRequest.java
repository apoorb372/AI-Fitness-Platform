
package com.aifitness.user.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class GoalRequest {

    @NotNull(message = "Daily calorie target is required")
    @Min(value = 100, message = "Daily calorie target must be at least 100")
    @Max(value = 10000, message = "Daily calorie target cannot exceed 10000")
    private Integer dailyCaloriesTarget;

    @NotNull(message = "Weekly workout target is required")
    @Min(value = 1, message = "Weekly workout target must be at least 1")
    @Max(value = 21, message = "Weekly workout target cannot exceed 21")
    private Integer weeklyWorkoutTarget;

    public Integer getDailyCaloriesTarget() {
        return dailyCaloriesTarget;
    }

    public void setDailyCaloriesTarget(Integer dailyCaloriesTarget) {
        this.dailyCaloriesTarget = dailyCaloriesTarget;
    }

    public Integer getWeeklyWorkoutTarget() {
        return weeklyWorkoutTarget;
    }

    public void setWeeklyWorkoutTarget(Integer weeklyWorkoutTarget) {
        this.weeklyWorkoutTarget = weeklyWorkoutTarget;
    }
}