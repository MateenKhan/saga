# Use the official, highly-optimized minimal Apache Kafka image
FROM apache/kafka:3.7.0

USER root

# Expose Kafka's internal private cluster broker port
EXPOSE 9042

# Tightly clamp Kafka's internal JVM Heap memory so it cannot exceed Render's 512MB limit
ENV KAFKA_JVM_PERFORMANCE_OPTS="-Xmx256M -Xms256M -XX:+UseG1GC -XX:+UseStringDeduplication"

# Configure standard KRaft variables to execute a single-node cluster without ZooKeeper
ENV KAFKA_NODE_ID=1
ENV KAFKA_PROCESS_ROLES=broker,controller
ENV KAFKA_CONTROLLER_QUORUM_VOTERS=1@127.0.0.1:9093
ENV KAFKA_CONTROLLER_LISTENER_NAMES=CONTROLLER

# Bind Kafka's listeners explicitly to your Render internal network space
ENV KAFKA_LISTENERS=PLAINTEXT://0.0.0.0:9042,CONTROLLER://0.0.0.0:9093
ENV KAFKA_ADVERTISED_LISTENERS=PLAINTEXT://kafka-cluster-broker:9042
ENV KAFKA_LISTENER_SECURITY_PROTOCOL_MAP=PLAINTEXT:PLAINTEXT,CONTROLLER:PLAINTEXT
ENV KAFKA_INTER_BROKER_LISTENER_NAME=PLAINTEXT
