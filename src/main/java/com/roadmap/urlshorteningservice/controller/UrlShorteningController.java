package com.roadmap.urlshorteningservice.controller;

import com.roadmap.urlshorteningservice.model.Request;
import com.roadmap.urlshorteningservice.model.Response;
import com.roadmap.urlshorteningservice.service.UrlShorteningService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/shorten")
@RequiredArgsConstructor
public class UrlShorteningController {

    private final UrlShorteningService service;

    @PostMapping
    public ResponseEntity<Response> shorten(@Valid @RequestBody Request request) {
        Response response = service.createShortUrl(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{shortCode}")
    public ResponseEntity<Response> retrieve(@PathVariable String shortCode) {
        return ResponseEntity.ok(service.getByShortCode(shortCode));
    }
}