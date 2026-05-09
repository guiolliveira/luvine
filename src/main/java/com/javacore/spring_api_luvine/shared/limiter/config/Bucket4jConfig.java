package com.javacore.spring_api_luvine.shared.limiter.config;

import io.github.bucket4j.BucketConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class Bucket4jConfig {

    @Bean
    public BucketConfiguration bucketConfiguration() {
        return BucketConfiguration.builder()
                .addLimit(limit -> limit
                        .capacity(5)
                        .refillGreedy(5, Duration.ofHours(1)))
                .build();
    }
}