package com.javarush.baymakov.taskmanager.controller;

import com.javarush.baymakov.taskmanager.service.dto.UserResponse;
import com.javarush.baymakov.taskmanager.service.impl.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/users")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
@Slf4j
public class AdminUserController {
    private final UserService userService;

    @GetMapping
    public List<UserResponse> getAllUsers() {
        log.info("Admin requested all users");
        return userService.findAll().stream().map(UserResponse::from).collect(Collectors.toList());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable Long id) {
        log.info("Admin deleting user id: {}", id);
        userService.deleteUser(id);
    }
}
