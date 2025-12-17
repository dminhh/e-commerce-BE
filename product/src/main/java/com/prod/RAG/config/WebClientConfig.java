package com.prod.RAG.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;

@Configuration
public class WebClientConfig {

    @Value("${chatbot.flask.url:http://localhost:5001}")
    private String flaskUrl;

    @Value("${chatbot.flask.timeout-ms:30000}")
    private Integer timeoutMs;

    @Bean
    public WebClient webClient() {
        HttpClient httpClient = HttpClient.create()
                .responseTimeout(Duration.ofMillis(timeoutMs));

        return WebClient.builder()
                .baseUrl(flaskUrl)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }
}
