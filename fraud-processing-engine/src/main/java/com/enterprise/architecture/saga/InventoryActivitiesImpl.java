package com.enterprise.architecture.saga;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class InventoryActivitiesImpl implements InventoryActivities {

    private static final Logger log = LoggerFactory.getLogger(InventoryActivitiesImpl.class);
    private final TransactionStateRepository stateRepository;

    // Inject repository via constructor
    public InventoryActivitiesImpl(TransactionStateRepository stateRepository) {
        this.stateRepository = stateRepository;
    }

    @Override
    public void reserveInventory(String orderId, int itemId, int quantity) {
        log.info("Saga Activity: Processing Stock Reservation for Order: {}", orderId);

        // Active State Lookup Check
        TransactionState currentState = stateRepository.findByOrderId(orderId);

        if (currentState != null && "CANCELLED".equals(currentState.getStatus())) {
            log.warn("ZOMBIE ATTACK CAUGHT: Discarding late reservation request for cancelled Order: {}", orderId);
            return;
        }

        if (currentState != null && "RESERVED".equals(currentState.getStatus())) {
            log.info("Idempotent bypass: Order {} already allocated.", orderId);
            return;
        }

        // Mutate and save local state
        stateRepository.save(new TransactionState(orderId, "RESERVED", 1L));
        log.info("Stock reserved cleanly for Order: {}", orderId);
    }

    @Override
    public void compensateInventory(String orderId, int itemId, int quantity) {
        log.info("Saga Compensation: Reverting Stock Reservation for Order: {}", orderId);

        TransactionState currentState = stateRepository.findByOrderId(orderId);

        // Compensation arrived FIRST before reservation
        if (currentState == null) {
            log.warn("Out-Of-Order Match: Writing CANCELLED shield state for Order: {}", orderId);
            stateRepository.save(new TransactionState(orderId, "CANCELLED", 1L));
            return;
        }

        if ("CANCELLED".equals(currentState.getStatus())) {
            log.info("Idempotent compensation bypass for Order: {}", orderId);
            return;
        }

        stateRepository.save(new TransactionState(orderId, "CANCELLED", currentState.getVersion() + 1));
        log.info("Stock returned cleanly for Order: {}", orderId);
    }
}
