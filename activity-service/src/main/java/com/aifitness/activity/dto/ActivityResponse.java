package com.aifitness.activity.dto;

import com.aifitness.activity.entity.Activity;

import java.time.LocalDateTime;

public class ActivityResponse {

    private String id;
    private Long userId;
    private String activityType;
    private Integer duration;
    private Double distance;
    private Integer steps;
    private Double caloriesBurned;
    private LocalDateTime activityDate;
    private LocalDateTime createdAt;

    public ActivityResponse(Activity activity) {
        this.id = activity.getId();
        this.userId = activity.getUserId();
        this.activityType = activity.getActivityType();
        this.duration = activity.getDuration();
        this.distance = activity.getDistance();
        this.steps = activity.getSteps();
        this.caloriesBurned = activity.getCaloriesBurned();
        this.activityDate = activity.getActivityDate();
        this.createdAt = activity.getCreatedAt();
    }

    public String getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public String getActivityType() {
        return activityType;
    }

    public Integer getDuration() {
        return duration;
    }

    public Double getDistance() {
        return distance;
    }

    public Integer getSteps() {
        return steps;
    }

    public Double getCaloriesBurned() {
        return caloriesBurned;
    }

    public LocalDateTime getActivityDate() {
        return activityDate;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}