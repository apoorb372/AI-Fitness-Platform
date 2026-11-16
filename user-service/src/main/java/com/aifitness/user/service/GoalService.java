
package com.aifitness.user.service;

import com.aifitness.user.dto.GoalRequest;
import com.aifitness.user.dto.GoalResponse;
import com.aifitness.user.entity.Goal;
import com.aifitness.user.repository.GoalRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GoalService {

    private final GoalRepository goalRepository;

    public GoalService(GoalRepository goalRepository) {
        this.goalRepository = goalRepository;
    }

    @Transactional
    public GoalResponse saveGoal(Long userId, GoalRequest request) {
        Goal goal = goalRepository.findByUserId(userId)
                .orElseGet(Goal::new);

        goal.setUserId(userId);
        goal.setDailyCaloriesTarget(request.getDailyCaloriesTarget());
        goal.setWeeklyWorkoutTarget(request.getWeeklyWorkoutTarget());

        Goal savedGoal = goalRepository.save(goal);
        return new GoalResponse(savedGoal);
    }

    @Transactional(readOnly = true)
    public GoalResponse getGoal(Long userId) {
        return goalRepository.findByUserId(userId)
                .map(GoalResponse::new)
                .orElse(null);
    }
}