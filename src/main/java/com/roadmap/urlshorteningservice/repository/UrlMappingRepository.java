package com.roadmap.urlshorteningservice.repository;

import com.roadmap.urlshorteningservice.entity.UrlMapping;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UrlMappingRepository extends JpaRepository<UrlMapping, Long> {

    boolean existsByShortCode(String shortCode);

    boolean existsByUrl(String url);

    Optional<UrlMapping> findByShortCode(String shortCode);
}