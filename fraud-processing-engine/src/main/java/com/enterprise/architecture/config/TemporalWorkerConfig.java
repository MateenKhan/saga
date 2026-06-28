package com.enterprise.architecture.config;

import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowClientOptions;
import io.temporal.common.converter.DefaultDataConverter;
import io.temporal.serviceclient.WorkflowServiceStubs;
import io.temporal.serviceclient.WorkflowServiceStubsOptions;
import io.temporal.worker.Worker;
import io.temporal.worker.WorkerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.enterprise.architecture.security.TemporalDataConverter;
import com.enterprise.architecture.saga.OrderSagaWorkflowImpl;
import com.enterprise.architecture.saga.InventoryActivities;
import org.springframework.boot.CommandLineRunner;

@Configuration
public class TemporalWorkerConfig {

    @Value("${temporal.server-address}")
    private String serverAddress;

    @Value("${temporal.task-queue}")
    private String taskQueue;

    @Bean
    public WorkflowServiceStubs workflowServiceStubs() {
        return WorkflowServiceStubs.newServiceStubs(
                WorkflowServiceStubsOptions.newBuilder()
                        .setTarget(serverAddress) // Connects over the private network mesh
                        .build());
    }

    @Bean
    public WorkflowClient workflowClient(WorkflowServiceStubs serviceStubs) {
        // 🔒 SECURITY GATE: Register the cryptographic payload interceptor
        // Scrambles transaction variables before writing records to PostgreSQL logs
        WorkflowClientOptions options = WorkflowClientOptions.newBuilder()
                .setDataConverter(
                        DefaultDataConverter.newWithExtraCodecs(new TemporalDataConverter()))
                .build();

        return WorkflowClient.newInstance(serviceStubs, options);
    }

    @Bean
    public CommandLineRunner bootstrapTemporalWorkerPool(
            WorkflowClient workflowClient,
            InventoryActivities inventoryActivitiesInstance) {

        return args -> {
            // Instantiate factory listeners linked directly to the orchestration engine
            WorkerFactory factory = WorkerFactory.newInstance(workflowClient);
            Worker worker = factory.newWorker(taskQueue);

            // Register deterministic state workflow blueprint
            worker.registerWorkflowImplementationTypes(OrderSagaWorkflowImpl.class);

            // Register Spring-managed activity bean injection instance
            worker.registerActivitiesImplementations(inventoryActivitiesInstance);

            // Boot the background execution polling threads loops
            factory.start();
            System.out.println("=== Temporal Java Worker Mesh Pool Started Successfully ===");
        };
    }
}
