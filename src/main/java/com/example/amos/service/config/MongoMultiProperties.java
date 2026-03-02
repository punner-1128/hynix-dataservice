package com.example.amos.service.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ConfigurationProperties(prefix = "mongodb")
public class MongoMultiProperties {

    private Instance mongodb1;
    private Instance mongodb2;
    private Instance mongodb3;

    @Getter
    @Setter
    public static class Instance {
        private String uri;
    }
}
