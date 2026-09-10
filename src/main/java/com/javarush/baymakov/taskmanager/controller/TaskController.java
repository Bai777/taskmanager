package com.javarush.baymakov.taskmanager.controller;

import com.javarush.baymakov.taskmanager.entity.Task;
import com.javarush.baymakov.taskmanager.entity.TaskStatus;
import com.javarush.baymakov.taskmanager.service.dto.TaskRequest;
import com.javarush.baymakov.taskmanager.service.dto.TaskResponse;
import com.javarush.baymakov.taskmanager.service.impl.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static com.javarush.baymakov.taskmanager.util.SecurityUtils.isAdmin;

@Tag(name = "Tasks", description = "Управление задачами")
@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
@Slf4j
public class TaskController {
    private final TaskService taskService;

    @Operation(summary = "Получить список задач с фильтрацией")
    @GetMapping
    public List<TaskResponse> getTasks(
            @RequestParam(required = false) TaskStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            Authentication authentication) {
        String username = authentication.getName();
        log.info("Get tasks for user: {}, status: {}, from: {}, to: {}", username, status, from, to);
        List<Task> tasks = taskService.getTasksForUser(username, status, from, to);
        return tasks.stream().map(TaskResponse::from).collect(Collectors.toList());
    }

    @Operation(summary = "Получить задачу по ID")
    @GetMapping("/{id}")
    public TaskResponse getTask(@PathVariable Long id, Authentication authentication) {
        String username = authentication.getName();
        log.info("Get task by id: {} for user: {}", id, username);
        Task task = taskService.getTask(id, username, isAdmin(authentication));
        return TaskResponse.from(task);
    }

    @Operation(summary = "Создать новую задачу")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TaskResponse createTask(@Valid @RequestBody TaskRequest request, Authentication authentication) {
        log.info("Create task for user: {}", authentication.getName());
        Task task = taskService.createTask(request, authentication.getName());
        return TaskResponse.from(task);
    }

    @Operation(summary = "Обновить задачу")
    @PutMapping("/{id}")
    public TaskResponse updateTask(@PathVariable Long id, @Valid @RequestBody TaskRequest request,
                                   Authentication authentication) {
        String username = authentication.getName();
        log.info("Update task id: {} for user: {}", id, username);
        Task task = taskService.updateTask(id, request, username, isAdmin(authentication));
        return TaskResponse.from(task);
    }

    @Operation(summary = "Мягкое удаление задачи (пометить как удалённую)")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTask(@PathVariable Long id, Authentication authentication) {
        String username = authentication.getName();
        log.info("Delete (soft) task id: {} for user: {}", id, username);
        taskService.deleteTask(id, username, isAdmin(authentication));
    }

    @Operation(summary = "Восстановить задачу (только ADMIN)")
    @PatchMapping("/{id}/restore")
    @ResponseStatus(HttpStatus.OK)
    public void restoreTask(@PathVariable Long id, Authentication authentication) {
        if (!isAdmin(authentication)) {
            throw new AccessDeniedException("Only admin can restore tasks");
        }
        log.info("Restore task id: {} by admin", id);
        taskService.restoreTask(id);
    }
}