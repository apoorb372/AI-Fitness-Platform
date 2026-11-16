
package com.aifitness.user.dto;

import com.aifitness.user.entity.Goal;

import java.time.LocalDateTime;

public class GoalResponse {

    private Long id;
    private Long userId;
    private Integer dailyCaloriesTarget;
    private Integer weeklyWorkoutTarget;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public GoalResponse(Goal goal) {
        this.id = goal.getId();
        this.userId = goal.getUserId();
        this.dailyCaloriesTarget = goal.getDailyCaloriesTarget();
        this.weeklyWorkoutTarget = goal.getWeeklyWorkoutTarget();
        this.createdAt = goal.getCreatedAt();
        this.updatedAt = goal.getUpdatedAt();
    }

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public Integer getDailyCaloriesTarget() {
        return dailyCaloriesTarget;
    }

    public Integer getWeeklyWorkoutTarget() {
        return weeklyWorkoutTarget;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}