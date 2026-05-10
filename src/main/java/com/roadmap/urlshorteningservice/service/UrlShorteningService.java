package com.roadmap.urlshorteningservice.service;

import com.roadmap.urlshorteningservice.entity.UrlMapping;
import com.roadmap.urlshorteningservice.model.Request;
import com.roadmap.urlshorteningservice.model.Response;
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
        String shortCode = generateUniqueShortCode();

        UrlMapping mapping = UrlMapping.builder()
                .url(request.getUrl())
                .shortCode(shortCode)
                .build();

        UrlMapping saved = repository.save(mapping);

        return toResponse(saved);
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
}