package com.baymakov.taskmanager.repository;

import com.baymakov.taskmanager.entity.Task;
import com.baymakov.taskmanager.entity.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByUserId(Long userId);

    List<Task> findByUserIdAndStatus(Long userId, TaskStatus status);

    List<Task> findByUserIdAndDeadlineBetween(Long userId, LocalDateTime from, LocalDateTime to);
}
