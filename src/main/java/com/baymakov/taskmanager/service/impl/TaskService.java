package com.baymakov.taskmanager.service.impl;

import com.baymakov.taskmanager.entity.Task;
import com.baymakov.taskmanager.entity.TaskStatus;
import com.baymakov.taskmanager.entity.User;
import com.baymakov.taskmanager.repository.TaskRepository;
import com.baymakov.taskmanager.service.dto.TaskRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TaskService {
    private final TaskRepository taskRepository;
    private final UserService userService;

    public Task createTask(TaskRequest request, String username) {
        User user = userService.findByUsername(username);
        Task task = Task.builder()
                .title(request.title())
                .description(request.description())
                .deadline(request.deadline())
                .status(request.status() != null ? request.status() : TaskStatus.PENDING)
                .user(user)
                .build();
        return taskRepository.save(task);
    }

    public Task updateTask(Long taskId, TaskRequest request, String username, boolean isAdmin) {
        Task task = findTaskAndCheckAccess(taskId, username, isAdmin);
        task.setTitle(request.title());
        task.setDescription(request.description());
        task.setDeadline(request.deadline());
        task.setStatus(request.status());
        return taskRepository.save(task);
    }

    public void deleteTask(Long taskId, String username, boolean isAdmin) {
        Task task = findTaskAndCheckAccess(taskId, username, isAdmin);
        taskRepository.delete(task);
    }

    public Task getTask(Long taskId, String username, boolean isAdmin) {
        return findTaskAndCheckAccess(taskId, username, isAdmin);
    }

    public List<Task> getTasksForUser(String username, TaskStatus status, LocalDateTime from, LocalDateTime to) {
        User user = userService.findByUsername(username);
        if (status != null) {
            return taskRepository.findByUserIdAndStatus(user.getId(), status);
        } else if (from != null && to != null) {
            return taskRepository.findByUserIdAndDeadlineBetween(user.getId(), from, to);
        } else {
            return taskRepository.findByUserId(user.getId());
        }
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
