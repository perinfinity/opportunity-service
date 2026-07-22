package com.perinfinity.volunteering.opportunity.controller;

import com.perinfinity.volunteering.opportunity.dto.ImageUploadResponse;
import com.perinfinity.volunteering.opportunity.dto.StoredImage;
import com.perinfinity.volunteering.opportunity.service.IImageStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.time.Duration;

@RestController
@RequestMapping("api/v1/images")
@RequiredArgsConstructor
public class ImageController {

    private final IImageStorageService imageStorageService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ImageUploadResponse> upload(@RequestParam("file") MultipartFile file) {
        String id = imageStorageService.store(file);
        String url = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/v1/images/{id}")
                .buildAndExpand(id)
                .toUriString();
        return ResponseEntity.created(URI.create(url)).body(new ImageUploadResponse(id, url));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Resource> getImage(@PathVariable String id) {
        StoredImage image = imageStorageService.load(id);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(image.contentType()))
                .cacheControl(CacheControl.maxAge(Duration.ofDays(365)).cachePublic())
                .body(image.resource());
    }
}
