# 🗺️ Codebase & Functionality Index

This index maps the directory structures, files, and their architectural functionalities across the workspace.

## 📁 Repository 1: `fraud-processing-engine`

Spring Boot microservice running Project Loom virtual threads, Kafka streaming consumers, and the Temporal distributed Saga business workflow.

| File Path | Description / Functionality | Link |
| :--- | :--- | :--- |
| `pom.xml` | Maven Dependency Management Descriptor (Spring Boot, Kafka, Temporal, Resilience4j) | [pom.xml](file:///f:/code/learning/java/system-design/saga/fraud-processing-engine/pom.xml) |
| `Dockerfile` | Multi-Stage production JRE builder for Render web service deployment | [Dockerfile](file:///f:/code/learning/java/system-design/saga/fraud-processing-engine/Dockerfile) |
| `src/main/java/com/enterprise/architecture/config/KafkaConsumerConfig.java` | Pairs Kafka ingestion loop with lightweight Virtual Threads (Project Loom) | [KafkaConsumerConfig.java](file:///f:/code/learning/java/system-design/saga/fraud-processing-engine/src/main/java/com/enterprise/architecture/config/KafkaConsumerConfig.java) |
| `src/main/java/com/enterprise/architecture/security/EncryptionUtil.java` | Core AES-256 GCM symmetric encryption utility for securing sensitive payload fields | [EncryptionUtil.java](file:///f:/code/learning/java/system-design/saga/fraud-processing-engine/src/main/java/com/enterprise/architecture/security/EncryptionUtil.java) |
| `src/main/java/com/enterprise/architecture/security/TemporalDataConverter.java` | Scrambles Temporal variables in transit before storing them in database history logs | [TemporalDataConverter.java](file:///f:/code/learning/java/system-design/saga/fraud-processing-engine/src/main/java/com/enterprise/architecture/security/TemporalDataConverter.java) |
| `src/main/java/com/enterprise/architecture/components/FraudValidationProcessor.java` | `@KafkaListener` wrapped in a Resilience4j Circuit Breaker for backpressure clamp | [FraudValidationProcessor.java](file:///f:/code/learning/java/system-design/saga/fraud-processing-engine/src/main/java/com/enterprise/architecture/components/FraudValidationProcessor.java) |
| `src/main/java/com/enterprise/architecture/saga/OrderSagaWorkflow.java` | Interface defining the distributed Order Saga workflow contract | [OrderSagaWorkflow.java](file:///f:/code/learning/java/system-design/saga/fraud-processing-engine/src/main/java/com/enterprise/architecture/saga/OrderSagaWorkflow.java) |
| `src/main/java/com/enterprise/architecture/saga/OrderSagaWorkflowImpl.java` | Replay-safe state machine implementation coordinating Saga try-compensation blocks | [OrderSagaWorkflowImpl.java](file:///f:/code/learning/java/system-design/saga/fraud-processing-engine/src/main/java/com/enterprise/architecture/saga/OrderSagaWorkflowImpl.java) |
| `src/main/java/com/enterprise/architecture/saga/InventoryActivities.java` | Interface defining activities performed during Saga execution (e.g. Reserve, Release) | [InventoryActivities.java](file:///f:/code/learning/java/system-design/saga/fraud-processing-engine/src/main/java/com/enterprise/architecture/saga/InventoryActivities.java) |
| `src/main/java/com/enterprise/architecture/saga/InventoryActivitiesImpl.java` | Idempotent activity operations handling state queries and filtering out-of-order calls | [InventoryActivitiesImpl.java](file:///f:/code/learning/java/system-design/saga/fraud-processing-engine/src/main/java/com/enterprise/architecture/saga/InventoryActivitiesImpl.java) |

---

## 📁 Repository 2: `temporal-infra-cluster`

Lightweight Docker-based control plane to host and bootstrap the Temporal orchestrator instance.

| File Path | Description / Functionality | Link |
| :--- | :--- | :--- |
| `Dockerfile` | Pulls pre-compiled Temporal Engine Server container matching DB parameters | [Dockerfile](file:///f:/code/learning/java/system-design/saga/temporal-infra-cluster/Dockerfile) |
| `schema-migration.sh` | Shell script executing schema bootstrap routines on the PostgreSQL instance | [schema-migration.sh](file:///f:/code/learning/java/system-design/saga/temporal-infra-cluster/schema-migration.sh) |
