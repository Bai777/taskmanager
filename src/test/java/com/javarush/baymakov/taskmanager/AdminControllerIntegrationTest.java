package com.javarush.baymakov.taskmanager;

import com.javarush.baymakov.taskmanager.entity.Role;
import com.javarush.baymakov.taskmanager.entity.User;
import com.javarush.baymakov.taskmanager.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class AdminControllerIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    private Long regularUserId;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        User admin = User.builder()
                .username("admin").email("admin@test.com")
                .password(passwordEncoder.encode("password"))
                .role(Role.ADMIN).build();
        User regular = User.builder()
                .username("regular").email("regular@test.com")
                .password(passwordEncoder.encode("password"))
                .role(Role.USER).build();
        userRepository.save(admin);
        regularUserId = userRepository.save(regular).getId();
    }

    private Authentication adminAuth() {
        return new UsernamePasswordAuthenticationToken(
                "admin", "password", List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
    }

    private Authentication userAuth() {
        return new UsernamePasswordAuthenticationToken(
                "regular", "password", List.of(new SimpleGrantedAuthority("ROLE_USER")));
    }

    @Test
    void shouldForbidRegularUserFromAdminEndpoint() throws Exception {
        mockMvc.perform(get("/api/admin/users").with(authentication(userAuth())))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldAllowAdminToGetAllUsers() throws Exception {
        mockMvc.perform(get("/api/admin/users").with(authentication(adminAuth())))
                .andExpect(status().isOk());
    }

    @Test
    void shouldAllowAdminToDeleteUser() throws Exception {
        mockMvc.perform(delete("/api/admin/users/" + regularUserId).with(authentication(adminAuth())))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldAllowAdminToGetAllTasks() throws Exception {
        mockMvc.perform(get("/api/admin/tasks/all").with(authentication(adminAuth())))
                .andExpect(status().isOk());
    }

    @Test
    void shouldForbidRegularUserFromRestoringTask() throws Exception {
        Authentication reg = userAuth();
        String response = mockMvc.perform(post("/api/tasks")
                        .with(authentication(reg))
                        .contentType("application/json")
                        .content("{\"title\":\"T\",\"deadline\":\"2027-01-01T00:00:00\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        Long taskId = new com.fasterxml.jackson.databind.ObjectMapper().readTree(response).get("id").asLong();

        mockMvc.perform(delete("/api/tasks/" + taskId).with(authentication(reg)))
                .andExpect(status().isNoContent());

        mockMvc.perform(patch("/api/tasks/" + taskId + "/restore").with(authentication(reg)))
                .andExpect(status().isForbidden());
    }
}