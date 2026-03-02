package com.example.amos.service.dto;

import java.util.Map;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class MongoCollectionSearchRequest {

    private String collection;
    private Map<String, Object> filter;
}
