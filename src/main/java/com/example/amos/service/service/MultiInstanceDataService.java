package com.example.amos.service.service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.math.BigDecimal;

import org.springframework.stereotype.Service;

import com.example.amos.service.common.exception.ServiceException;
import com.example.amos.service.repository.logpresso.LogpressoDb1Client;
import com.example.amos.service.repository.logpresso.LogpressoDb2Client;
import com.example.amos.service.repository.logpresso.LogpressoDb3Client;
import com.example.amos.service.repository.mongo.MongoDb1Repository;
import com.example.amos.service.repository.mongo.MongoDb2Repository;
import com.example.amos.service.repository.mongo.MongoDb3Repository;
import com.example.amos.service.repository.oracle.OracleDb1Repository;
import com.example.amos.service.repository.oracle.OracleDb2Repository;
import com.example.amos.service.repository.oracle.OracleDb3Repository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MultiInstanceDataService {

    private final OracleDb1Repository oracleDb1Repository;
    private final OracleDb2Repository oracleDb2Repository;
    private final OracleDb3Repository oracleDb3Repository;

    private final MongoDb1Repository mongoDb1Repository;
    private final MongoDb2Repository mongoDb2Repository;
    private final MongoDb3Repository mongoDb3Repository;

    private final LogpressoDb1Client logpressoDb1Client;
    private final LogpressoDb2Client logpressoDb2Client;
    private final LogpressoDb3Client logpressoDb3Client;

    public Map<String, Object> oracleTimestamp(String instance) {
        String normalized = normalizeInstance(instance);
        String ts;

        switch (normalized) {
            case "oracledb1" -> ts = oracleDb1Repository.currentTimestamp();
            case "oracledb2" -> ts = oracleDb2Repository.currentTimestamp();
            case "oracledb3" -> ts = oracleDb3Repository.currentTimestamp();
            default -> throw new ServiceException("4001", "Unknown Oracle instance: " + instance);
        }

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("instance", normalized);
        data.put("timestamp", ts);
        return data;
    }

    public List<Map<String, Object>> oracleNewTable(String instance) {
        String normalized = normalizeOracleInstance(instance);

        return switch (normalized) {
            case "oracledb1" -> oracleDb1Repository.findAllNewTable();
            case "oracledb2" -> oracleDb2Repository.findAllNewTable();
            case "oracledb3" -> oracleDb3Repository.findAllNewTable();
            default -> throw new ServiceException("4001", "Unknown Oracle instance: " + instance);
        };
    }

    public List<Map<String, Object>> oracleNewTableSearch(String instance, String column1, BigDecimal column2) {
        String normalized = normalizeOracleInstance(instance);
        String normalizedColumn1 = normalizeOptionalText(column1);

        return switch (normalized) {
            case "oracledb1" -> oracleDb1Repository.searchNewTable(normalizedColumn1, column2);
            case "oracledb2" -> oracleDb2Repository.searchNewTable(normalizedColumn1, column2);
            case "oracledb3" -> oracleDb3Repository.searchNewTable(normalizedColumn1, column2);
            default -> throw new ServiceException("4001", "Unknown Oracle instance: " + instance);
        };
    }

    public Map<String, Object> mongoPing(String instance) {
        String normalized = normalizeInstance(instance);
        String response;

        switch (normalized) {
            case "mongodb1" -> response = mongoDb1Repository.ping();
            case "mongodb2" -> response = mongoDb2Repository.ping();
            case "mongodb3" -> response = mongoDb3Repository.ping();
            default -> throw new ServiceException("4002", "Unknown MongoDB instance: " + instance);
        }

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("instance", normalized);
        data.put("ping", response);
        return data;
    }

    public Map<String, Object> logpressoHealth(String instance) {
        String normalized = normalizeInstance(instance);
        String response;

        switch (normalized) {
            case "logpresso1" -> response = logpressoDb1Client.health();
            case "logpresso2" -> response = logpressoDb2Client.health();
            case "logpresso3" -> response = logpressoDb3Client.health();
            default -> throw new ServiceException("4003", "Unknown Logpresso instance: " + instance);
        }

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("instance", normalized);
        data.put("health", response);
        return data;
    }

    private String normalizeInstance(String instance) {
        return instance == null ? "" : instance.toLowerCase(Locale.ROOT).trim();
    }

    private String normalizeOracleInstance(String instance) {
        String normalized = normalizeInstance(instance);
        return switch (normalized) {
            case "primary" -> "oracledb1";
            case "secondary" -> "oracledb2";
            case "tertiary" -> "oracledb3";
            default -> normalized;
        };
    }

    private String normalizeOptionalText(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
