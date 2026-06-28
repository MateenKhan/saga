# 🗺️ Master Cloud Architecture Execution Roadmap (`EXECUTION.md`)

This roadmap defines the precise file creation and deployment sequence for our high-throughput, resilient distributed architecture on **Render**. 

To survive within Render's free tier limits (**512 MB RAM per service**, **256 MB RAM for PostgreSQL**), we use a strict **Infrastructure-First** approach. We spin up and configure our cloud platform layers before deploying our application code.

---

## 🧭 The Blueprint Sequence


## 🧱 PHASE 1: Repository 2 (`temporal-infra-cluster`) Setup
This repository contains the infrastructure descriptors needed to launch the central control plane engine.

### 📄 File 1: `Dockerfile` (The Central Server Wrapper)
*   **Path:** `temporal-infra-cluster/Dockerfile`
*   **Purpose:** Pulls the official, pre-compiled Temporal server container. It is configured to receive and parse the internal PostgreSQL connection strings injected into the Render dashboard environment.

### 📄 File 2: `schema-migration.sh` (The Database Bootstrapper)
*   **Path:** `temporal-infra-cluster/schema-migration.sh`
*   **Purpose:** Automatically executes upon container startup. It provisions the required relational system state tables inside your Free Render PostgreSQL database instance before the server begins listening for workers.

---

## 🔒 PHASE 2: Repository 1 (`fraud-processing-engine`) - Security Core
We establish data-layer cryptographic protection before writing any business logic or routing streaming threads.

### 📄 File 3: `EncryptionUtil.java` (AES-256 GCM Base Engine)
*   **Path:** `src/main/java/com/enterprise/architecture/security/EncryptionUtil.java`
*   **Purpose:** Houses the core mathematical cryptographic operations. It uses AES-256 in Galois/Counter Mode (GCM) with random Initialization Vectors (IV) to protect data integrity at the byte level.

### 📄 File 4: `TemporalDataConverter.java` (Cryptographic Data Interceptor)
*   **Path:** `src/main/java/com/enterprise/architecture/security/TemporalDataConverter.java`
*   **Purpose:** Binds the encryption engine directly to Temporal's serialization payload hooks. It ensures all application variables sent across the cloud mesh are completely scrambled before touching the database log.

---

## ⚡ PHASE 3: Repository 1 (`fraud-processing-engine`) - High-Throughput Ingestion
We build the streaming pipeline, enforcing strict memory boundaries to match our 512 MB RAM cloud limits.

### 📄 File 5: `KafkaConsumerConfig.java` (Project Loom Virtual Thread Router)
*   **Path:** `src/main/java/com/enterprise/architecture/config/KafkaConsumerConfig.java`
*   **Purpose:** Pairs Kafka message ingestion with Project Loom virtual threads. It enforces `max.poll.records = 50` and tight database connection pool sizes to eliminate out-of-memory (OOM) crashes under heavy backlogs.

### 📄 File 6: `components/FraudValidationProcessor.java` (The Protected Consumer)
*   **Path:** `src/main/java/com/enterprise/architecture/components/FraudValidationProcessor.java`
*   **Purpose:** The actual `@KafkaListener` implementation. It uses a fast cache-aside fallback pattern, wrapped inside Resilience4j circuit breakers to handle downstream database or cache slowness.

---

## ⏱️ PHASE 4: Repository 1 (`fraud-processing-engine`) - Distributed Saga Core
We deploy the long-running orchestrator business logic to handle transaction consistency.

### 📄 File 7: `saga/OrderSagaWorkflow.java` & `saga/InventoryActivities.java`
*   **Path:** `src/main/java/com/enterprise/architecture/saga/[Interfaces]`
*   **Purpose:** Decoupled business interface descriptors required by the Temporal compiler to safely build proxy stubs.

### 📄 File 8: `saga/OrderSagaWorkflowImpl.java` (The Replay-Safe State Machine)
*   **Path:** `src/main/java/com/enterprise/architecture/saga/OrderSagaWorkflowImpl.java`
*   **Purpose:** The core Try-Catch business workflow sequence. It utilizes `Workflow.getVersion` to protect against deployment change traps during updates.

### 📄 File 9: `saga/InventoryActivitiesImpl.java` (Idempotency Execution Core)
*   **Path:** `src/main/java/com/enterprise/architecture/saga/InventoryActivitiesImpl.java`
*   **Purpose:** Executes downstream database adjustments. It uses local check-before-update state queries to catch and discard late, out-of-order zombie network requests.

### 📄 File 10: `Dockerfile` (Multi-Stage Java Production Wrapper)
*   **Path:** `fraud-processing-engine/Dockerfile`
*   **Purpose:** A production-grade multi-stage container file. It compiles your Java source files via Maven, discards the heavy build tools, and runs the optimized executable Spring Boot JAR inside Render's compute space.