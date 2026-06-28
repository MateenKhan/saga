package com.enterprise.architecture.config;

/**
 * KafkaConsumerConfig - Project Loom Thread Config
 * 
 * Purpose: Pairs Kafka message ingestion with Project Loom virtual threads.
 * Enforces max.poll.records = 50 and tight database connection pool sizes to eliminate
 * out-of-memory (OOM) crashes under heavy backlogs within Render's free tier.
 */
public class KafkaConsumerConfig {
    // Config logic to be added
}
