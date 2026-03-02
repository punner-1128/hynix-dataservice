package com.example.amos.service.controller;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.amos.service.common.response.ApiResponse;
import com.example.amos.service.dto.MongoCollectionSearchRequest;
import com.example.amos.service.dto.MongoOrdersAggregateRequest;
import com.example.amos.service.service.MultiInstanceDataService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/data")
@RequiredArgsConstructor
public class MongoDataController {

    private final MultiInstanceDataService multiInstanceDataService;

    @PostMapping("/mongo/{instanceId}/collection/search")
    public ApiResponse<List<Map<String, Object>>> searchCollection(@PathVariable String instanceId,
                                                                   @RequestBody MongoCollectionSearchRequest request) {
        MongoCollectionSearchRequest body = request == null ? new MongoCollectionSearchRequest() : request;

        return ApiResponse.ok(multiInstanceDataService.mongoCollectionSearch(
                instanceId,
                body.getCollection(),
                body.getFilter()));
    }

    @PostMapping("/mongo/{instanceId}/aggregate/orders")
    public ApiResponse<List<Map<String, Object>>> aggregateOrders(@PathVariable String instanceId,
                                                                  @RequestBody MongoOrdersAggregateRequest request) {
        MongoOrdersAggregateRequest body = request == null ? new MongoOrdersAggregateRequest() : request;
        return ApiResponse.ok(multiInstanceDataService.mongoAggregateOrders(
                instanceId,
                body.getCollection(),
                body.getMatch(),
                body.getGroupField(),
                body.getSumField(),
                body.getSortDesc()));
    }
}
