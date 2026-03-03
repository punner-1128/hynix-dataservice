package com.example.amos.service.service;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.example.amos.service.config.MongoProperties;
import com.example.amos.service.repository.mongo.MongoRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class MongoService {

    private static final String FIND_ALL_API_KEYS_QUERY_ID = "findAllApiKeys";

    private final MongoProperties mongoProperties;
    private final MongoRepository mongoRepository;

    public MongoService(MongoProperties mongoProperties, MongoRepository mongoRepository) {
        this.mongoProperties = mongoProperties;
        this.mongoRepository = mongoRepository;
    }

    public List<Map<String, Object>> getApikeysAll() {
        Map<String, Object> merged = new LinkedHashMap<>();
        for (String mongoKey : mongoProperties.getDatabases()) {
            try {
                merged.put(mongoKey, mongoRepository.findAllByQueryId(mongoKey, FIND_ALL_API_KEYS_QUERY_ID));
            } catch (Exception e) {
                log.error("Failed to query api_keys from {}", mongoKey, e);
                merged.put(mongoKey, Collections.emptyList());
            }
        }
        return List.of(merged);
    }

    public List<Map<String, Object>> getApikeysAll2(String service, String from, String to) {
        Map<String, Object> merged = new LinkedHashMap<>();
        for (String mongoKey : mongoProperties.getDatabases()) {
            try {
                merged.put(mongoKey, mongoRepository.findApiKeysWithFilter(mongoKey, service, from, to));
            } catch (Exception e) {
                log.error("Failed to query filtered api_keys from {}", mongoKey, e);
                merged.put(mongoKey, Collections.emptyList());
            }
        }
        return List.of(merged);
    }
}
