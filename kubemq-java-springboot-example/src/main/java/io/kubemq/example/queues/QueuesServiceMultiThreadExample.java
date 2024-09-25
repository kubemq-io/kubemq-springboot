package io.kubemq.example.queues;

import io.kubemq.sdk.common.ServerInfo;
import io.kubemq.sdk.queues.QueueMessage;
import io.kubemq.sdk.queues.QueueMessageReceived;
import io.kubemq.sdk.queues.QueueSendResult;
import io.kubemq.sdk.queues.QueuesClient;
import io.kubemq.sdk.queues.QueuesPollRequest;
import io.kubemq.sdk.queues.QueuesPollResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Example class demonstrating how to work with KubeMQ queue.
 */
@Service
public class QueuesServiceMultiThreadExample {

    @Autowired
    private QueuesClient queuesClient;
    private ExecutorService workerPool;

    public QueuesServiceMultiThreadExample(){
        // Create a thread pool with a fixed number of workers
        this.workerPool = Executors.newFixedThreadPool(10);
    }

    /**
     * Ping to test Connection is successful
     */
    public void ping() {
        System.out.println("Executing ping...");

        ServerInfo pingResult = queuesClient.ping();
        System.out.println("Ping Response: " + pingResult.toString());
    }

    /**
     * Sends 20 messages to the queue.
     */
    public void sendQueueMessage(String channelName) {
        System.out.println("\n============================== Send Queue Messages Started =============================\n");
        try {
            for (int i = 0; i < 10; i++) {
                Map<String, String> tags = new HashMap<>();
                tags.put("tag1", "kubemq");
                tags.put("tag2", "kubemq2");

                QueueMessage message = QueueMessage.builder()
                        .body(("Message " + (i + 1)).getBytes())  // Unique message body
                        .channel(channelName)
                        .metadata("Sample metadata " + (i + 1))
                        .id(UUID.randomUUID().toString())
                        .tags(tags)
                        .delayInSeconds(1)
                        .expirationInSeconds(3600)
                        .attemptsBeforeDeadLetterQueue(3)
                        .deadLetterQueue("dlq-" + channelName)
                        .build();

                QueueSendResult sendResult = queuesClient.sendQueuesMessage(message);
                System.out.println("Message " + (i + 1) + " sent with ID: " + sendResult.getId());
            }
        } catch (RuntimeException e) {
            System.err.println("Failed to send queue messages: " + e.getMessage());
        }
    }

    /**
     * Receives messages from the queue and processes them in multiple threads.
     */
    public void receiveQueuesMessages(String channelName) {
        try {
            QueuesPollRequest queuesPollRequest = QueuesPollRequest.builder()
                    .channel(channelName)
                    .pollMaxMessages(10)  // Pull 10 messages at a time
                    .pollWaitTimeoutInSeconds(10)
                    .build();

            QueuesPollResponse pollResponse = queuesClient.receiveQueuesMessages(queuesPollRequest);

            System.out.println("Received Message Response:");
            System.out.println("RefRequestId: " + pollResponse.getRefRequestId());
            System.out.println("ReceiverClientId: " + pollResponse.getReceiverClientId());
            System.out.println("TransactionId: " + pollResponse.getTransactionId());

            if (pollResponse.isError()) {
                System.out.println("Error: " + pollResponse.getError());
            } else {
                pollResponse.getMessages().forEach(msg -> {
                    // Submit each message to a separate worker thread for processing
                    workerPool.submit(() -> processMessage(msg));
                });
            }

        } catch (RuntimeException e) {
            System.err.println("Failed to receive queue messages: " + e.getMessage());
        }
        System.out.println("All Polled message processed successfully");
    }

    /**
     * Processes a single message in a separate thread.
     */
    private void processMessage(QueueMessageReceived msg) {
        try {
            System.out.println("Processing Message ID: " + msg.getId());
            System.out.println("Message Body: " + new String(msg.getBody()));

            // Simulate job processing, do some real world work to mark it success or fail
            boolean jobSuccess = ThreadLocalRandom.current().nextBoolean(); // Here i am sending true/false randomly

            if (jobSuccess) {
                msg.ack();
                System.out.println("Message ID: " + msg.getId() + " acknowledged.");
            } else {
                msg.reject();
                System.out.println("Message ID: " + msg.getId() + " rejected.");
            }

        } catch (Exception e) {
            System.err.println("Error processing message ID: " + msg.getId() + ". Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

     /**
     * Shutdown the thread pool gracefully.
     */
    public void shutdownWorkerPool() throws InterruptedException {
        workerPool.shutdown();
        if (!workerPool.awaitTermination(60, TimeUnit.SECONDS)) {
            workerPool.shutdownNow();
        }
    }

    /**
     * Closes the KubeMQ client connection.
     */
    public void shutdown() {
        try {
            queuesClient.close();
        } catch (RuntimeException e) {
            System.err.println("Failed to close KubeMQ client: " + e.getMessage());
        }
    }
}
