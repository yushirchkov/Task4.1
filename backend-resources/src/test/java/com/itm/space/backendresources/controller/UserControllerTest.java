package com.itm.space.backendresources.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.itm.space.backendresources.BaseIntegrationTest;
import com.itm.space.backendresources.api.request.UserRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc
class UserControllerTest extends BaseIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    private static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:13.5");

    @DynamicPropertySource
    static void postgresProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Test
    @WithMockUser(roles = "MODERATOR")
    public void createUserTest() throws Exception {
        // Given
        UserRequest userRequest = new UserRequest("UserFan", "cat.doe@example.com", "test",
                "Fan", "Cattin");

        // When
        mockMvc.perform(MockMvcRequestBuilders.post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(userRequest)))
                // Then
                .andExpect(status().isOk());
    }
    @Test
    @WithMockUser(roles = "MODERATOR")
    public void createUserTest_InvalidEmail() throws Exception {
        // Given
        UserRequest userRequest = new UserRequest("UserFan", "invalid-email", "test", "Fan", "Cattin");

        // When
        mockMvc.perform(MockMvcRequestBuilders.post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(userRequest)))

                // Then
                .andExpect(status().isBadRequest());
    }


    @Test
    @WithMockUser(roles = "MODERATOR")
    void getUserById() throws Exception {
        // Given
        var userID = "968d26bc-f9bd-4322-93d5-921d02de6e75";

        // when
        mockMvc.perform(MockMvcRequestBuilders.get("/api/users/{id}", userID))
                // then
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.jsonPath("$.firstName").value(""))
                .andExpect(MockMvcResultMatchers.jsonPath("$.lastName").value(""))
                .andExpect(MockMvcResultMatchers.jsonPath("$.email").value(""))
                .andExpect(MockMvcResultMatchers.jsonPath("$.roles").value("default-roles-itm"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.groups").value("Moderators"));
    }
    @Test
    @WithMockUser(username = "testUser", roles = "MODERATOR")
    void helloIntegrationTest() throws Exception {
        // Given
        String expectedUsername = "testUser";
        // When
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/api/users/hello")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        // Then
        String responseContent = result.getResponse().getContentAsString();
        assertEquals(expectedUsername, responseContent);
    }

    private static String asJsonString(Object obj) {
        try {
            return new ObjectMapper().writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    @Test
    @WithMockUser(roles = "MODERATOR")
    void getUserById_NonExistingUser() throws Exception {

        var nonExistingUserID = "non-existing-user-id";

        mockMvc.perform(MockMvcRequestBuilders.get("/api/users/{id}", nonExistingUserID))

                .andExpect(status().isNotFound());

}}