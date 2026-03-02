package com.example.amos.service.repository.mongo;

import java.util.List;
import java.util.Map;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.Date;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

@Repository
public class MongoDb1Repository {

    private final MongoTemplate mongoTemplate;
    
    public MongoDb1Repository(@Qualifier("mongoTemplate1") MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    public String ping() {
        Document response = mongoTemplate.executeCommand("{ ping: 1 }");
        return response.toJson();
    }

    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> searchCollection(String collection, Map<String, Object> filter) {
        if (filter == null || filter.isEmpty()) {
            return (List<Map<String, Object>>) (List<?>) mongoTemplate.findAll(Map.class, collection);
        }

        Query query = new Query();
        for (Map.Entry<String, Object> entry : filter.entrySet()) {
            Criteria criteria = Criteria.where(entry.getKey());
            Object value = entry.getValue();
            boolean hasOperator = false;

            if (value instanceof Map<?, ?> operatorMap) {
                for (Map.Entry<?, ?> opEntry : operatorMap.entrySet()) {
                    if (!(opEntry.getKey() instanceof String operator)) {
                        continue;
                    }
                    Object converted = convertValue(opEntry.getValue());
                    switch (operator) {
                        case "$gte" -> {
                            criteria.gte(converted);
                            hasOperator = true;
                        }
                        case "$lte" -> {
                            criteria.lte(converted);
                            hasOperator = true;
                        }
                        case "$gt" -> {
                            criteria.gt(converted);
                            hasOperator = true;
                        }
                        case "$lt" -> {
                            criteria.lt(converted);
                            hasOperator = true;
                        }
                        case "$eq" -> {
                            criteria.is(converted);
                            hasOperator = true;
                        }
                        default -> {
                        }
                    }
                }
            }

            if (!hasOperator) {
                criteria.is(convertValue(value));
            }
            query.addCriteria(criteria);
        }
        return (List<Map<String, Object>>) (List<?>) mongoTemplate.find(query, Map.class, collection);
    }

    private Object convertValue(Object value) {
        if (value instanceof String str) {
            try {
                return Date.from(Instant.parse(str));
            } catch (DateTimeParseException ignored) {
                return str;
            }
        }
        return value;
    }
}
