package com.ad.bidding.analytics.domain.configs;

import lombok.Getter;

@Getter
public class SourceConfigs {
    private final String endpoint;
    private final String accessKey;
    private final String secretKey;

    public SourceConfigs() {
        this.endpoint = getEnvOrThrow("MINIO_ENDPOINT");
        this.accessKey = getEnvOrThrow("MINIO_ACCESS_KEY");
        this.secretKey = getEnvOrThrow("MINIO_SECRET_KEY");
    }

    private String getEnvOrThrow(String key) {
        String value = System.getenv(key);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Missing required environment variable: " + key);
        }
        return value;
    }
    
}

