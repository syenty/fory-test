package com.example.forytest.step2.server.controller;

import com.example.forytest.step2.server.service.ImageService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class ImageController {
    private final ImageService imageService;

    public ImageController(ImageService imageService) {
        this.imageService = imageService;
    }

    @PostMapping("/json")
    public ResponseEntity<String> uploadJson(@RequestBody byte[] body) {
        return ResponseEntity.ok(imageService.handleJson(body));
    }

    @PostMapping("/fory")
    public ResponseEntity<String> uploadFory(@RequestBody byte[] body) {
        return ResponseEntity.ok(imageService.handleFory(body));
    }
}
