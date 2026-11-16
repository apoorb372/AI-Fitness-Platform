
package com.aifitness.user.controller;

import com.aifitness.user.dto.GoalRequest;
import com.aifitness.user.dto.GoalResponse;
import com.aifitness.user.service.GoalService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/goals")
public class GoalController {

    private final GoalService goalService;

    public GoalController(GoalService goalService) {
        this.goalService = goalService;
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<GoalResponse> getGoal(
            @PathVariable Long userId) {

        GoalResponse goal = goalService.getGoal(userId);

        if (goal == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(goal);
    }

    @PutMapping("/user/{userId}")
    public ResponseEntity<GoalResponse> saveGoal(
            @PathVariable Long userId,
            @Valid @RequestBody GoalRequest request) {

        return ResponseEntity.ok(
                goalService.saveGoal(userId, request)
        );
    }
}