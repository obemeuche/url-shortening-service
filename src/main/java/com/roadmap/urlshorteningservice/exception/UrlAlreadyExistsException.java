package com.roadmap.urlshorteningservice.exception;

public class UrlAlreadyExistsException extends RuntimeException {

    public UrlAlreadyExistsException(String url) {
        super("A short code already exists for: " + url);
    }
}