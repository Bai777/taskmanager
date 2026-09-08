package com.javarush.baymakov.taskmanager.service.dto;

import com.javarush.baymakov.taskmanager.entity.Role;

public record RegisterRequest(String username, String email, String password, Role role) {
}
