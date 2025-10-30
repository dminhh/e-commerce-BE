package com.ex.apigateway.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/fallback")
public class FallbackController {

    @GetMapping("/identify")
    public Mono<String> identifyFallback() {
        return Mono.just("Identity Service is currently unavailable. Please try again later.");
    }

    @GetMapping("/information")
    public Mono<String> informationFallback() {
        return Mono.just("Information Service is currently unavailable. Please try again later.");
    }

    @GetMapping("/product")
    public Mono<String> productFallback() {
        return Mono.just("Product Service is currently unavailable. Please try again later.");
    }

    @GetMapping("/payment")
    public Mono<String> paymentFallback() {
        return Mono.just("Payment Service is currently unavailable. Please try again later.");
    }

    @GetMapping("/notification")
    public Mono<String> notificationFallback() {
        return Mono.just("Notification Service is currently unavailable. Please try again later.");
    }

    @GetMapping("/logger")
    public Mono<String> loggerFallback() {
        return Mono.just("Logger Service is currently unavailable. Please try again later.");
    }
}
