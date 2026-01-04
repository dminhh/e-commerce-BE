package com.common.configs;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisClientConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import redis.clients.jedis.JedisPoolConfig;

import java.time.Duration;

@Configuration
public class RedisConfig {
    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        // Cấu hình Redis server
        RedisStandaloneConfiguration redisStandaloneConfiguration = new RedisStandaloneConfiguration();
        redisStandaloneConfiguration.setHostName("localhost");
        redisStandaloneConfiguration.setPort(6379);

        // Cấu hình connection pool
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxTotal(20);              // Tối đa 20 connection
        poolConfig.setMaxIdle(10);               // Tối đa 10 connection idle
        poolConfig.setMinIdle(5);                // Tối thiểu 5 connection idle
        poolConfig.setMaxWait(Duration.ofSeconds(2));  // Chờ tối đa 2s để lấy connection
        poolConfig.setTestOnBorrow(true);        // Test khi borrow connection
        poolConfig.setTestOnReturn(true);        // Test khi return connection
        poolConfig.setTestWhileIdle(true);       // Test connection idle
        poolConfig.setTimeBetweenEvictionRuns(Duration.ofSeconds(30));  // Evictor chạy mỗi 30s

        // Cấu hình Jedis client với timeout tăng lên
        JedisClientConfiguration.JedisClientConfigurationBuilder jedisClientConfiguration =
            JedisClientConfiguration.builder();
        jedisClientConfiguration.connectTimeout(Duration.ofSeconds(5));  // Connect timeout 5s
        jedisClientConfiguration.readTimeout(Duration.ofSeconds(5));     // Read timeout 5s
        jedisClientConfiguration.usePooling().poolConfig(poolConfig);

        JedisConnectionFactory factory = new JedisConnectionFactory(
            redisStandaloneConfiguration,
            jedisClientConfiguration.build()
        );

        return factory;
    }

    @Bean
    public RedisTemplate<Object, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<Object, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        return template;
    }
}
