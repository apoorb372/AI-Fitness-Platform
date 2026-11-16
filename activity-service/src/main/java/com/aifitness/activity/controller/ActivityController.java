package com.aifitness.activity.controller;

import com.aifitness.activity.dto.ActivityRequest;
import com.aifitness.activity.dto.ActivityResponse;
import com.aifitness.activity.service.ActivityService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/activities")
public class ActivityController {

    private final ActivityService activityService;

    public ActivityController(ActivityService activityService) {
        this.activityService = activityService;
    }

    @PostMapping
    public ResponseEntity<ActivityResponse> createActivity(
            @Valid @RequestBody ActivityRequest request) {

        ActivityResponse response = activityService.createActivity(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping
    public ResponseEntity<List<ActivityResponse>> getAllActivities() {

        return ResponseEntity.ok(activityService.getAllActivities());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ActivityResponse> getActivityById(
            @PathVariable String id) {

        return ResponseEntity.ok(activityService.getActivityById(id));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<ActivityResponse>> getActivitiesByUserId(
            @PathVariable Long userId) {

        return ResponseEntity.ok(
                activityService.getActivitiesByUserId(userId)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteActivity(
            @PathVariable String id) {

        activityService.deleteActivity(id);

        return ResponseEntity.noContent().build();
    }
}