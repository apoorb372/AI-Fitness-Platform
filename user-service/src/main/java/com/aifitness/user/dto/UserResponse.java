package com.aifitness.user.dto;

import com.aifitness.user.entity.User;

import java.time.LocalDateTime;

public class UserResponse {

    private Long id;
    private String name;
    private String email;
    private String gender;
    private Integer age;
    private Double height;
    private Double weight;
    private LocalDateTime createdAt;

    public UserResponse(User user) {
        this.id = user.getId();
        this.name = user.getName();
        this.email = user.getEmail();
        this.gender = user.getGender();
        this.age = user.getAge();
        this.height = user.getHeight();
        this.weight = user.getWeight();
        this.createdAt = user.getCreatedAt();
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getGender() {
        return gender;
    }

    public Integer getAge() {
        return age;
    }

    public Double getHeight() {
        return height;
    }

    public Double getWeight() {
        return weight;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}