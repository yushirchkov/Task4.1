package com.itm.space.backendresources.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;

import javax.ws.rs.core.MediaType;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class UserServiceImplTest {
    @Autowired
    private MockMvc mockMvc;
    @Value("${keycloak.realm}")
    private String realm;

    @Container
    public static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:latest")
            .withReuse(true);

    @Container
    public static GenericContainer<?> keycloak = new GenericContainer<>("jboss/keycloak:latest")
            .withExposedPorts(8080)
            .withEnv("KEYCLOAK_USER", "admin")
            .withEnv("KEYCLOAK_PASSWORD", "admin")
            .dependsOn(postgres);

    @Test
    @WithMockUser(roles = "MODERATOR")
    void testCreateUser() throws Exception {
        // Given
        String userRequestPayload = "{\"username\": \"testUser\", \"email\": \"test.uik@mail.com\", " +
                "\"password\": \"test\", \"firstName\": \"John\", \"lastName\": \"Uik\"}";
        // When
        mockMvc.perform(MockMvcRequestBuilders.post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userRequestPayload)
                        .header("realm", realm))
                // Then
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "MODERATOR")
    void getUserById() throws Exception {
        // Given
        var userId = "968d26bc-f9bd-4322-93d5-921d02de6e75";
        // When
        mockMvc.perform(MockMvcRequestBuilders.get("/api/users/{id}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("realm", realm))
                // Then
                .andExpect(MockMvcResultMatchers.status().isOk());
    }
}