package io.kubemq.example.queues;

import com.google.protobuf.ByteString;
import io.kubemq.sdk.common.ServerInfo;
import io.kubemq.sdk.queues.QueueMessage;
import io.kubemq.sdk.queues.QueueMessageWaitingPulled;
import io.kubemq.sdk.queues.QueueMessagesPulled;
import io.kubemq.sdk.queues.QueueMessagesWaiting;
import io.kubemq.sdk.queues.QueueSendResult;
import io.kubemq.sdk.queues.QueuesChannel;
import io.kubemq.sdk.queues.QueuesClient;
import io.kubemq.sdk.queues.QueuesPollRequest;
import io.kubemq.sdk.queues.QueuesPollResponse;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Example class demonstrating how to work with KubeMQ queue.
 */
@Service
public class QueuesService {

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
     * Creates a queue channel using the {@link QueuesClient}.
     * This method checks if the channel creation is successful and logs the result.
     */
    public void createQueueChannel(String queueChannelName) {
        try {
            boolean isChannelCreated = queuesClient.createQueuesChannel(queueChannelName);
            System.out.println("QueueChannel created: " + isChannelCreated);
        } catch (RuntimeException e) {
            System.err.println("Failed to create queue channel: " + e.getMessage());
        }
    }
    
    /**
     * Lists all queue channels using the {@link QueuesClient} and prints their attributes.
     * This method retrieves all available queue channels and logs their details such as name, type,
     * last activity, active status, and incoming and outgoing statistics.
     */
    public void listQueueChannels() {
        try {
            List<QueuesChannel> channels = queuesClient.listQueuesChannels("");
            for (QueuesChannel channel : channels) {
                System.out.println("Channel Name: " + channel.getName());
                System.out.println("Type: " + channel.getType());
                System.out.println("Last Activity: " + channel.getLastActivity());
                System.out.println("Is Active: " + channel.getIsActive());
                System.out.println("Incoming Stats: " + channel.getIncoming());
                System.out.println("Outgoing Stats: " + channel.getOutgoing());
                System.out.println();
            }
        } catch (RuntimeException e) {
            System.err.println("Failed to list queue channels: " + e.getMessage());
        }
    }
    
    /**
     * Initiates the queue messages stream to send messages and receive messages send result from server.
     */
    public void sendQueueMessage(String channelName) {
         System.out.println("\n============================== sendMessage Started =============================\n");
            // Send message in Stream 
            QueueMessage message = QueueMessage.builder()
                    .body(("Sending data in queue message").getBytes())
                    .channel(channelName)
                    .metadata("metadata")
                    .id(UUID.randomUUID().toString())
                    .build();
            QueueSendResult sendResult = queuesClient.sendQueuesMessage(message);

            System.out.println("Message sent Response: " + sendResult);

    }
    
    public void receiveQueuesMessages(String channelName) {
        try {
            QueuesPollRequest queuesPollRequest = QueuesPollRequest.builder()
                    .channel(channelName)
                    .pollMaxMessages(1)
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
                    System.out.println("Message ID: " + msg.getId());
                    System.out.println("Message Body: " + new String(msg.getBody()));

                    // Message handling options:

                    // 1. Acknowledge message (mark as processed)
                    msg.ack();

                    // 2. Reject message (won't be requeued)
                    // msg.reject();

                    // 3. Requeue message (send back to queue)
                    // msg.reQueue(channelName);
                });
            }

        } catch (RuntimeException e) {
            System.err.println("Failed to receive queue messages: " + e.getMessage());
        }
    }



    public void receiveAndBulkHandleQueueMessages(String channelName) {
        System.out.println("\n============================== Receive and Bulk Handle Queue Messages =============================\n");
        try {
            QueuesPollRequest queuesPollRequest = QueuesPollRequest.builder()
                    .channel(channelName)
                    .pollMaxMessages(10)  // Increased to receive multiple messages
                    .pollWaitTimeoutInSeconds(15)
                    .build();

            QueuesPollResponse pollResponse = queuesClient.receiveQueuesMessages(queuesPollRequest);

            System.out.println("Received Message Response:");
            System.out.println("RefRequestId: " + pollResponse.getRefRequestId());
            System.out.println("ReceiverClientId: " + pollResponse.getReceiverClientId());
            System.out.println("TransactionId: " + pollResponse.getTransactionId());

            if (pollResponse.isError()) {
                System.out.println("Error: " + pollResponse.getError());
            } else {
                int messageCount = pollResponse.getMessages().size();
                System.out.println("Received " + messageCount + " messages.");

                // Print details of received messages
                pollResponse.getMessages().forEach(msg -> {
                    System.out.println("Message ID: " + msg.getId());
                    System.out.println("Message Body: " + new String(msg.getBody()));
                });

                // Acknowledge all messages
                pollResponse.ackAll();
                System.out.println("Acknowledged all messages.");

                // Reject all messages
                // pollResponse.rejectAll();
                // System.out.println("Rejected all messages.");

                // Requeue all messages
                // pollResponse.reQueueAll(channelName);
                // System.out.println("Requeued all messages.");
            }

        } catch (RuntimeException e) {
            System.err.println("Failed to receive or handle queue messages: " + e.getMessage());
        }
    }
    
    


    public void getWaitingMessages(String channelName) {
        System.out.println("\n============================== getWaitingMessages Started =============================\n");
        try {

            int maxNumberOfMessages = 1;
            int waitTimeSeconds = 10;

            QueueMessagesWaiting rcvMessages = queuesClient.waiting(channelName, maxNumberOfMessages, waitTimeSeconds);

            if (rcvMessages.isError()) {
                System.out.println("Error occurred: " + rcvMessages.getError());
                return;
            }

            System.out.println("Waiting Messages Count: " + rcvMessages.getMessages().size());

            for (QueueMessageWaitingPulled msg : rcvMessages.getMessages()) {
                System.out.println("Message ID: " + msg.getId());
                System.out.println("Channel: " + msg.getChannel());
                System.out.println("Metadata: " + msg.getMetadata());
                System.out.println("Body: " + new String(msg.getBody()));
                System.out.println("From Client ID: " + msg.getFromClientId());
                System.out.println("Tags: " + msg.getTags());
                System.out.println("Timestamp: " + msg.getTimestamp());
                System.out.println("Sequence: " + msg.getSequence());
                System.out.println("Receive Count: " + msg.getReceiveCount());
                System.out.println("Is Re-routed: " + msg.isReRouted());
                System.out.println("Re-route From Queue: " + msg.getReRouteFromQueue());
                System.out.println("Expired At: " + msg.getExpiredAt());
                System.out.println("Delayed To: " + msg.getDelayedTo());
                System.out.println("Receiver Client ID: " + msg.getReceiverClientId());
                System.out.println("--------------------");
            }
        } catch (RuntimeException e) {
            System.err.println("Failed to get waiting messages: " + e.getMessage());
        }

    }


    public void getPullMessages(String channelName) {
        System.out.println("\n============================== getPullMessages Started =============================\n");
        QueueMessagesPulled rcvMessages = queuesClient.pull(channelName, 1, 10);
        System.out.println("Pulled Messages Count: "+rcvMessages.getMessages().size());
        for (QueueMessageWaitingPulled msg : rcvMessages.getMessages()) {
            System.out.println("Message  Id: " + msg.getId());
            System.out.println("Message Body: "+ByteString.copyFrom(msg.getBody()).toStringUtf8());
        }

    }
    
    /**
     * Deletes a queue channel using the {@link QueuesClient}.
     * This method attempts to delete the specified queue channel and logs the result.
     */
    public void deleteQueueChannel(String channelName) {
        try {
            boolean isChannelDeleted = queuesClient.deleteQueuesChannel(channelName);
            System.out.println("QueueChannel deleted: " + isChannelDeleted);
        } catch (RuntimeException e) {
            System.err.println("Failed to delete queue channel: " + e.getMessage());
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
