package com.roadmap.urlshorteningservice.controller;

import com.roadmap.urlshorteningservice.exception.GlobalExceptionHandler;
import com.roadmap.urlshorteningservice.model.Request;
import com.roadmap.urlshorteningservice.model.Response;
import com.roadmap.urlshorteningservice.model.StatsResponse;
import com.roadmap.urlshorteningservice.service.UrlShorteningService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "URL Shortening", description = "Endpoints for managing shortened URLs")
@RestController
@RequestMapping("/shorten")
@RequiredArgsConstructor
public class UrlShorteningController {

    private final UrlShorteningService service;

    @Operation(
            summary = "Create a short URL",
            description = "Generates a unique short code for the given URL. Returns 409 if a short code already exists for that URL."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Short URL created successfully",
                    content = @Content(schema = @Schema(implementation = Response.class))),
            @ApiResponse(responseCode = "400", description = "Invalid or blank URL",
                    content = @Content(schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "A short code already exists for this URL",
                    content = @Content(schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class)))
    })
    @PostMapping
    public ResponseEntity<Response> shorten(@Valid @RequestBody Request request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createShortUrl(request));
    }

    @Operation(
            summary = "Retrieve a URL by short code",
            description = "Returns the original URL mapping and increments the access count."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "URL found",
                    content = @Content(schema = @Schema(implementation = Response.class))),
            @ApiResponse(responseCode = "404", description = "Short code not found",
                    content = @Content(schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class)))
    })
    @GetMapping("/{shortCode}")
    public ResponseEntity<Response> retrieve(
            @Parameter(description = "The short code identifying the URL") @PathVariable String shortCode) {
        return ResponseEntity.ok(service.getByShortCode(shortCode));
    }

    @Operation(
            summary = "Update a shortened URL",
            description = "Replaces the original URL associated with the given short code. Returns 409 if the new URL already has a short code."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "URL updated successfully",
                    content = @Content(schema = @Schema(implementation = Response.class))),
            @ApiResponse(responseCode = "400", description = "Invalid or blank URL",
                    content = @Content(schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Short code not found",
                    content = @Content(schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "A short code already exists for the new URL",
                    content = @Content(schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class)))
    })
    @PutMapping("/{shortCode}")
    public ResponseEntity<Response> update(
            @Parameter(description = "The short code identifying the URL") @PathVariable String shortCode,
            @Valid @RequestBody Request request) {
        return ResponseEntity.ok(service.updateShortUrl(shortCode, request));
    }

    @Operation(
            summary = "Get URL statistics",
            description = "Returns the URL mapping along with the total number of times it has been accessed."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Statistics retrieved successfully",
                    content = @Content(schema = @Schema(implementation = StatsResponse.class))),
            @ApiResponse(responseCode = "404", description = "Short code not found",
                    content = @Content(schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class)))
    })
    @GetMapping("/{shortCode}/stats")
    public ResponseEntity<StatsResponse> stats(
            @Parameter(description = "The short code identifying the URL") @PathVariable String shortCode) {
        return ResponseEntity.ok(service.getStats(shortCode));
    }

    @Operation(
            summary = "Delete a short URL",
            description = "Permanently removes the short code and its associated URL."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Short URL deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Short code not found",
                    content = @Content(schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class)))
    })
    @DeleteMapping("/{shortCode}")
    public ResponseEntity<Void> delete(
            @Parameter(description = "The short code identifying the URL") @PathVariable String shortCode) {
        service.deleteByShortCode(shortCode);
        return ResponseEntity.noContent().build();
    }
}