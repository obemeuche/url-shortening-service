package com.roadmap.urlshorteningservice.model;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Getter
@SuperBuilder
@JsonPropertyOrder({"id", "url", "shortCode", "createdAt", "updatedAt"})
@Schema(description = "Shortened URL mapping")
public class Response {

    @Schema(description = "Unique identifier of the URL mapping", example = "1")
    private String id;

    @Schema(description = "The original URL", example = "https://www.example.com/very/long/path")
    private String url;

    @Schema(description = "The generated short code", example = "abc123")
    private String shortCode;

    @Schema(description = "Timestamp when the short URL was created", example = "2026-01-01T12:00:00")
    private LocalDateTime createdAt;

    @Schema(description = "Timestamp when the short URL was last updated", example = "2026-01-01T12:00:00")
    private LocalDateTime updatedAt;
}