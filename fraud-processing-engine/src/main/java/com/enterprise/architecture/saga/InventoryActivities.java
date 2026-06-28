package com.enterprise.architecture.saga;

import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;

/**
 * Interface defining individual microservice network activities managed by the
 * Saga.
 */
@ActivityInterface
public interface InventoryActivities {

    /**
     * Deducts item stock from the sharded database inventory tables.
     */
    @ActivityMethod
    void reserveInventory(String orderId, int itemId, int quantity);

    /**
     * COMPENSATING TRANSACTION: Reverts inventory adjustments if subsequent steps
     * fail.
     */
    @ActivityMethod
    void compensateInventory(String orderId, int itemId, int quantity);
}
