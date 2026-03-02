package com.example.amos.service.controller;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.amos.service.common.response.ApiResponse;
import com.example.amos.service.service.MultiInstanceDataService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class OracleDataController {

    private final MultiInstanceDataService multiInstanceDataService;

    @PostMapping("/oracle/{instanceId}/newtable")
    public ApiResponse<List<Map<String, Object>>> findAllNewTable(@PathVariable String instanceId) {
        return ApiResponse.ok(multiInstanceDataService.oracleNewTable(instanceId));
    }
}
