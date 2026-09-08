package com.javarush.baymakov.taskmanager.controller;

import com.javarush.baymakov.taskmanager.entity.User;
import com.javarush.baymakov.taskmanager.security.JwtService;
import com.javarush.baymakov.taskmanager.service.dto.AuthRequest;
import com.javarush.baymakov.taskmanager.service.dto.JwtResponse;
import com.javarush.baymakov.taskmanager.service.dto.RegisterRequest;
import com.javarush.baymakov.taskmanager.service.dto.UserResponse;
import com.javarush.baymakov.taskmanager.service.impl.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final UserService userService;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        User user = userService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(UserResponse.from(user));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody AuthRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password())
        );
        String token = jwtService.generateToken((UserDetails) authentication.getPrincipal());
        return ResponseEntity.ok(new JwtResponse(token));
    }
}
