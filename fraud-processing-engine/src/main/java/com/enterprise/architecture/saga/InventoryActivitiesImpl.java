package com.enterprise.architecture.saga;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Downstream execution engine handling local database adjustments.
 * Enforces strict transaction isolation and idempotency checks to defeat
 * out-of-order race conditions.
 */
@Service
public class InventoryActivitiesImpl implements InventoryActivities {

    private static final Logger log = LoggerFactory.getLogger(InventoryActivitiesImpl.class);

    // In production, inject your actual sharded Spring Data JPA/Hibernate
    // repository here
    // private final InventoryStateRepository stateRepository;

    @Override
    @Transactional
    public void reserveInventory(String orderId, int itemId, int quantity) {
        log.info("Activity Started: Processing Stock Reservation for Order: {} | Item: {}", orderId, itemId);

        // 🛡️ STEP 1: ANTI-OUT-OF-ORDER IDEMPOTENCY CHECK
        // Query your local transaction status table before touching stock balances
        String currentState = queryLocalTransactionStateTable(orderId);

        if ("CANCELLED".equals(currentState)) {
            log.warn(
                    "CRITICAL BOUNDARY ENCOUNTERED: Caught late network zombie reservation request for Order: {}. Dropping query immediately to prevent data corruption.",
                    orderId);
            return; // Exit safely. The compensation arrived first; do not modify stock!
        }

        if ("RESERVED".equals(currentState)) {
            log.info("Idempotent Retry Detected: Order {} already processed. Skipping duplicate execution loop.",
                    orderId);
            return;
        }

        // STEP 2: MUTATE STOCK BALANCES
        log.info("Deducting {} units from Item Shard ID: {}", quantity, itemId);
        // executeStockDeductionSQL(itemId, quantity);

        // STEP 3: LOG THE REPLAY STATE
        saveLocalTransactionState(orderId, "RESERVED");
        log.info("Activity Completed Successfully: Stock allocated cleanly for Order: {}", orderId);
    }

    @Override
    @Transactional
    public void compensateInventory(String orderId, int itemId, int quantity) {
        log.info("Compensation Activity Started: Rolling back Stock Reservation for Order: {}", orderId);

        String currentState = queryLocalTransactionStateTable(orderId);

        // 🛡️ STEP 1: CONCURRENT RACE CONDITION SHIELD
        // If the compensation arrives FIRST because the reservation is stuck on a slow
        // router
        if (currentState == null) {
            log.warn(
                    "OUT-OF-ORDER WARNING: Compensation arrived before original reservation for Order: {}. Writing CANCELLED shield record to database state table.",
                    orderId);
            saveLocalTransactionState(orderId, "CANCELLED");
            return; // Exit safely. When the late reservation finally arrives, Step 1 of
                    // reserveInventory will drop it!
        }

        if ("CANCELLED".equals(currentState)) {
            log.info("Idempotent Compensation Retry: Order {} already rolled back. Exiting cleanly.", orderId);
            return;
        }

        // STEP 2: REVERT STOCK MULTIPLIERS
        log.info("Restoring {} units back to Item Shard ID: {}", quantity, itemId);
        // executeStockRestorationSQL(itemId, quantity);

        // STEP 3: UPDATE THE STATE
        saveLocalTransactionState(orderId, "CANCELLED");
        log.info("Compensation Activity Completed: Stock safely returned for Order: {}", orderId);
    }

    // SIMULATED DATA-LAYER STORAGE PRIMITIVES FOR COMPILATION
    private String queryLocalTransactionStateTable(String orderId) {
        // Simulated select status from transaction_states where order_id = orderId;
        return null;
    }

    private void saveLocalTransactionState(String orderId, String status) {
        // Simulated insert into/update transaction_states values (orderId, status);
    }
}
