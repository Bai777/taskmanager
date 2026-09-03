package com.baymakov.taskmanager.service.dto;

import com.baymakov.taskmanager.entity.Task;
import com.baymakov.taskmanager.entity.TaskStatus;

import java.time.LocalDateTime;

public record TaskResponse(Long id, String title, String description, LocalDateTime deadline,
                           TaskStatus status, Long userId, String username) {
    public static TaskResponse from(Task task) {
        return new TaskResponse(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.getDeadline(),
                task.getStatus(),
                task.getUser().getId(),
                task.getUser().getUsername()
        );
    }
}
