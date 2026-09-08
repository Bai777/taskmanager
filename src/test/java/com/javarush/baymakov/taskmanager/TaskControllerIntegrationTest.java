package com.javarush.baymakov.taskmanager;

import com.javarush.baymakov.taskmanager.entity.Role;
import com.javarush.baymakov.taskmanager.entity.User;
import com.javarush.baymakov.taskmanager.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
public class TaskControllerIntegrationTest {

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

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        User user = User.builder()
                .username("user")
                .email("user@test.com")
                .password(passwordEncoder.encode("password"))
                .role(Role.USER)
                .build();
        userRepository.save(user);
        userRepository.flush();

        if (userRepository.findByUsername("user").isEmpty()) {
            throw new RuntimeException("Test user not saved!");
        }
    }

    @Test
    @WithMockUser(username = "user", roles = "USER")
    void shouldCreateTask() throws Exception {
        String json = "{\"title\":\"Test\",\"deadline\":\"2027-01-01T00:00:00\"}";
        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Test"));
    }

    @Test
    @WithMockUser(username = "user", roles = "USER")
    void shouldUpdateTask() throws Exception {
        String createJson = "{\"title\":\"Original\",\"deadline\":\"2027-01-01T00:00:00\"}";
        String response = mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createJson))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        Long taskId = new com.fasterxml.jackson.databind.ObjectMapper()
                .readTree(response).get("id").asLong();

        String updateJson = "{\"title\":\"Updated\",\"deadline\":\"2027-02-01T00:00:00\",\"status\":\"IN_PROGRESS\"}";
        mockMvc.perform(put("/api/tasks/" + taskId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated"))
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));
    }

    @Test
    @WithMockUser(username = "user", roles = "USER")
    void shouldSoftDeleteTask() throws Exception {
        String createJson = "{\"title\":\"ToDelete\",\"deadline\":\"2027-01-01T00:00:00\"}";
        String response = mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createJson))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        Long taskId = new com.fasterxml.jackson.databind.ObjectMapper()
                .readTree(response).get("id").asLong();

        mockMvc.perform(delete("/api/tasks/" + taskId))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/tasks/" + taskId))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "user", roles = "USER")
    void shouldGetTaskById() throws Exception {
        String createJson = "{\"title\":\"GetMe\",\"deadline\":\"2027-01-01T00:00:00\"}";
        String response = mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createJson))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        Long taskId = new com.fasterxml.jackson.databind.ObjectMapper()
                .readTree(response).get("id").asLong();

        mockMvc.perform(get("/api/tasks/" + taskId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("GetMe"));
    }

    @Test
    @WithMockUser(username = "user", roles = "USER")
    void shouldFilterTasksByStatusAndDates() throws Exception {
        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Pending1\",\"deadline\":\"2027-01-01T00:00:00\",\"status\":\"PENDING\"}"))
                .andExpect(status().isCreated());
        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Completed1\",\"deadline\":\"2027-02-01T00:00:00\",\"status\":\"COMPLETED\"}"))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/tasks?status=PENDING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("PENDING"));

        mockMvc.perform(get("/api/tasks?from=2027-01-01T00:00:00&to=2027-01-31T23:59:59"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Pending1"));
    }
}