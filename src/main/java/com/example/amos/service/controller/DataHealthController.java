package com.example.amos.service.controller;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.amos.service.common.response.ApiResponse;
import com.example.amos.service.service.MultiInstanceDataService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/data")
@RequiredArgsConstructor
public class DataHealthController {

    private final MultiInstanceDataService multiInstanceDataService;

    @GetMapping("/oracle/{instanceId}/timestamp")
    public ApiResponse<Map<String, Object>> oracleTimestamp(@PathVariable String instanceId) {
        return ApiResponse.ok(multiInstanceDataService.oracleTimestamp(instanceId));
    }

    @GetMapping("/mongo/{instanceId}/ping")
    public ApiResponse<Map<String, Object>> mongoPing(@PathVariable String instanceId) {
        return ApiResponse.ok(multiInstanceDataService.mongoPing(instanceId));
    }

    @GetMapping("/logpresso/{instanceId}/health")
    public ApiResponse<Map<String, Object>> logpressoHealth(@PathVariable String instanceId) {
        return ApiResponse.ok(multiInstanceDataService.logpressoHealth(instanceId));
    }
}
