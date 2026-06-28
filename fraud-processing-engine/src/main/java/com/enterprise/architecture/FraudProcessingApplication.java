package com.enterprise.architecture;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main application entrypoint initiating our high-throughput Spring Boot
 * context.
 */
@SpringBootApplication
public class FraudProcessingApplication {
    public static void main(String[] args) {
        // Launches the microservice node pipeline
        SpringApplication.run(FraudProcessingApplication.class, args);
    }
}
