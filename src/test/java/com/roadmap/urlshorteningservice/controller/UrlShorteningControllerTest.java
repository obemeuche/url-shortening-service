package com.roadmap.urlshorteningservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.roadmap.urlshorteningservice.exception.GlobalExceptionHandler;
import com.roadmap.urlshorteningservice.exception.UrlAlreadyExistsException;
import com.roadmap.urlshorteningservice.model.Response;
import com.roadmap.urlshorteningservice.service.UrlShorteningService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class UrlShorteningControllerTest {

    @Mock
    private UrlShorteningService service;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new UrlShorteningController(service))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void shorten_validRequest_returns201WithBody() throws Exception {
        Response response = Response.builder()
                .id("1")
                .url("https://www.example.com/long/url")
                .shortCode("abc123")
                .createdAt(LocalDateTime.of(2026, 1, 1, 12, 0))
                .updatedAt(LocalDateTime.of(2026, 1, 1, 12, 0))
                .build();
        when(service.createShortUrl(any())).thenReturn(response);

        mockMvc.perform(post("/shorten")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("url", "https://www.example.com/long/url"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("1"))
                .andExpect(jsonPath("$.url").value("https://www.example.com/long/url"))
                .andExpect(jsonPath("$.shortCode").value("abc123"))
                .andExpect(jsonPath("$.createdAt").isNotEmpty())
                .andExpect(jsonPath("$.updatedAt").isNotEmpty());
    }

    @Test
    void shorten_blankUrl_returns400() throws Exception {
        mockMvc.perform(post("/shorten")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("url", ""))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0]").value("url: url must not be blank"));
    }

    @Test
    void shorten_invalidUrl_returns400() throws Exception {
        mockMvc.perform(post("/shorten")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("url", "not-a-url"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0]").value("url: url must be a valid URL"));
    }

    @Test
    void shorten_missingUrl_returns400() throws Exception {
        mockMvc.perform(post("/shorten")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0]").value("url: url must not be blank"));
    }

    @Test
    void shorten_duplicateUrl_returns409() throws Exception {
        when(service.createShortUrl(any()))
                .thenThrow(new UrlAlreadyExistsException("https://www.example.com/long/url"));

        mockMvc.perform(post("/shorten")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("url", "https://www.example.com/long/url"))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errors[0]").value("A short code already exists for: https://www.example.com/long/url"));
    }
}