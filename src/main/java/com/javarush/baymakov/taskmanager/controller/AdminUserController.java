package com.javarush.baymakov.taskmanager.controller;

import com.javarush.baymakov.taskmanager.service.dto.UserResponse;
import com.javarush.baymakov.taskmanager.service.impl.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Tag(name = "Admin Users", description = "Управление пользователями (только ADMIN)")
@RestController
@RequestMapping("/api/admin/users")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
@Slf4j
public class AdminUserController {
    private final UserService userService;

    @Operation(summary = "Получить список всех пользователей")
    @GetMapping
    public List<UserResponse> getAllUsers() {
        log.info("Admin requested all users");
        return userService.findAll().stream().map(UserResponse::from).collect(Collectors.toList());
    }

    @Operation(summary = "Удалить пользователя по ID")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable Long id) {
        log.info("Admin deleting user id: {}", id);
        userService.deleteUser(id);
    }
}
