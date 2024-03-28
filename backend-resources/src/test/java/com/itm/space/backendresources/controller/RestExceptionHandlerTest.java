package com.itm.space.backendresources.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.itm.space.backendresources.api.request.UserRequest;
import com.itm.space.backendresources.exception.BackendResourcesException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;


import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class RestExceptionHandlerTest {
    @Autowired
    private MockMvc mockMvc;

    @Test
    void handleException() throws Exception {
        String errorMessage = "This is a test error message";
        HttpStatus expectedStatus = HttpStatus.NOT_FOUND;
        BackendResourcesException exception = new BackendResourcesException(errorMessage, expectedStatus);

        mockMvc.perform(MockMvcRequestBuilders.get("/test")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(exception.getMessage()))
                .andExpect(MockMvcResultMatchers.status().is(expectedStatus.value()));
        assertEquals(exception.getMessage(), errorMessage);
    }

    @Test
    @WithMockUser(roles = "MODERATOR")
    void handleInvalidArgument() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        UserRequest userRequest = new UserRequest("a", "not@mal.com", "12345", "Kis", "Kas");

        Map<String, String> expectedResponse = new HashMap<>();
        expectedResponse.put("username", "Username should be between 2 and 30 characters long");

        String actualResponse = mockMvc.perform(MockMvcRequestBuilders.post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(userRequest)))
                .andExpect(status().isBadRequest())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Map<String, String> actualResponseMap = objectMapper.readValue(actualResponse,
                new TypeReference<HashMap<String, String>>() {});

        assertEquals(actualResponseMap, expectedResponse);
    }

    private static String asJsonString(Object obj) {
        try {
            return new ObjectMapper().writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}