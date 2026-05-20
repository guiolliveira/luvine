package com.javacore.spring_api_luvine.common.limiter.service;

import io.github.bucket4j.Bucket;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.ConsumptionProbe;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RateLimiterService {

    private final ProxyManager<byte[]> proxyManager;
    private final BucketConfiguration configuration;

    public ConsumptionProbe tryConsume(UUID publicId) {
        byte[] key = ("rate_limit:publicId:" + publicId).getBytes(StandardCharsets.UTF_8);

        Bucket bucket = proxyManager.getProxy(key, () -> configuration);

        return bucket.tryConsumeAndReturnRemaining(1);
    }
}