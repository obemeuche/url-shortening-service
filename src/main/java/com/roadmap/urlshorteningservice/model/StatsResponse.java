package com.roadmap.urlshorteningservice.model;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@JsonPropertyOrder({"id", "url", "shortCode", "createdAt", "updatedAt", "accessCount"})
@Schema(description = "Shortened URL mapping including access statistics")
public class StatsResponse extends Response {

    @Schema(description = "Number of times the short URL has been accessed", example = "42")
    private Long accessCount;
}
