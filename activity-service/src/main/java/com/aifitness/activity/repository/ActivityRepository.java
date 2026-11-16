package com.aifitness.activity.repository;

import com.aifitness.activity.entity.Activity;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ActivityRepository extends MongoRepository<Activity, String> {

    List<Activity> findByUserId(Long userId);

}