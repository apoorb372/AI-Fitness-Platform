package com.aifitness.activity.service;

import com.aifitness.activity.dto.ActivityRequest;
import com.aifitness.activity.dto.ActivityResponse;
import com.aifitness.activity.entity.Activity;
import com.aifitness.activity.repository.ActivityRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ActivityService {

    private final ActivityRepository activityRepository;

    public ActivityService(ActivityRepository activityRepository) {
        this.activityRepository = activityRepository;
    }

    public ActivityResponse createActivity(ActivityRequest request) {

        Activity activity = new Activity();

        activity.setUserId(request.getUserId());
        activity.setActivityType(request.getActivityType());
        activity.setDuration(request.getDuration());
        activity.setDistance(request.getDistance());
        activity.setSteps(request.getSteps());
        activity.setCaloriesBurned(request.getCaloriesBurned());
        activity.setActivityDate(request.getActivityDate());
        activity.setCreatedAt(LocalDateTime.now());

        Activity savedActivity = activityRepository.save(activity);

        return new ActivityResponse(savedActivity);
    }

    public List<ActivityResponse> getAllActivities() {

        return activityRepository.findAll()
                .stream()
                .map(ActivityResponse::new)
                .toList();
    }

    public ActivityResponse getActivityById(String id) {

        Activity activity = activityRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Activity not found with id: " + id
                        ));

        return new ActivityResponse(activity);
    }

    public List<ActivityResponse> getActivitiesByUserId(Long userId) {

        return activityRepository.findByUserId(userId)
                .stream()
                .map(ActivityResponse::new)
                .toList();
    }

    public void deleteActivity(String id) {

        if (!activityRepository.existsById(id)) {
            throw new IllegalArgumentException(
                    "Activity not found with id: " + id
            );
        }

        activityRepository.deleteById(id);
    }
}