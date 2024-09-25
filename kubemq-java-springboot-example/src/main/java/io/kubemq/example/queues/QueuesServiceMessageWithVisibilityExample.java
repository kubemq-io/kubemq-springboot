package io.kubemq.example.queues;

import io.kubemq.sdk.common.ServerInfo;
import io.kubemq.sdk.queues.QueueMessage;
import io.kubemq.sdk.queues.QueueSendResult;
import io.kubemq.sdk.queues.QueuesClient;
import io.kubemq.sdk.queues.QueuesPollRequest;
import io.kubemq.sdk.queues.QueuesPollResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Example class demonstrating how to work with KubeMQ queue.
 */
@Service
public class QueuesServiceMessageWithVisibilityExample {

    @Autowired
    private QueuesClient queuesClient;


    /**
     * Ping to test Connection is successful
     */
    public void ping() {
        System.out.println("Executing ping...");

        ServerInfo pingResult = queuesClient.ping();
        System.out.println("Ping Response: " + pingResult.toString());
    }

    /**
     * Sends 3 messages to the queue.
     */
    public void sendQueueMessage(String channelName) {
        System.out.println("\n============================== Send Queue Messages Started =============================\n");
        try {
            for (int i = 0; i < 5; i++) {
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



    public void receiveExampleWithVisibility(String channelName) {
        System.out.println("\n============================== receiveExampleWithVisibility =============================\n");
       try {
            QueuesPollRequest queuesPollRequest = QueuesPollRequest.builder()
                    .channel(channelName)
                    .pollMaxMessages(1)  // Pull 10 messages at a time
                    .pollWaitTimeoutInSeconds(10)
                    .visibilitySeconds(5)
                    .autoAckMessages(false)
                    .build();

            QueuesPollResponse pollResponse = queuesClient.receiveQueuesMessages(queuesPollRequest);

            System.out.println("Received Message Response:"+" TransactionId: " + pollResponse.getTransactionId());
            System.out.println("RefRequestId: " + pollResponse.getRefRequestId()+" ReceiverClientId: " + pollResponse.getReceiverClientId());

            if (pollResponse.isError()) {
                System.out.println("Error: " + pollResponse.getError());
            } else {
                pollResponse.getMessages().forEach(msg -> {
                    try {
                        System.out.println("Message ID: " + msg.getId()+" Message Body: " + new String(msg.getBody()));
                        Thread.sleep(1000);
                        msg.ack();
                        System.out.println("Acknowledged to message");
                    } catch (InterruptedException ex) {
                        System.out.println("Exception: "+ex.getMessage());
                    }
                });
            }

        } catch (RuntimeException e) {
            System.err.println("Failed to receive queue messages: " + e.getMessage());
        }
    }

    public void receiveExampleWithVisibilityExpired(String channelName) {
        System.out.println("\n============================== receiveExampleWithVisibilityExpired =============================\n");
        try {
            QueuesPollRequest queuesPollRequest = QueuesPollRequest.builder()
                    .channel(channelName)
                    .pollMaxMessages(1)  // Pull 10 messages at a time
                    .pollWaitTimeoutInSeconds(10)
                    .visibilitySeconds(2)
                    .autoAckMessages(false)
                    .build();

            QueuesPollResponse pollResponse = queuesClient.receiveQueuesMessages(queuesPollRequest);

            System.out.println("Received Message Response:"+" TransactionId: " + pollResponse.getTransactionId());
            System.out.println("RefRequestId: " + pollResponse.getRefRequestId()+" ReceiverClientId: " + pollResponse.getReceiverClientId());

            if (pollResponse.isError()) {
                System.out.println("Error: " + pollResponse.getError());
            } else {
                pollResponse.getMessages().forEach(msg -> {
                    try {
                        System.out.println("Message ID: " + msg.getId()+" Message Body: " + new String(msg.getBody()));
                        Thread.sleep(3000);
                        msg.ack();
                        System.out.println("Acknowledged to message");
                    } catch (InterruptedException ex) {
                        System.out.println("Exception: "+ex.getMessage());
                    }
                });
            }

        } catch (RuntimeException e) {
            System.err.println("Failed to receive queue messages: " + e.getMessage());
        }
    }

    
    public void receiveExampleWithVisibilityExtension(String channelName) {
        System.out.println("\n============================== receiveExampleWithVisibilityExtension =============================\n");
        try {
            QueuesPollRequest queuesPollRequest = QueuesPollRequest.builder()
                    .channel(channelName)
                    .pollMaxMessages(1)  // Pull 10 messages at a time
                    .pollWaitTimeoutInSeconds(10)
                    .visibilitySeconds(3)
                    .autoAckMessages(false)
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
                    try {
                        System.out.println("Message ID: " + msg.getId()+" Message Body: " + new String(msg.getBody()));
                        Thread.sleep(1000);
                        msg.extendVisibilityTimer(3);
                        Thread.sleep(2000);
                        msg.ack();
                        System.out.println("Acknowledged to message");
                    } catch (Exception ex) {
                        System.out.println("Exception: "+ex.getMessage());
                    }
                });
            }

        } catch (RuntimeException e) {
            System.err.println("Failed to receive queue messages: " + e.getMessage());
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
