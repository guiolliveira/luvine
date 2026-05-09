package com.javacore.spring_api_luvine.shared.limiter.config;

import io.github.bucket4j.distributed.proxy.ProxyManager;
import io.github.bucket4j.redis.lettuce.Bucket4jLettuce;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(ValkeyProperties.class)
public class ValkeyConfig {

    private final ValkeyProperties properties;

    @Bean(destroyMethod = "shutdown")
    public RedisClient redisClient() {
        RedisURI.Builder builder = RedisURI.Builder
                .redis(properties.host(), properties.port())
                .withTimeout(Duration.ofSeconds(2));

        if (properties.password() != null && !properties.password().isBlank()) {
            builder.withPassword(properties.password().toCharArray());
        }

        RedisURI redisURI = builder.build();
        return RedisClient.create(redisURI);
    }

    @Bean
    public ProxyManager<byte[]> proxyManager(RedisClient redisClient) {
        return Bucket4jLettuce
                .casBasedBuilder(redisClient)
                .build();
    }
}