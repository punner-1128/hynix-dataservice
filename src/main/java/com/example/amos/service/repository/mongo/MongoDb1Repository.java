package com.example.amos.service.repository.mongo;

import java.util.List;
import java.util.Map;

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
            query.addCriteria(Criteria.where(entry.getKey()).is(entry.getValue()));
        }
        return (List<Map<String, Object>>) (List<?>) mongoTemplate.find(query, Map.class, collection);
    }
}
