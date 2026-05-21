package com.roadmap.urlshorteningservice.model;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@JsonPropertyOrder({"id", "url", "shortCode", "createdAt", "updatedAt", "accessCount"})
public class StatsResponse extends Response {
    private Long accessCount;
}
