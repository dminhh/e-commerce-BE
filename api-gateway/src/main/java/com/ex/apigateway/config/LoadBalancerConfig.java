package com.ex.apigateway.config;

import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.loadbalancer.core.RandomLoadBalancer;
import org.springframework.cloud.loadbalancer.core.ReactorLoadBalancer;
import org.springframework.cloud.loadbalancer.core.RoundRobinLoadBalancer;
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;
import org.springframework.cloud.loadbalancer.support.LoadBalancerClientFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

/**
 * Load Balancer Configuration
 *
 * This configuration sets up the load balancing strategy for the API Gateway.
 * By default, Spring Cloud LoadBalancer uses Round Robin algorithm.
 *
 * Available strategies:
 * - RoundRobinLoadBalancer: Distributes requests evenly across all instances
 * - RandomLoadBalancer: Randomly selects an instance for each request
 * - Custom: You can implement your own LoadBalancer for custom strategies
 */
@Configuration
public class LoadBalancerConfig {

    /**
     * Configure Round Robin Load Balancer (Default)
     * This ensures requests are distributed evenly across service instances
     */
    @Bean
    public ReactorLoadBalancer<ServiceInstance> roundRobinLoadBalancer(
            Environment environment,
            LoadBalancerClientFactory loadBalancerClientFactory) {
        String name = environment.getProperty(LoadBalancerClientFactory.PROPERTY_NAME);
        return new RoundRobinLoadBalancer(
                loadBalancerClientFactory.getLazyProvider(name, ServiceInstanceListSupplier.class),
                name);
    }

    /**
     * Alternative: Random Load Balancer
     * Uncomment this bean and comment out the roundRobinLoadBalancer bean above
     * if you want to use random load balancing strategy
     */
    // @Bean
    // public ReactorLoadBalancer<ServiceInstance> randomLoadBalancer(
    //         Environment environment,
    //         LoadBalancerClientFactory loadBalancerClientFactory) {
    //     String name = environment.getProperty(LoadBalancerClientFactory.PROPERTY_NAME);
    //     return new RandomLoadBalancer(
    //             loadBalancerClientFactory.getLazyProvider(name, ServiceInstanceListSupplier.class),
    //             name);
    // }
}
