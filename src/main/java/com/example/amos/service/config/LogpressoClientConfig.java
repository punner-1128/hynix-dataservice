package com.example.amos.service.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
public class LogpressoClientConfig {

    @Bean(name = "logpressoRestClient1")
    public RestClient logpressoRestClient1(@Value("${logpresso.logpresso1.base-url}") String baseUrl,
                                           @Value("${logpresso.logpresso1.connect-timeout-ms}") int connectTimeoutMs,
                                           @Value("${logpresso.logpresso1.read-timeout-ms}") int readTimeoutMs) {
        return buildClient(baseUrl, connectTimeoutMs, readTimeoutMs);
    }

    @Bean(name = "logpressoRestClient2")
    public RestClient logpressoRestClient2(@Value("${logpresso.logpresso2.base-url}") String baseUrl,
                                           @Value("${logpresso.logpresso2.connect-timeout-ms}") int connectTimeoutMs,
                                           @Value("${logpresso.logpresso2.read-timeout-ms}") int readTimeoutMs) {
        return buildClient(baseUrl, connectTimeoutMs, readTimeoutMs);
    }

    @Bean(name = "logpressoRestClient3")
    public RestClient logpressoRestClient3(@Value("${logpresso.logpresso3.base-url}") String baseUrl,
                                           @Value("${logpresso.logpresso3.connect-timeout-ms}") int connectTimeoutMs,
                                           @Value("${logpresso.logpresso3.read-timeout-ms}") int readTimeoutMs) {
        return buildClient(baseUrl, connectTimeoutMs, readTimeoutMs);
    }

    private RestClient buildClient(String baseUrl, int connectTimeoutMs, int readTimeoutMs) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(connectTimeoutMs);
        requestFactory.setReadTimeout(readTimeoutMs);

        return RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(requestFactory)
                .build();
    }
}
