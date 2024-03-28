package com.itm.space.backendresources.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;



@SpringBootTest
@Testcontainers
@AutoConfigureMockMvc
class UserControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Container
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
        UserRequest userRequest = new UserRequest("CatUser", "cat.doe@example.com", "test",
                "Cat", "Cattin");
        // When
        mockMvc.perform(MockMvcRequestBuilders.post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(userRequest)))
                // Then
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "MODERATOR")
    void getUserById() throws Exception {
        // Given
        var userID = "968d26bc-f9bd-4322-93d5-921d02de6e75";

        // when
        mockMvc.perform(MockMvcRequestBuilders.get("/api/users/{id}", userID))
                // then
                .andExpectAll(
                        status().isOk(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        content().json("""
                                    {
                                       "firstName": "",
                                       "lastName": "",
                                       "email": "kit@mail.com",
                                       "roles": [
                                         "default-roles-itm"
                                       ],
                                       "groups": [
                                         "Moderators"
                                       ]
                                     }
                                """)
                );
    }

    @Test
    @WithMockUser(username = "testUser", roles = "MODERATOR")
    void helloIntegrationTest() throws Exception {
        // Given
        String expectedUsername = "testUser";
        // When
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/api/users/hello")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
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

}