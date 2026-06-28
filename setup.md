
---

## 📋 Prerequisites

Before initiating cloud provisioning, create two separate source code repositories on **GitHub**:
1. **Repository 1 (`fraud-processing-engine`):** Contains your Java Spring Boot microservice, Project Loom virtual thread consumers, and business logic.
2. **Repository 2 (`temporal-infra-cluster`):** Contains a lightweight deployment `Dockerfile` to pull and boot the official Temporal engine.

---

## 🛠️ Step-by-Step Cloud Provisioning Blueprint

### 🗄️ Step 1: Deploy the Shared Persistence Layer (PostgreSQL)
The Temporal Central Server requires an ACID-compliant datastore to track and freeze transaction history logs.

1. Navigate to your **Render Dashboard**, click **New +** in the top right menu, and select **PostgreSQL**.
2. Apply the following parameters:
   * **Name:** `temporal-persistence-db`
   * **Database Name:** `temporal`
   * **User:** `temporal_admin`
   * **Instance Type:** Select **Free** *(Allocates 1 GB Storage & 256 MB RAM)*
3. Click **Create Database**.
4. Once the status shows **Available**, scroll down to the **Connections** panel and copy the **Internal Database URL** (e.g., `postgresql://temporal_admin:pass@internal-host:5432/temporal`). 
   * *Note: This private routing prevents public database access.*

---

### ⏱️ Step 2: Provision the Central Temporal Orchestrator
This service runs the central control plane that executes our Saga retry loops, manages timeouts, and processes replay logs.

1. Click **New +** on the dashboard and select **Web Service**.
2. Connect your GitHub account and select your **`temporal-infra-cluster`** repository.
3. Configure the engine limits:
   * **Name:** `temporal-central-server`
   * **Runtime:** Select **Docker**.
   * **Instance Type:** Select **Free**.
4. Scroll to the **Environment Variables** panel and inject your private persistence credentials:
   * `DB` = `postgres`
   * `POSTGRES_SEEDS` = `[Paste the Internal Database URL copied in Step 1]`
5. Click **Create Web Service**. 
6. Render will build the cluster container, expose it to your private network mesh, and listen at:
   `http://temporal-central-server:7233`

---

### ⚡ Step 3: Deploy the High-Throughput Stream Pipeline (Apache Kafka)
This broker processes the 100,000 TPS financial event pipeline.

1. Click **New +** on the dashboard and select **Web Service**.
2. Click **Build from a Docker Image** and type the official verified hub path: `ubuntu/kafka:latest`
3. Configure the platform boundary:
   * **Name:** `kafka-cluster-broker`
   * **Instance Type:** Select **Free**.
4. Add the following **Environment Variables** to bind listeners to Render's private routing mesh:
   * `KAFKA_LISTENER_SECURITY_PROTOCOL_MAP` = `INTERNAL:PLAINTEXT`
   * `KAFKA_LISTENERS` = `INTERNAL://0.0.0.0:9042`
   * `KAFKA_ADVERTISED_LISTENERS` = `INTERNAL://kafka-cluster-broker:9042`
5. Click **Create Web Service**.

---

### ☕ Step 4: Boot the Java Spring Boot Engine (Processing Core)
This container hosts your Java code, manages Project Loom virtual threads, and connects your app to your cloud infrastructure.

1. Click **New +** on the dashboard and select **Web Service**.
2. Connect your GitHub account and select your **`fraud-processing-engine`** repository.
3. Configure the compute layer:
   * **Name:** `fraud-evaluation-service`
   * **Runtime:** Select **Docker** (Ensure your repository has a valid multi-stage `Dockerfile`).
   * **Instance Type:** Select **Free** *(Allocates 512 MB RAM)*.
4. Open the **Environment Variables** sub-menu and wire your Spring application properties directly to your running dependencies:
   * `KAFKA_BOOTSTRAP_SERVERS` = `kafka-cluster-broker:9042`
   * `TEMPORAL_SERVER_ADDRESS` = `temporal-central-server:7233`
5. Click **Create Web Service**.

---

## 🚨 Critical Sandbox Constraints & Operations Guardrails

Because you are hosting this enterprise-grade platform on a 100% free developer account tier, keep these automated platform constraints in mind during live testing:

* **💤 The 15-Minute Auto-Sleep Rule:** If your `fraud-evaluation-service` endpoint receives zero transaction traffic for 15 consecutive minutes, Render automatically spins down (freezes) your container pod. The next time you trigger a test request, you will experience a **30 to 60-second cold-start delay** while the worker wakes up and re-establishes network connections.
* **⏳ The 30-Day Database Life Cycle:** Render's free tier PostgreSQL databases automatically **expire and delete themselves 30 days after creation**. This window is sufficient for feature testing and validation, but ensure your data configuration scripts are backed up locally.
* **📉 Compute Restrictions:** With 512 MB of RAM, heavy database caching or large in-memory message buffering will trigger an Out Of Memory (OOM) crash. Always keep your Kafka consumer batch limits turned down (`max.poll.records = 50`) to keep allocations stable.