package com.example.amos.service.config;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;

@Data
@ConfigurationProperties(prefix = "spring.oracle")
public class OracleProperties {

    private List<String> databases = new ArrayList<>();
}
