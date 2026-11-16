package com.aifitness.activity.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.time.LocalDateTime;

public class ActivityRequest {

    @NotNull(message = "User ID is required")
    private Long userId;

    @NotBlank(message = "Activity type is required")
    private String activityType;

    @NotNull(message = "Duration is required")
    @PositiveOrZero(message = "Duration cannot be negative")
    private Integer duration;

    @PositiveOrZero(message = "Distance cannot be negative")
    private Double distance;

    @PositiveOrZero(message = "Steps cannot be negative")
    private Integer steps;

    @PositiveOrZero(message = "Calories burned cannot be negative")
    private Double caloriesBurned;

    @NotNull(message = "Activity date is required")
    private LocalDateTime activityDate;

    public ActivityRequest() {
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getActivityType() {
        return activityType;
    }

    public void setActivityType(String activityType) {
        this.activityType = activityType;
    }

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    public Double getDistance() {
        return distance;
    }

    public void setDistance(Double distance) {
        this.distance = distance;
    }

    public Integer getSteps() {
        return steps;
    }

    public void setSteps(Integer steps) {
        this.steps = steps;
    }

    public Double getCaloriesBurned() {
        return caloriesBurned;
    }

    public void setCaloriesBurned(Double caloriesBurned) {
        this.caloriesBurned = caloriesBurned;
    }

    public LocalDateTime getActivityDate() {
        return activityDate;
    }

    public void setActivityDate(LocalDateTime activityDate) {
        this.activityDate = activityDate;
    }
}