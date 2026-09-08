package com.javarush.baymakov.taskmanager.service.impl;

import com.javarush.baymakov.taskmanager.entity.Task;
import com.javarush.baymakov.taskmanager.entity.TaskStatus;
import com.javarush.baymakov.taskmanager.entity.User;
import com.javarush.baymakov.taskmanager.exception.TaskNotFoundException;
import com.javarush.baymakov.taskmanager.monitor.TaskMetrics;
import com.javarush.baymakov.taskmanager.repository.TaskRepository;
import com.javarush.baymakov.taskmanager.service.dto.TaskRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class TaskService {
    private final TaskRepository taskRepository;
    private final UserService userService;
    private final TaskMetrics taskMetrics;

    public Task createTask(TaskRequest request, String username) {
        User user = userService.findByUsername(username);
        Task task = Task.builder()
                .title(request.title())
                .description(request.description())
                .deadline(request.deadline())
                .status(request.status() != null ? request.status() : TaskStatus.PENDING)
                .user(user)
                .deleted(false)
                .build();
        Task saved = taskRepository.save(task);
        taskMetrics.incrementTaskCreated();
        log.info("Task created with id: {} for user: {}", saved.getId(), username);
        return saved;
    }

    public Task updateTask(Long taskId, TaskRequest request, String username, boolean isAdmin) {
        Task task = findTaskAndCheckAccess(taskId, username, isAdmin);
        task.setTitle(request.title());
        task.setDescription(request.description());
        task.setDeadline(request.deadline());
        task.setStatus(request.status());
        log.info("Task updated: {}", taskId);
        return taskRepository.save(task);
    }

    public void deleteTask(Long taskId, String username, boolean isAdmin) {
        Task task = findTaskAndCheckAccess(taskId, username, isAdmin);
        task.setDeleted(true);
        taskRepository.save(task);
        log.info("Task soft-deleted: {}", taskId);
    }

    public void restoreTask(Long taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new TaskNotFoundException("Task not found"));
        task.setDeleted(false);
        taskRepository.save(task);
        log.info("Task restored by admin: {}", taskId);
    }

    public Task getTask(Long taskId, String username, boolean isAdmin) {
        Task task = findTaskAndCheckAccess(taskId, username, isAdmin);
        if (!isAdmin && task.isDeleted()) {
            throw new TaskNotFoundException("Task not found");
        }
        return task;
    }

    public List<Task> getTasksForUser(String username, TaskStatus status, LocalDateTime from, LocalDateTime to) {
        User user = userService.findByUsername(username);
        List<Task> tasks;
        if (status != null && from != null && to != null) {
            tasks = taskRepository.findByUserIdAndStatusAndDeletedFalse(user.getId(), status);
            tasks = tasks.stream()
                    .filter(t -> t.getDeadline() != null && !t.getDeadline().isBefore(from) && !t.getDeadline().isAfter(to))
                    .toList();
        } else if (status != null) {
            tasks = taskRepository.findByUserIdAndStatusAndDeletedFalse(user.getId(), status);
        } else if (from != null && to != null) {
            tasks = taskRepository.findByUserIdAndDeadlineBetweenAndDeletedFalse(user.getId(), from, to);
        } else {
            tasks = taskRepository.findByUserIdAndDeletedFalse(user.getId());
        }
        log.debug("Retrieved {} tasks for user {}", tasks.size(), username);
        return tasks;
    }

    private Task findTaskAndCheckAccess(Long taskId, String username, boolean isAdmin) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new TaskNotFoundException("Task not found"));
        if (!isAdmin && !task.getUser().getUsername().equals(username)) {
            throw new AccessDeniedException("You do not have permission to access this task");
        }
        return task;
    }
}
