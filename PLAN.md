# 🗺️ Master Cloud Engineering Runbook (`PLAN.md`)

This master runbook outlines the exact sequence to register, configure, structure, and deploy a high-throughput, transaction-safe Fraud Processing Engine and distributed Saga orchestrator on **Render** for free.

---

## 🧭 STEP 1: External Platform Registrations
Execute these steps before setting up your infrastructure or writing code.

   * Go to [render.com](https://render.com) and click **Sign Up**.
   * Fork this repo and to link this repo to your render account.

---
### 🗄️ Step 2: Deploy the Shared Persistence Layer (PostgreSQL)
1. Navigate to your **Render Dashboard**, click **New +** in the top right, and select **PostgreSQL**.
2. Input the exact values:
   * **Name:** `temporal-persistence-db`
   * **Database Name:** `temporal`
   * **User:** `temporal_admin`
   * **Instance Type:** Select **Free**
3. Click **Create Database**.
4. Wait for status **Available**. Scroll down to **Connections** and copy the value for **Internal Database URL**.

---

### ⏱️ Step 3: Provision the Central Temporal Orchestrator Server
1. Click **New +** on the Render dashboard and select **Web Service**.
2. Select your linked **`temporal-infra-cluster`** repository.
3. Input the parameters:
   * **Name:** `temporal-central-server`
   * **Runtime:** Select **Docker**.
   * **Instance Type:** Select **Free**.
4. Open **Environment Variables** and add:
   * `DB` = `postgres`
   * `DB_HOST` = `[Paste your Internal Database Hostname]`
   * `DB_PORT` = `5432`
   * `DB_USER` = `temporal_admin`
   * `DB_PWD` = `[Paste your Database Password]`
4. Open **Settings** and edit:
   * **Name** = `temporal-central-server`
   * **Root Directory** = `temporal-infra-cluster`
   * **Dockerfile Path** = `temporal-infra-cluster/Dockerfile`
   * **Docker Build Context Directory** = `temporal-infra-cluster/.`
5. Click **Create Web Service**.

### ⚡ Step 4: Deploy the Stream Pipeline (Apache Kafka)
1. Click **New +** on the dashboard and select **Web Service**.
2. Select **Build from a Docker Image** and type: `ubuntu/kafka:latest`
3. Input the parameters:
*  **Name** = `kafka-cluster-broker`
   * **Root Directory** = `kafka-cluster-broker`
   * **Dockerfile Path** = `Dockerfile`
4. Click **Create Web Service**.

### ☕ Step 5: Boot the Java Spring Boot Processing Engine
1. Click **New +** on the dashboard and select **Web Service**.
2. Select your linked **`fraud-processing-engine`** repository.
3. Input the parameters:
   * **Name:** `fraud-evaluation-service`
   * **Root Directory:** `fraud-evaluation-service`
   * **Runtime:** Select **Docker**.
   * **Instance Type:** Select **Free**.
   * **Advanced -> Dockerfile Path:** Select **Dockerfile**.
4. Open **Environment Variables** and add:
   * `KAFKA_BOOTSTRAP_SERVERS` = `kafka-cluster-broker:9042`
   * `TEMPORAL_SERVER_ADDRESS` = `temporal-central-server:7233`
   * `DB_URL` = `jdbc:postgresql://dpg-d909ol8k1i2s73ffl560-`
   
5. Click **Create Web Service**.

---

### ☕ Step 6: Logs
1. * Go to [grafana.com](https://grafana.com/) and click **Sign Up**.
2. new datasource -> prometheus -> addnew datasource
3. Input the parameters:
   * **Name:** `fraud-evaluation-service`
   * **Runtime:** Select **Docker**.
   * **Instance Type:** Select **Free**.
   * **Advanced -> Dockerfile Path:** Select **Dockerfile**.
4. Open **Environment Variables** and add:
   * `KAFKA_BOOTSTRAP_SERVERS` = `kafka-cluster-broker:9042`
   * `TEMPORAL_SERVER_ADDRESS` = `temporal-central-server:7233`
   * `DB_URL` = `jdbc:postgresql://dpg-d909ol8k1i2s73ffl560-`
   
5. Click **Create Web Service**.

---

## 🚀 PHASE 4: Local Git & Execution Commands

### Local Code Setup & Synchronization
Execute these terminal commands inside your local project machine to commit changes and trigger automated cloud builds on Render:

```bash
# 1. Initialize and Push Infrastructure Cluster (Repository 2)
cd path/to/temporal-infra-cluster
git init
git add .
git commit -m "feat: infrastructure deployment core"
git remote add origin https://github.comyour-username/temporal-infra-cluster.git
git branch -M main
git push -u origin main

# 2. Initialize and Push Spring Boot Processing Engine (Repository 1)
cd path/to/fraud-processing-engine
git init
git add .
git commit -m "feat: system business logic and loom consumers"
git remote add origin https://github.comyour-username/fraud-processing-engine.git
git branch -M main
git push -u origin main
```
