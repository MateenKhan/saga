package com.enterprise.architecture.saga;

import io.temporal.activity.ActivityOptions;
import io.temporal.common.RetryOptions;
import io.temporal.workflow.Workflow;
import org.slf4j.Logger;
import java.time.Duration;

/**
 * Replay-safe, strictly deterministic Saga Orchestration State Machine.
 * Manages distributed commit steps and triggers compensating transactions upon
 * failures.
 */
public class OrderSagaWorkflowImpl implements OrderSagaWorkflow {

    private static final Logger log = Workflow.getLogger(OrderSagaWorkflowImpl.class);

    // 🛡️ Bounding downstream timeouts and exponential retries to prevent thread
    // lockups
    private final InventoryActivities inventoryActivities = Workflow.newActivityStub(
            InventoryActivities.class,
            ActivityOptions.newBuilder()
                    .setStartToCloseTimeout(Duration.ofSeconds(5)) // Protects against infinite microservice locks
                    .setRetryOptions(RetryOptions.newBuilder()
                            .setInitialInterval(Duration.ofSeconds(1)) // Retry initial backoff gap
                            .setBackoffCoefficient(2.0) // Exponential multiply factor: 1s, 2s, 4s...
                            .setMaximumInterval(Duration.ofSeconds(30)) // Cap maximum backoff ceiling
                            .setMaximumAttempts(5) // Relinquish execution completely after 5 tries
                            .build())
                    .build());

    @Override
    public void executeOrderSaga(String orderId, String customerId, int itemId, int quantity) {
        // CRITICAL REPLAY ENGINE RULE: Code within this block MUST be perfectly
        // deterministic.
        // Never invoke native System.currentTimeMillis() or UUID.randomUUID() here.

        boolean inventoryReservedSuccessfully = false;

        try {
            log.info("Initiating Distributed Saga Orchestration for Order: " + orderId);

            // 🚀 STEP 1: Execute initial business step (Stock Reservation)
            inventoryActivities.reserveInventory(orderId, itemId, quantity);
            inventoryReservedSuccessfully = true;

            // 🛑 UPGRADE VERIFICATION EXAMPLE (DEPLOYMENT TRAP ENGINE DEFENSE)
            // Using Workflow.getVersion to securely evolve logic without breaking active
            // replays
            int logicVersion = Workflow.getVersion("FraudScoringRollout", Workflow.DEFAULT_VERSION, 1);
            if (logicVersion == 1) {
                log.info("Executing New Feature Logic: Running inline fraud scoring checks.");
                // futureFeatureActivities.evaluateAdvancedFraudProfile(customerId);
            }

            log.info("Saga Transaction Completed Successfully for Order: " + orderId);

        } catch (Exception SagasException) {
            log.error("CRITICAL: Saga transaction breakdown detected! Initiating Rollback Sequences. Reason: "
                    + SagasException.getMessage());

            // Invoke the compensating vector to clean up data states across the cluster
            // shards
            executeCompensations(inventoryReservedSuccessfully, orderId, itemId, quantity);

            // Re-throw exception to signal to the central Temporal Cluster that the
            // workflow failed
            throw SagasException;
        }
    }

    private void executeCompensations(boolean inventoryReserved, String orderId, int itemId, int quantity) {
        if (inventoryReserved) {
            try {
                log.info("Compensating Step: Reverting Stock Reservation for Order: " + orderId);
                inventoryActivities.compensateInventory(orderId, itemId, quantity);
            } catch (Exception compensationError) {
                log.error(
                        "FATAL: Compensating activity crashed! Human operations intervention required immediately. Context: "
                                + orderId);
                throw compensationError;
            }
        }
    }
}
