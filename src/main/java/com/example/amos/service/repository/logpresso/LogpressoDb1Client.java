package com.example.amos.service.repository.logpresso;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.RestClient;

@Repository
public class LogpressoDb1Client implements LogpressoClient {

    private final RestClient restClient;
    
    public LogpressoDb1Client(@Qualifier("logpressoRestClient1") RestClient restClient) {
        this.restClient = restClient;
    }

    @Override
    @Retryable(retryFor = Exception.class,
            maxAttemptsExpression = "${logpresso.retry.max-attempts}",
            backoff = @Backoff(delayExpression = "${logpresso.retry.backoff-ms}"))
    public String health() {
        return restClient.get()
                .uri("/health")
                .retrieve()
                .body(String.class);
    }
}
