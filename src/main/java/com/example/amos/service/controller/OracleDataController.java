package com.example.amos.service.controller;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.amos.service.common.response.ApiResponse;
import com.example.amos.service.dto.OracleNewTableSearchRequest;
import com.example.amos.service.service.MultiInstanceDataService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/data")
public class OracleDataController {

    private final MultiInstanceDataService multiInstanceDataService;

    @PostMapping("/oracle/{instanceId}/newtable")
    public ApiResponse<List<Map<String, Object>>> findAllNewTable(@PathVariable String instanceId) {
        return ApiResponse.ok(multiInstanceDataService.oracleNewTable(instanceId));
    }
    
    @PostMapping("/oracle/{instanceId}/newtable/search")
    public ApiResponse<List<Map<String, Object>>> searchNewTable(@PathVariable String instanceId,
                                                                  @RequestBody OracleNewTableSearchRequest request) {
        OracleNewTableSearchRequest body = request == null ? new OracleNewTableSearchRequest() : request;
        return ApiResponse.ok(multiInstanceDataService.oracleNewTableSearch(
                instanceId,
                body.getColumn1(),
                body.getColumn2()));
    }
}
