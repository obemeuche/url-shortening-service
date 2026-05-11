package com.roadmap.urlshorteningservice.service;

import com.roadmap.urlshorteningservice.entity.UrlMapping;
import com.roadmap.urlshorteningservice.exception.ShortUrlNotFoundException;
import com.roadmap.urlshorteningservice.exception.UrlAlreadyExistsException;
import com.roadmap.urlshorteningservice.model.Request;
import com.roadmap.urlshorteningservice.model.Response;
import com.roadmap.urlshorteningservice.repository.UrlMappingRepository;
import com.roadmap.urlshorteningservice.util.ShortCodeGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UrlShorteningServiceTest {

    @Mock
    private UrlMappingRepository repository;

    @Mock
    private ShortCodeGenerator shortCodeGenerator;

    @InjectMocks
    private UrlShorteningService service;

    @Test
    void createShortUrl_savesEntityAndReturnsResponse() {
        Request request = new Request();
        setUrl(request, "https://www.example.com/long/url");

        when(repository.existsByUrl("https://www.example.com/long/url")).thenReturn(false);
        when(shortCodeGenerator.generate()).thenReturn("abc123");
        when(repository.existsByShortCode("abc123")).thenReturn(false);

        UrlMapping saved = UrlMapping.builder()
                .id(1L)
                .url("https://www.example.com/long/url")
                .shortCode("abc123")
                .accessCount(0L)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        when(repository.save(any(UrlMapping.class))).thenReturn(saved);

        Response response = service.createShortUrl(request);

        assertThat(response.getId()).isEqualTo("1");
        assertThat(response.getUrl()).isEqualTo("https://www.example.com/long/url");
        assertThat(response.getShortCode()).isEqualTo("abc123");
        assertThat(response.getCreatedAt()).isNotNull();
        assertThat(response.getUpdatedAt()).isNotNull();
        verify(repository).save(any(UrlMapping.class));
    }

    @Test
    void createShortUrl_throwsWhenUrlAlreadyExists() {
        Request request = new Request();
        setUrl(request, "https://www.example.com/duplicate");

        when(repository.existsByUrl("https://www.example.com/duplicate")).thenReturn(true);

        assertThatThrownBy(() -> service.createShortUrl(request))
                .isInstanceOf(UrlAlreadyExistsException.class)
                .hasMessageContaining("https://www.example.com/duplicate");

        verify(repository, never()).save(any());
    }

    @Test
    void createShortUrl_retriesShortCodeGenerationOnCollision() {
        Request request = new Request();
        setUrl(request, "https://www.example.com/long/url");

        when(repository.existsByUrl(any())).thenReturn(false);
        // First code collides, second is unique
        when(shortCodeGenerator.generate()).thenReturn("taken1", "free22");
        when(repository.existsByShortCode("taken1")).thenReturn(true);
        when(repository.existsByShortCode("free22")).thenReturn(false);

        UrlMapping saved = UrlMapping.builder()
                .id(1L)
                .url("https://www.example.com/long/url")
                .shortCode("free22")
                .accessCount(0L)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        when(repository.save(any(UrlMapping.class))).thenReturn(saved);

        Response response = service.createShortUrl(request);

        assertThat(response.getShortCode()).isEqualTo("free22");
        verify(shortCodeGenerator, times(2)).generate();
    }

    @Test
    void getByShortCode_returnsResponse() {
        UrlMapping mapping = UrlMapping.builder()
                .id(1L)
                .url("https://www.example.com/long/url")
                .shortCode("abc123")
                .accessCount(0L)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        when(repository.findByShortCode("abc123")).thenReturn(java.util.Optional.of(mapping));

        Response response = service.getByShortCode("abc123");

        assertThat(response.getId()).isEqualTo("1");
        assertThat(response.getUrl()).isEqualTo("https://www.example.com/long/url");
        assertThat(response.getShortCode()).isEqualTo("abc123");
    }

    @Test
    void getByShortCode_throwsWhenNotFound() {
        when(repository.findByShortCode("unknown")).thenReturn(java.util.Optional.empty());

        assertThatThrownBy(() -> service.getByShortCode("unknown"))
                .isInstanceOf(ShortUrlNotFoundException.class)
                .hasMessageContaining("unknown");
    }

    // Request has no public setter — use reflection to set url in tests
    private void setUrl(Request request, String url) {
        try {
            var field = Request.class.getDeclaredField("url");
            field.setAccessible(true);
            field.set(request, url);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}