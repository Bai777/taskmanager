package com.baymakov.taskmanager.service.dto;

import com.baymakov.taskmanager.entity.TaskStatus;

import java.time.LocalDateTime;

public record TaskRequest(String title, String description, LocalDateTime deadline, TaskStatus status) {
}
