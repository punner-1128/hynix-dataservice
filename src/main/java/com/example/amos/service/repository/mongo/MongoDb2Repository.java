package com.example.amos.service.repository.mongo;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class MongoDb2Repository {

    private final MongoTemplate mongoTemplate;
    
    public MongoDb2Repository(@Qualifier("mongoTemplate2") MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    public String ping() {
        Document response = mongoTemplate.executeCommand("{ ping: 1 }");
        return response.toJson();
    }
}
