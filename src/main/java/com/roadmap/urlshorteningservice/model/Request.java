package com.roadmap.urlshorteningservice.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import org.hibernate.validator.constraints.URL;

@Getter
@Schema(description = "Request body for creating or updating a shortened URL")
public class Request {

    @NotBlank(message = "url must not be blank")
    @URL(message = "url must be a valid URL")
    @Schema(description = "The original URL to shorten", example = "https://www.example.com/very/long/path")
    private String url;
}