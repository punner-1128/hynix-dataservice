package com.example.amos.service.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.example.amos.service.common.response.ApiResponse;
import com.example.amos.service.dto.ApikeyFilterRequest;
import com.example.amos.service.service.MongoService;

@RestController
@RequestMapping("/api/v1/data")
public class MongoController {

    private final MongoService mongoService;

    public MongoController(MongoService mongoService) {
        this.mongoService = mongoService;
    }

    @PostMapping("/mongo/apikeys/all")
    public ApiResponse<List<Map<String, Object>>> getApikeysAll() {
        return ApiResponse.ok(mongoService.getApikeysAll());
    }

    @PostMapping("/mongo/apikeys/all2")
    public ResponseEntity<?> getApikeysAll2(
            @RequestBody ApikeyFilterRequest request) {
        if (request == null
                || isBlank(request.getService())
                || isBlank(request.getFrom())
                || isBlank(request.getTo())) {
            return ResponseEntity.badRequest().body("Invalid request body");
        }

        return ResponseEntity.ok(
                mongoService.getApikeysAll2(
                        request.getService(),
                        request.getFrom(),
                        request.getTo()));
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
