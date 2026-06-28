# 🏛️ Distributed Architecture & High-Throughput Saga Master Study Guide

This document captures the ultimate list of architectural challenges, edge cases, and technical solutions required to design, scale, and secure a **100,000 TPS Distributed Saga and Stream Processing Platform**.

---

## 🗺️ Part 1: Distributed Transactions & Eventual Consistency (The Saga Tier)

### Q1: The Dual-Write Dilemma
*   **Scenario:** An application needs to save an order to its local PostgreSQL database and notify a separate inventory service over the network. If it writes to the database first and crashes before sending the network notification, the system enters an inconsistent state.
*   **The Challenge:** How do you guarantee that a database commit and a network event notification both succeed or fail together without using slow, unscalable two-phase commits (2PC/XA)?
*   **Core Architectural Solution:** Implement the **Transactional Outbox Pattern** combined with **Change Data Capture (CDC)** via **Debezium**. Write both records into the same local database transaction, allowing Debezium to safely tail the write-ahead logs (WAL) and stream events asynchronously to Kafka.

### Q2: The Out-of-Order Compensation Race Condition
*   **Scenario:** A network partition causes a reservation request to freeze. The central orchestrator hits a timeout, assumes the step failed, and triggers a compensating transaction (Rollback) to cancel the order. Due to a multi-instance pod environment or network delays, the compensation request arrives *before* the original stuck reservation request.
*   **The Challenge:** When the slow, late zombie reservation request finally arrives at the downstream microservice *after* the cancellation has already processed, how do you prevent it from executing and leaking inventory data?
*   **Core Architectural Solution:** Implement a local **Pre-Commit State Table** inside the downstream service. The compensation step logs `STATUS = CANCELLED` first. When the late zombie request arrives, it checks this table, detects the out-of-order execution via an **Idempotency Check**, and safely discards itself.

### Q3: Concurrent Multi-Instance Shard Collisions
*   **Scenario:** Instance A is processing a delayed reservation write while Instance B is simultaneously processing an urgent cancellation request on the exact same database shard for the same Order ID.
*   **The Challenge:** How do you prevent these two separate microservice pod instances from overwriting each other's data or causing a race condition right at the database shard layer?
*   **Core Architectural Solution:** Enforce data-layer synchronization using **Optimistic Locking with a `@Version` Column** or a distributed traffic cop lock via **Redis (Redlock Pattern)**. If a version mismatch occurs, the late thread throws an exception and safely aborts.

---

## ⚡ Part 2: High-Throughput Streaming & Infrastructure Scaling (The Kafka Tier)

### Q4: Processing 100,000 TPS Without Memory Collapses
*   **Scenario:** A real-time Fraud Evaluation Engine must ingest financial events from Kafka, evaluate risk rules, and return a decision within a strict 50-millisecond budget under peak traffic.
*   **The Challenge:** How do you structure your Java application threads to handle 100,000 transactions per second safely without triggering out-of-memory (OOM) memory heap crashes or losing transaction event ordering?
*   **Core Architectural Solution:** Scale horizontally by linking your max thread concurrency directly to your **Kafka Partition Count** (e.g., 100 partitions). Use standard heavy Platform Threads solely for the low-overhead Kafka polling loops, and offload the blocking downstream I/O evaluations to unbounded, lightweight **Java Virtual Threads (Project Loom)**.

### Q5: Zero-Downtime Schema Evolution & Rollouts
*   **Scenario:** Six months into production, the business decides to completely rewrite the core transaction event payload schema (changing data types, adding, and removing fields).
*   **The Challenge:** How do you deploy this code update so that old legacy consumer pods and new upgraded consumer pods can run simultaneously in production without dropping packets, throwing serialization exceptions, or forcing a system blackout?
*   **Core Architectural Solution:** Implement the **Schema Registry Pattern** utilizing **Apache Avro or Protocol Buffers** configured to strict **FULL Compatibility**. The payload deserialization schemas are heavily cached in-memory directly inside the pods, eliminating slow, high-latency gRPC network hops on the critical path.

### Q6: The Thundering Herd Autoscaling Crash
*   **Scenario:** A 45-second downstream cache outage causes 4 million backlogged messages to pile up in your Kafka cluster. When the cache wakes up, your cloud infrastructure auto-scales your consumer pods from 25 to 100 nodes to clear the lag. Suddenly, your core sharded databases crash under 100% CPU utilization.
*   **The Challenge:** Why did scaling your compute tier crash your database, and how do you protect your finite data stores from being destroyed by a thundering herd of catch-up consumers?
*   **Core Architectural Solution:** Enforce strict **Backpressure Boundaries**. Clamp your database connection pools per pod down using **HikariCP maximum pool ceilings** (e.g., max 5 connections per pod). Balance this by slowing down your intake tap via Kafka's `max.poll.records` property, and wrap execution methods inside a **Resilience4j Circuit Breaker** to redirect overflow data to an **AWS S3 Dead Letter Queue (DLQ)**.

---

## 📊 Part 3: Distributed Observability & The Mechanics of Time

### Q7: The Mathematical Blindspot of Dashboard Averages
*   **Scenario:** Your global Grafana latency dashboard shows a perfectly healthy green metric hovering at a steady **15ms Mean (Average)**. However, your largest, highest-paying enterprise client calls your executive team furious because their checkouts are hanging for over **300ms**.
*   **The Challenge:** Why is a standard mathematical average completely blind to this failure, and how do you re-architect your data instrumentation to expose these edge cases instantly?
*   **Core Architectural Solution:** Banish the average metric and deploy **Histogram Quantiles (Percentiles: P50, P99, and P99.9)** via **Micrometer and Prometheus**. While the P50 tracks standard retail users, the P99.9 instantly isolates and exposes the worst-performing paths experienced by heavy enterprise bulk requests.

### Q8: The Time Paradox Error (Clock Skew & Drift)
*   **Scenario:** A central orchestrator cluster is scaled to 10 server nodes across multiple cloud availability zones. Server hardware quartz crystals drift apart over time due to variances in manufacturing or CPU operating temperatures, causing clocks to fall out of sync by hundreds of milliseconds.
*   **The Challenge:** If Server A logs that a transaction started according to its fast clock, but Server B logs that it completed according to its slow clock, it results in a negative duration value. How do you prevent these physical clock skews from breaking distributed timers and corrupting transaction history logs?
*   **Core Architectural Solution:** Implement a **Hybrid Time Architecture**. For system correctness and lock integrity across nodes, completely abandon physical wall-clock times. Rely strictly on **Logical Sequence Counters (Lamport Timestamps)** and single-worker shard ownership. For local, near-instant timeout measurements inside a single pod, enforce the use of **Monotonic Clocks (`System.nanoTime()`)**, which physically cannot go backward or jump.

### Q9: The Human Observability Gap
*   **Scenario:** You have completely banned physical machine clock times from your core database transaction logs and workflow orchestrator state engines to maintain 100% system safety against clock skews.
*   **The Challenge:** If a major production failure happens at 3:15 AM, and you have completely deleted clock times from your system logic, how do your human engineers know what happened and when?
*   **Core Architectural Solution:** Maintain a separate **Observability Lane**. Use **Network Time Protocol (NTP)** or Chrony daemons at the operating system layer to continuously slew and smooth out physical machine clocks across nodes. Stamp your logs with standard **UTC Wall-Clock Time (`System.currentTimeMillis()`)** alongside your immutable **Trace IDs and Logical Sequence Counters**, allowing humans to map out timelines perfectly without risking core data corruption.
