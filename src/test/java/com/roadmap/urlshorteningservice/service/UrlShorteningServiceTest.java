package com.roadmap.urlshorteningservice.service;

import com.roadmap.urlshorteningservice.entity.UrlMapping;
import com.roadmap.urlshorteningservice.exception.ShortUrlNotFoundException;
import com.roadmap.urlshorteningservice.exception.UrlAlreadyExistsException;
import com.roadmap.urlshorteningservice.model.Request;
import com.roadmap.urlshorteningservice.model.Response;
import com.roadmap.urlshorteningservice.model.StatsResponse;
import com.roadmap.urlshorteningservice.repository.UrlMappingRepository;
import com.roadmap.urlshorteningservice.util.ShortCodeGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

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
        when(repository.findByShortCode("abc123")).thenReturn(Optional.of(mapping));

        Response response = service.getByShortCode("abc123");

        assertThat(response.getId()).isEqualTo("1");
        assertThat(response.getUrl()).isEqualTo("https://www.example.com/long/url");
        assertThat(response.getShortCode()).isEqualTo("abc123");
        verify(repository, never()).save(any());
    }

    @Test
    void getByShortCode_throwsWhenNotFound() {
        when(repository.findByShortCode("unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getByShortCode("unknown"))
                .isInstanceOf(ShortUrlNotFoundException.class)
                .hasMessageContaining("unknown");
    }

    @Test
    void updateShortUrl_updatesUrlAndReturnsResponse() {
        Request request = new Request();
        setUrl(request, "https://www.example.com/updated-url");

        UrlMapping existing = UrlMapping.builder()
                .id(1L)
                .url("https://www.example.com/original-url")
                .shortCode("abc123")
                .accessCount(0L)
                .createdAt(LocalDateTime.of(2026, 1, 1, 12, 0))
                .updatedAt(LocalDateTime.of(2026, 1, 1, 12, 0))
                .build();
        when(repository.findByShortCode("abc123")).thenReturn(Optional.of(existing));
        when(repository.existsByUrl("https://www.example.com/updated-url")).thenReturn(false);

        UrlMapping updated = UrlMapping.builder()
                .id(1L)
                .url("https://www.example.com/updated-url")
                .shortCode("abc123")
                .accessCount(0L)
                .createdAt(LocalDateTime.of(2026, 1, 1, 12, 0))
                .updatedAt(LocalDateTime.of(2026, 1, 1, 12, 30))
                .build();
        when(repository.save(existing)).thenReturn(updated);

        Response response = service.updateShortUrl("abc123", request);

        assertThat(response.getId()).isEqualTo("1");
        assertThat(response.getUrl()).isEqualTo("https://www.example.com/updated-url");
        assertThat(response.getShortCode()).isEqualTo("abc123");
        assertThat(response.getCreatedAt()).isEqualTo(LocalDateTime.of(2026, 1, 1, 12, 0));
        assertThat(response.getUpdatedAt()).isEqualTo(LocalDateTime.of(2026, 1, 1, 12, 30));
        verify(repository).save(existing);
    }

    @Test
    void updateShortUrl_throwsWhenShortCodeNotFound() {
        Request request = new Request();
        setUrl(request, "https://www.example.com/updated-url");

        when(repository.findByShortCode("unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.updateShortUrl("unknown", request))
                .isInstanceOf(ShortUrlNotFoundException.class)
                .hasMessageContaining("unknown");

        verify(repository, never()).save(any());
    }

    @Test
    void updateShortUrl_throwsWhenNewUrlAlreadyTakenByDifferentRecord() {
        Request request = new Request();
        setUrl(request, "https://www.example.com/taken-url");

        UrlMapping existing = UrlMapping.builder()
                .id(1L)
                .url("https://www.example.com/original-url")
                .shortCode("abc123")
                .accessCount(0L)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        when(repository.findByShortCode("abc123")).thenReturn(Optional.of(existing));
        when(repository.existsByUrl("https://www.example.com/taken-url")).thenReturn(true);

        assertThatThrownBy(() -> service.updateShortUrl("abc123", request))
                .isInstanceOf(UrlAlreadyExistsException.class)
                .hasMessageContaining("https://www.example.com/taken-url");

        verify(repository, never()).save(any());
    }

    @Test
    void updateShortUrl_allowsUpdatingToSameUrl() {
        Request request = new Request();
        setUrl(request, "https://www.example.com/same-url");

        UrlMapping existing = UrlMapping.builder()
                .id(1L)
                .url("https://www.example.com/same-url")
                .shortCode("abc123")
                .accessCount(0L)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        when(repository.findByShortCode("abc123")).thenReturn(Optional.of(existing));
        when(repository.save(existing)).thenReturn(existing);

        Response response = service.updateShortUrl("abc123", request);

        assertThat(response.getUrl()).isEqualTo("https://www.example.com/same-url");
        verify(repository, never()).existsByUrl(any());
        verify(repository).save(existing);
    }

    @Test
    void getStats_returnsStatsResponseWithAccessCount() {
        UrlMapping mapping = UrlMapping.builder()
                .id(1L)
                .url("https://www.example.com/long/url")
                .shortCode("abc123")
                .accessCount(7L)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        when(repository.findByShortCode("abc123")).thenReturn(Optional.of(mapping));

        StatsResponse response = service.getStats("abc123");

        assertThat(response.getId()).isEqualTo("1");
        assertThat(response.getUrl()).isEqualTo("https://www.example.com/long/url");
        assertThat(response.getShortCode()).isEqualTo("abc123");
        assertThat(response.getAccessCount()).isEqualTo(7L);
        verify(repository, never()).save(any());
    }

    @Test
    void getStats_throwsWhenNotFound() {
        when(repository.findByShortCode("unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getStats("unknown"))
                .isInstanceOf(ShortUrlNotFoundException.class)
                .hasMessageContaining("unknown");
    }

    @Test
    void deleteByShortCode_deletesSuccessfully() {
        UrlMapping existing = UrlMapping.builder()
                .id(1L)
                .url("https://www.example.com/to-be-deleted")
                .shortCode("abc123")
                .accessCount(0L)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        when(repository.findByShortCode("abc123")).thenReturn(Optional.of(existing));

        service.deleteByShortCode("abc123");

        verify(repository).delete(existing);
    }

    @Test
    void deleteByShortCode_throwsWhenShortCodeNotFound() {
        when(repository.findByShortCode("unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.deleteByShortCode("unknown"))
                .isInstanceOf(ShortUrlNotFoundException.class)
                .hasMessageContaining("unknown");

        verify(repository, never()).delete(any());
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