package com.enterprise.architecture.saga;

import java.io.Serializable;

// Using standard Java encapsulation to support rapid sharded serialization pools
public class TransactionState implements Serializable {
    private String orderId;
    private String status;
    private Long version; // Supports optimistic locking version evaluations

    public TransactionState() {
    }

    public TransactionState(String orderId, String status, Long version) {
        this.orderId = orderId;
        this.status = status;
        this.version = version;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }
}
