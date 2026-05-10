package com.roadmap.urlshorteningservice.model;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class Response {
    private String id;
    private String url;
    private String shortCode;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}