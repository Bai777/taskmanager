package com.javarush.baymakov.taskmanager.controller;

import com.javarush.baymakov.taskmanager.entity.Task;
import com.javarush.baymakov.taskmanager.entity.TaskStatus;
import com.javarush.baymakov.taskmanager.service.dto.TaskResponse;
import com.javarush.baymakov.taskmanager.service.impl.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Tag(name = "Admin Tasks", description = "Администрирование задач (только ADMIN)")
@RestController
@RequestMapping("/api/admin/tasks")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
@Slf4j
public class AdminTaskController {

    private final TaskService taskService;

    @Operation(summary = "Получить все задачи всех пользователей (включая удалённые)")
    @GetMapping("/all")
    public List<TaskResponse> getAllTasks() {
        log.info("Admin requested all tasks (including deleted)");
        List<Task> tasks = taskService.getAllTasksForAdmin();
        return tasks.stream().map(TaskResponse::from).collect(Collectors.toList());
    }

    @Operation(summary = "Получить все задачи конкретного пользователя (включая удалённые)")
    @GetMapping("/user/{userId}")
    public List<TaskResponse> getAllTasksForUser(@PathVariable Long userId) {
        log.info("Admin requested all tasks for user id: {} (including deleted)", userId);
        List<Task> tasks = taskService.getAllTasksForUserByAdmin(userId);
        return tasks.stream().map(TaskResponse::from).collect(Collectors.toList());
    }

    @Operation(summary = "Получить задачи пользователя по статусу (включая удалённые)")
    @GetMapping("/user/{userId}/status")
    public List<TaskResponse> getTasksForUserByStatus(@PathVariable Long userId,
                                                      @RequestParam TaskStatus status) {
        log.info("Admin requested tasks for user {} with status {} (including deleted)", userId, status);
        List<Task> tasks = taskService.getTasksByUserIdAndStatusAdmin(userId, status);
        return tasks.stream().map(TaskResponse::from).collect(Collectors.toList());
    }

    @Operation(summary = "Получить задачи пользователя по диапазону дат (включая удалённые)")
    @GetMapping("/user/{userId}/deadline")
    public List<TaskResponse> getTasksForUserByDeadline(@PathVariable Long userId,
                                                        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
                                                        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
        log.info("Admin requested tasks for user {} between {} and {} (including deleted)", userId, from, to);
        List<Task> tasks = taskService.getTasksByUserIdAndDeadlineBetweenAdmin(userId, from, to);
        return tasks.stream().map(TaskResponse::from).collect(Collectors.toList());
    }
}
