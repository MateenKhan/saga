package com.enterprise.architecture.saga;

import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

/**
 * Top-level Orchestrator interface defining the core transaction sequence
 * boundary.
 */
@WorkflowInterface
public interface OrderSagaWorkflow {

    /**
     * Master entrypoint to execute our high-throughput distributed transaction.
     */
    @WorkflowMethod
    void executeOrderSaga(String orderId, String customerId, int itemId, int quantity);
}
