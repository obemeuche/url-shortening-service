package com.roadmap.urlshorteningservice.model;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import org.hibernate.validator.constraints.URL;

@Getter
public class Request {

    @NotBlank(message = "url must not be blank")
    @URL(message = "url must be a valid URL")
    private String url;
}