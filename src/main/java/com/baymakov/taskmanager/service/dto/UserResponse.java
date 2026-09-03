package com.baymakov.taskmanager.service.dto;

import com.baymakov.taskmanager.entity.Role;
import com.baymakov.taskmanager.entity.User;

public record UserResponse(Long id, String username, String email, Role role) {
    public static UserResponse from(User user) {
        return new UserResponse(user.getId(), user.getUsername(), user.getEmail(), user.getRole());
    }
}
