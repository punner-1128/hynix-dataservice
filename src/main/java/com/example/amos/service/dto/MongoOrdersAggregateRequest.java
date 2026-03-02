package com.example.amos.service.dto;

import java.util.Map;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class MongoOrdersAggregateRequest {

    private String collection;
    private Map<String, Object> match;
    private String groupField;
    private String sumField;
    private Boolean sortDesc;
}
