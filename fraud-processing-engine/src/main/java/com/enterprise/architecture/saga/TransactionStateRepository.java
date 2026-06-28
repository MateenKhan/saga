package com.enterprise.architecture.saga;

import org.springframework.stereotype.Repository;
import java.util.concurrent.ConcurrentHashMap;

/**
 * High-performance sharded memory datastore simulator.
 * Holds exact transactional states to allow your activity layers to evaluate
 * idempotency logic.
 */
@Repository
public class TransactionStateRepository {

    // Simulates an un-throttled sharded execution partition index map
    private final ConcurrentHashMap<String, TransactionState> databaseMockMap = new ConcurrentHashMap<>();

    public TransactionState findByOrderId(String orderId) {
        return databaseMockMap.get(orderId);
    }

    public void save(TransactionState state) {
        databaseMockMap.put(state.getOrderId(), state);
    }
}
