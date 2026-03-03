package com.example.amos.service.config;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;

import com.example.amos.service.common.exception.ServiceException;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;

@Configuration
@EnableConfigurationProperties(MongoProperties.class)
public class MongoConfig {

    private final MongoProperties mongoProperties;
    private final Environment environment;

    public MongoConfig(MongoProperties mongoProperties, Environment environment) {
        this.mongoProperties = mongoProperties;
        this.environment = environment;
    }

    @Bean(name = "mongoTemplateMap")
    public Map<String, MongoTemplate> mongoTemplateMap() {
        Map<String, MongoTemplate> templateMap = new LinkedHashMap<>();
        for (String mongoKey : mongoProperties.getDatabases()) {
            String prefix = "spring.data.mongodb." + mongoKey;
            String uri = requiredProperty(prefix + ".uri", mongoKey);
            int maxSize = requiredIntProperty(prefix + ".pool.max-size", mongoKey);
            int minSize = requiredIntProperty(prefix + ".pool.min-size", mongoKey);

            ConnectionString connectionString = new ConnectionString(uri);
            String database = connectionString.getDatabase();
            if (database == null || database.isBlank()) {
                throw new ServiceException("5002", "MongoDB database name is missing in URI for key: " + mongoKey);
            }

            MongoClientSettings settings = MongoClientSettings.builder()
                    .applyConnectionString(connectionString)
                    .applyToConnectionPoolSettings(poolBuilder -> {
                        poolBuilder.maxSize(maxSize);
                        poolBuilder.minSize(minSize);
                    })
                    .build();

            MongoClient mongoClient = MongoClients.create(settings);
            MongoTemplate mongoTemplate = new MongoTemplate(
                    new SimpleMongoClientDatabaseFactory(mongoClient, database));
            templateMap.put(mongoKey, mongoTemplate);
        }
        return Collections.unmodifiableMap(templateMap);
    }

    @Bean(name = "mongoTemplate")
    @Primary
    public MongoTemplate mongoTemplate(@Qualifier("mongoTemplateMap") Map<String, MongoTemplate> mongoTemplateMap) {
        if (mongoProperties.getDatabases().isEmpty()) {
            throw new ServiceException("5002", "spring.mongodb.databases is empty");
        }

        String primaryKey = mongoProperties.getDatabases().get(0);
        MongoTemplate template = mongoTemplateMap.get(primaryKey);
        if (template == null) {
            throw new ServiceException("5002", "Primary MongoTemplate not found for key: " + primaryKey);
        }
        return template;
    }

    private String requiredProperty(String key, String mongoKey) {
        String value = environment.getProperty(key);
        if (value == null || value.isBlank()) {
            throw new ServiceException("5002", "Missing MongoDB property: " + key + " (" + mongoKey + ")");
        }
        return value;
    }

    private int requiredIntProperty(String key, String mongoKey) {
        Integer value = environment.getProperty(key, Integer.class);
        if (value == null) {
            throw new ServiceException("5002", "Missing MongoDB property: " + key + " (" + mongoKey + ")");
        }
        return value;
    }
}
