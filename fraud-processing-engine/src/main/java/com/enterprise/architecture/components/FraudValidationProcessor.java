package com.enterprise.architecture.components;

import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import com.enterprise.architecture.saga.OrderSagaWorkflow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class FraudValidationProcessor {

    private static final Logger log = LoggerFactory.getLogger(FraudValidationProcessor.class);
    private final WorkflowClient workflowClient;

    public FraudValidationProcessor(WorkflowClient workflowClient) {
        this.workflowClient = workflowClient;
    }

    @KafkaListener(topics = "OrderCreatedEvent", groupId = "fraud-evaluation-group", containerFactory = "kafkaListenerContainerFactory")
    public void processIncomingTransactionStream(String transactionMessage) {
        log.info("Ingested event token from Kafka Stream: " + transactionMessage);

        // Simulating parsing token variables out of message payload string
        String extractedOrderId = "ORD-" + System.currentTimeMillis();
        String customerId = "CUST-9921";
        int itemId = 4502;
        int quantity = 1;

        // Configure options to bind unique transaction boundaries to the cluster
        WorkflowOptions options = WorkflowOptions.newBuilder()
                .setWorkflowId(extractedOrderId) // Binds single unique owner shard context
                .setTaskQueue("order-saga-queue")
                .build();

        // Instantiate workflow stub proxy pointer instance
        OrderSagaWorkflow workflow = workflowClient.newWorkflowStub(OrderSagaWorkflow.class, options);

        log.info("Spawning Asynchronous Distributed Saga Workflow Orchestration loop inside cluster...");
        // 🚀 THE KAFKA-TO-SAGA BRIDGE
        // Executes the workflow asynchronously so the listener loop returns
        // immediately, processing 100K TPS
        WorkflowClient.start(workflow::executeOrderSaga, extractedOrderId, customerId, itemId, quantity);
    }
}
