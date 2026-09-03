package com.baymakov.taskmanager.service.dto;

import com.baymakov.taskmanager.entity.Role;

public record RegisterRequest(String username, String email, String password, Role role) {
}
