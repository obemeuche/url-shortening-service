package com.roadmap.urlshorteningservice.model;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Getter
@SuperBuilder
@JsonPropertyOrder({"id", "url", "shortCode", "createdAt", "updatedAt"})
public class Response {
    private String id;
    private String url;
    private String shortCode;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}