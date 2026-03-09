package com.example.amos.service.controller;

import java.util.Map;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.amos.service.common.response.ApiResponse;
import com.example.amos.service.dto.SampleRequest;
import com.example.amos.service.service.OracleService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class HomeController {

    private final OracleService oracleService;

    @PostMapping("/api/sample")
    public ApiResponse<Map<String, Object>> sample(@RequestBody SampleRequest request) {
        return ApiResponse.ok(oracleService.selectNewTable(request.getColumn1(), request.getColumn2()));
    }

    @PostMapping("/api/newtable/select")
    public ApiResponse<Map<String, Object>> selectNewTable(@RequestBody SampleRequest request) {
        return ApiResponse.ok(oracleService.selectNewTable(request.getColumn1(), request.getColumn2()));
    }

    @PostMapping("/api/newtable/select-map")
    public ApiResponse<Map<String, Object>> selectNewTableByMap(@RequestBody Map<String, Object> request) {
        String column1 = (String) request.get("column1");
        Long column2 = request.get("column2") == null ? null : Long.valueOf(request.get("column2").toString());
        return ApiResponse.ok(oracleService.selectNewTable(column1, column2));
    }

    @PostMapping("/api/newtable/select-param")
    public ApiResponse<Map<String, Object>> selectNewTableByParam(
            @RequestParam String column1,
            @RequestParam Long column2) {
        return ApiResponse.ok(oracleService.selectNewTable(column1, column2));
    }

    @PostMapping("/api/newtable/all")
    public ApiResponse<Map<String, Object>> selectAllNewTable() {
        return ApiResponse.ok(oracleService.selectAllNewTable());
    }
}
