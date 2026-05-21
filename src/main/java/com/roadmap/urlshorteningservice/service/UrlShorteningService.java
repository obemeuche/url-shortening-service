package com.roadmap.urlshorteningservice.service;

import com.roadmap.urlshorteningservice.entity.UrlMapping;
import com.roadmap.urlshorteningservice.exception.ShortUrlNotFoundException;
import com.roadmap.urlshorteningservice.exception.UrlAlreadyExistsException;
import com.roadmap.urlshorteningservice.model.Request;
import com.roadmap.urlshorteningservice.model.Response;
import com.roadmap.urlshorteningservice.model.StatsResponse;
import com.roadmap.urlshorteningservice.repository.UrlMappingRepository;
import com.roadmap.urlshorteningservice.util.ShortCodeGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UrlShorteningService {

    private final UrlMappingRepository repository;
    private final ShortCodeGenerator shortCodeGenerator;

    @Transactional
    public Response createShortUrl(Request request) {
        if (repository.existsByUrl(request.getUrl())) {
            throw new UrlAlreadyExistsException(request.getUrl());
        }

        String shortCode = generateUniqueShortCode();

        UrlMapping mapping = UrlMapping.builder()
                .url(request.getUrl())
                .shortCode(shortCode)
                .build();

        UrlMapping saved = repository.save(mapping);

        return toResponse(saved);
    }

    @Transactional
    public Response updateShortUrl(String shortCode, Request request) {
        UrlMapping mapping = repository.findByShortCode(shortCode)
                .orElseThrow(() -> new ShortUrlNotFoundException(shortCode));

        String newUrl = request.getUrl();
        if (!newUrl.equals(mapping.getUrl()) && repository.existsByUrl(newUrl)) {
            throw new UrlAlreadyExistsException(newUrl);
        }

        mapping.setUrl(newUrl);
        return toResponse(repository.save(mapping));
    }

    @Transactional
    public Response getByShortCode(String shortCode) {
        UrlMapping mapping = repository.findByShortCode(shortCode)
                .orElseThrow(() -> new ShortUrlNotFoundException(shortCode));
        mapping.setAccessCount(mapping.getAccessCount() + 1);
        return toResponse(repository.save(mapping));
    }

    @Transactional(readOnly = true)
    public StatsResponse getStats(String shortCode) {
        UrlMapping mapping = repository.findByShortCode(shortCode)
                .orElseThrow(() -> new ShortUrlNotFoundException(shortCode));
        return toStatsResponse(mapping);
    }

    @Transactional
    public void deleteByShortCode(String shortCode) {
        UrlMapping mapping = repository.findByShortCode(shortCode)
                .orElseThrow(() -> new ShortUrlNotFoundException(shortCode));
        repository.delete(mapping);
    }

    private String generateUniqueShortCode() {
        String code;
        do {
            code = shortCodeGenerator.generate();
        } while (repository.existsByShortCode(code));
        return code;
    }

    private Response toResponse(UrlMapping mapping) {
        return Response.builder()
                .id(String.valueOf(mapping.getId()))
                .url(mapping.getUrl())
                .shortCode(mapping.getShortCode())
                .createdAt(mapping.getCreatedAt())
                .updatedAt(mapping.getUpdatedAt())
                .build();
    }

    private StatsResponse toStatsResponse(UrlMapping mapping) {
        return StatsResponse.builder()
                .id(String.valueOf(mapping.getId()))
                .url(mapping.getUrl())
                .shortCode(mapping.getShortCode())
                .createdAt(mapping.getCreatedAt())
                .updatedAt(mapping.getUpdatedAt())
                .accessCount(mapping.getAccessCount())
                .build();
    }
}