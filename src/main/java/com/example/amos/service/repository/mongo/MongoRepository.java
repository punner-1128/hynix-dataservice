package com.example.amos.service.repository.mongo;

import java.io.InputStream;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.query.BasicQuery;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;

@Repository
public class MongoRepository {

    private final Map<String, MongoTemplate> mongoTemplateMap;
    private final Map<String, MongoQueryDefinition> queryDefinitionMap;

    public MongoRepository(@Qualifier("mongoTemplateMap") Map<String, MongoTemplate> mongoTemplateMap) {
        this.mongoTemplateMap = mongoTemplateMap;
        this.queryDefinitionMap = loadQueryDefinitions();
    }

    public String ping(String mongoKey) {
        Document response = getTemplate(mongoKey).executeCommand("{ ping: 1 }");
        return response.toJson();
    }

    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> findAll(String mongoKey, String collection) {
        Query query = queryByCollection(collection)
                .map(definition -> createQuery(definition.criteria()))
                .orElseGet(Query::new);
        return (List<Map<String, Object>>) (List<?>) getTemplate(mongoKey).find(query, Map.class, collection);
    }

    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> findAllByQueryId(String mongoKey, String queryId) {
        MongoQueryDefinition definition = queryDefinitionMap.get(queryId);
        if (definition == null) {
            throw new IllegalArgumentException("Invalid mongo query id: " + queryId);
        }

        Query query = createQuery(definition.criteria());
        return (List<Map<String, Object>>) (List<?>) getTemplate(mongoKey)
                .find(query, Map.class, definition.collection());
    }

    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> findApiKeysWithFilter(String mongoKey, String service, String from, String to) {
        MongoQueryDefinition definition = queryDefinitionMap.get("findApiKeysWithFilter");
        if (definition == null) {
            throw new IllegalArgumentException("Invalid mongo query id: findApiKeysWithFilter");
        }

        String match = definition.match() == null ? "" : definition.match();
        if (!match.contains("service") || !match.contains("created_at")) {
            throw new IllegalStateException("Invalid match structure for query id: findApiKeysWithFilter");
        }

        Date fromDate = parseDateTime(from);
        Date toDate = parseDateTime(to);

        Query query = new Query();
        query.addCriteria(
                Criteria.where("service").is(service)
                        .and("created_at").gte(fromDate).lte(toDate));

        return (List<Map<String, Object>>) (List<?>) getTemplate(mongoKey)
                .find(query, Map.class, definition.collection());
    }

    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> find(String mongoKey, String collection, Map<String, Object> filter) {
        MongoTemplate mongoTemplate = getTemplate(mongoKey);

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

    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> aggregate(String mongoKey, String collection, Aggregation aggregation) {
        MongoTemplate mongoTemplate = getTemplate(mongoKey);
        return (List<Map<String, Object>>) (List<?>) mongoTemplate.aggregate(aggregation, collection, Map.class)
                .getMappedResults();
    }

    private MongoTemplate getTemplate(String mongoKey) {
        MongoTemplate template = mongoTemplateMap.get(mongoKey);
        if (template == null) {
            throw new IllegalArgumentException("Invalid mongoKey: " + mongoKey);
        }
        return template;
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

    private Map<String, MongoQueryDefinition> loadQueryDefinitions() {
        Map<String, MongoQueryDefinition> definitions = new LinkedHashMap<>();

        try (InputStream inputStream = new ClassPathResource("mongo-query.xml").getInputStream()) {
            var builderFactory = DocumentBuilderFactory.newInstance();
            var builder = builderFactory.newDocumentBuilder();
            var xmlDocument = builder.parse(inputStream);
            NodeList queryNodes = xmlDocument.getElementsByTagName("query");

            for (int i = 0; i < queryNodes.getLength(); i++) {
                var queryElement = (org.w3c.dom.Element) queryNodes.item(i);
                String id = queryElement.getAttribute("id");
                String collection = queryElement.getElementsByTagName("collection").item(0).getTextContent().trim();
                String criteria = getTagText(queryElement, "criteria");
                String match = getTagText(queryElement, "match");
                definitions.put(id, new MongoQueryDefinition(collection, criteria, match));
            }
        } catch (Exception e) {
            throw new IllegalStateException("Failed to load mongo-query.xml", e);
        }

        return definitions;
    }

    private Optional<MongoQueryDefinition> queryByCollection(String collection) {
        return queryDefinitionMap.values().stream()
                .filter(definition -> definition.collection().equals(collection))
                .findFirst();
    }

    private Query createQuery(String criteriaJson) {
        if (criteriaJson == null || criteriaJson.isBlank() || "{}".equals(criteriaJson.trim())) {
            return new Query();
        }
        return new BasicQuery(criteriaJson);
    }

    private String getTagText(org.w3c.dom.Element element, String tagName) {
        NodeList nodes = element.getElementsByTagName(tagName);
        if (nodes.getLength() == 0) {
            return "";
        }
        return nodes.item(0).getTextContent().trim();
    }

    private Date parseDateTime(String value) {
        try {
            return Date.from(Instant.parse(value));
        } catch (DateTimeParseException ignored) {
            LocalDateTime localDateTime = LocalDateTime.parse(value.replace(" ", "T"));
            return Date.from(localDateTime.toInstant(ZoneOffset.UTC));
        }
    }

    private record MongoQueryDefinition(String collection, String criteria, String match) {
    }
}
