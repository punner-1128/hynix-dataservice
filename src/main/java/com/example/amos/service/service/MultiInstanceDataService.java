package com.example.amos.service.service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.Date;

import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;

import com.example.amos.service.common.exception.ServiceException;
import com.example.amos.service.repository.logpresso.LogpressoDb1Client;
import com.example.amos.service.repository.logpresso.LogpressoDb2Client;
import com.example.amos.service.repository.logpresso.LogpressoDb3Client;
import com.example.amos.service.repository.mongo.MongoRepository;
import com.example.amos.service.repository.oracle.OracleRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MultiInstanceDataService {

    private final OracleRepository oracleRepository;
    private final OracleNewTableService oracleNewTableService;
    private final MongoRepository mongoRepository;

    private final LogpressoDb1Client logpressoDb1Client;
    private final LogpressoDb2Client logpressoDb2Client;
    private final LogpressoDb3Client logpressoDb3Client;

    public Map<String, Object> oracleTimestamp(String instance) {
        String ts = oracleRepository.currentTimestamp(instance);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("instance", instance);
        data.put("timestamp", ts);
        return data;
    }

    public List<Map<String, Object>> oracleNewTable(String instance) {
        return oracleNewTableService.getNewTable(instance);
    }

    public List<Map<String, Object>> oracleNewTableSearch(String instance, String column1, BigDecimal column2) {
        String normalizedColumn1 = normalizeOptionalText(column1);
        return oracleNewTableService.searchNewTable(instance, normalizedColumn1, column2);
    }

    public Map<String, Object> mongoPing(String instance) {
        String response = mongoRepository.ping(instance);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("instance", instance);
        data.put("ping", response);
        return data;
    }

    public List<Map<String, Object>> mongoCollectionSearch(String instance, String collection, Map<String, Object> filter) {
        String validatedCollection = normalizeRequiredText(collection, "collection is required");
        Map<String, Object> normalizedFilter = normalizeMatchValues(filter);
        return mongoRepository.find(instance, validatedCollection, normalizedFilter);
    }

    public List<Map<String, Object>> mongoAggregateOrders(String instance,
                                                          String collection,
                                                          Map<String, Object> match,
                                                          String groupField,
                                                          String sumField,
                                                          Boolean sortDesc) {
        String validatedCollection = normalizeRequiredText(collection, "collection is required");
        String normalizedGroupField = normalizeOptionalText(groupField);
        String normalizedSumField = normalizeOptionalText(sumField);
        Map<String, Object> normalizedMatch = normalizeMatchValues(match);
        boolean useGroupAggregation = normalizedGroupField != null && normalizedSumField != null;

        List<AggregationOperation> operations = new ArrayList<>();
        if (normalizedMatch != null && !normalizedMatch.isEmpty()) {
            List<Criteria> criteriaList = new ArrayList<>();
            for (Map.Entry<String, Object> entry : normalizedMatch.entrySet()) {
                Criteria fieldCriteria = Criteria.where(entry.getKey());
                Object value = entry.getValue();
                boolean hasOperator = false;

                if (value instanceof Map<?, ?> operatorMap) {
                    for (Map.Entry<?, ?> operatorEntry : operatorMap.entrySet()) {
                        if (!(operatorEntry.getKey() instanceof String operator)) {
                            continue;
                        }

                        Object operatorValue = operatorEntry.getValue();
                        switch (operator) {
                            case "$gte" -> {
                                fieldCriteria.gte(operatorValue);
                                hasOperator = true;
                            }
                            case "$lte" -> {
                                fieldCriteria.lte(operatorValue);
                                hasOperator = true;
                            }
                            case "$gt" -> {
                                fieldCriteria.gt(operatorValue);
                                hasOperator = true;
                            }
                            case "$lt" -> {
                                fieldCriteria.lt(operatorValue);
                                hasOperator = true;
                            }
                            case "$eq" -> {
                                fieldCriteria.is(operatorValue);
                                hasOperator = true;
                            }
                            default -> {
                            }
                        }
                    }
                }

                if (!hasOperator) {
                    fieldCriteria.is(value);
                }
                criteriaList.add(fieldCriteria);
            }

            if (!criteriaList.isEmpty()) {
                operations.add(Aggregation.match(new Criteria().andOperator(criteriaList.toArray(new Criteria[0]))));
            }
        }

        if (useGroupAggregation) {
            operations.add(Aggregation.group(normalizedGroupField).sum(normalizedSumField).as("totalAmount"));
            if (sortDesc != null) {
                operations.add(Aggregation.sort(Boolean.TRUE.equals(sortDesc) ? Sort.Direction.DESC : Sort.Direction.ASC,
                        "totalAmount"));
            }
        }

        Aggregation aggregation = Aggregation.newAggregation(operations);

        return mongoRepository.aggregate(instance, validatedCollection, aggregation);
    }

    public Map<String, Object> logpressoHealth(String instance) {
        String response;

        switch (instance) {
            case "logpresso1" -> response = logpressoDb1Client.health();
            case "logpresso2" -> response = logpressoDb2Client.health();
            case "logpresso3" -> response = logpressoDb3Client.health();
            default -> throw new ServiceException("4003", "Unknown Logpresso instance: " + instance);
        }

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("instance", instance);
        data.put("health", response);
        return data;
    }

    private String normalizeOptionalText(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String normalizeRequiredText(String value, String message) {
        String normalized = normalizeOptionalText(value);
        if (normalized == null) {
            throw new ServiceException("4004", message);
        }
        return normalized;
    }

    private Map<String, Object> normalizeMatchValues(Map<String, Object> source) {
        if (source == null) {
            return null;
        }

        Map<String, Object> normalized = new LinkedHashMap<>();
        for (Map.Entry<String, Object> entry : source.entrySet()) {
            normalized.put(entry.getKey(), normalizeMatchValue(entry.getValue()));
        }
        return normalized;
    }

    @SuppressWarnings("unchecked")
    private Object normalizeMatchValue(Object value) {
        if (value instanceof String str) {
            try {
                return Date.from(Instant.parse(str));
            } catch (DateTimeParseException ignored) {
                return str;
            }
        }
        if (value instanceof Map<?, ?> mapValue) {
            Map<String, Object> converted = new LinkedHashMap<>();
            for (Map.Entry<?, ?> mapEntry : mapValue.entrySet()) {
                if (mapEntry.getKey() instanceof String key) {
                    converted.put(key, normalizeMatchValue(mapEntry.getValue()));
                }
            }
            return converted;
        }
        if (value instanceof List<?> listValue) {
            List<Object> converted = new ArrayList<>();
            for (Object item : listValue) {
                converted.add(normalizeMatchValue(item));
            }
            return converted;
        }
        return value;
    }

}
