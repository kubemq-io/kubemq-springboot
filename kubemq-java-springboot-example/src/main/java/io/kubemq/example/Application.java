package io.kubemq.example;

import io.kubemq.example.cq.CQService;
import io.kubemq.example.pubsub.PubsubService;
import io.kubemq.example.queues.QueuesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"io.kubemq"})
public class Application implements CommandLineRunner {

    @Autowired
    private CQService cqService;

    @Autowired
    private PubsubService pubsubService;

    @Autowired
    private QueuesService queuesService;

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        // Example usage of CQService
        System.out.println("=== CQService Examples ===\n");
        cqService.ping();
        cqService.createCommandsChannel("commands_channel");
        cqService.createQueriesChannel("queries_channel");
        cqService.listCommandsChannels("commands");
        cqService.listQueriesChannels("queries");
        cqService.subscribeToCommands("commands_channel");
        cqService.sendCommandRequest("commands_channel");
        cqService.subscribeToQueries("queries_channel");
        cqService.sendQueryRequest("queries_channel");
        // Sleep for 3 seconds before, cancel, subscription, deleting channel and shutdown
        Thread.sleep(3 *1000);
        cqService.cancelSusbscription();
        cqService.deleteCommandsChannel("commands_channel");
        cqService.deleteQueriesChannel("queries_channel");
        cqService.shutdown();

        // Example usage of PubsubService
        System.out.println("\n=== PubsubService Examples ===\n");
        pubsubService.ping();
        pubsubService.createEventsChannel("events_channel");
        pubsubService.createEventsStoreChannel("events_store_channel");
        pubsubService.listEventsChannel("events");
        pubsubService.listEventsStoreChannel("events_store");
        pubsubService.subscribeToEvents("events_channel");
        pubsubService.subscribeToEventsStore("events_store_channel");
        pubsubService.sendEventMessage("events_channel");
        pubsubService.sendEventStoreMessage("events_store_channel");
        // Sleep for 3 seconds before, cancel, subscription, deleting channel and shutdown
        Thread.sleep(3 *1000);
        pubsubService.cancelSusbscription();
        pubsubService.deleteEventsChannel("events_channel");
        pubsubService.deleteEventsStoreChannel("events_store_channel");
        pubsubService.shutdown();

        // Example usage of QueuesService
        System.out.println("\n=== QueuesService Examples ===\n");
        queuesService.ping();
        queuesService.createQueueChannel("queue_channel");
        queuesService.listQueueChannels();
        queuesService.sendQueueMessage("queue_channel");
        queuesService.receiveQueuesMessages("queue_channel");
        queuesService.receiveAndBulkHandleQueueMessages("queue_channel");
        queuesService.getWaitingMessages("queue_channel");
        queuesService.getPullMessages("queue_channel");
         // Sleep for 3 seconds before, deleting channel and shutdown
        Thread.sleep(3 *1000);
        queuesService.deleteQueueChannel("queue_channel");
        queuesService.shutdown();
        
        System.out.println("\n\n=============== Example Execution Completed ===============");
    }
}
