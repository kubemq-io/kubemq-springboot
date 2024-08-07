package io.kubemq.example.cq;

import io.kubemq.sdk.cq.*;
import io.kubemq.sdk.common.ServerInfo;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Example class demonstrating how to work with KubeMQ CQ(Command/Query).
 */
@Service
public class CQService {

    @Autowired
    private  CQClient cqClient;
            
    private CommandsSubscription commandSubscription;
    private QueriesSubscription queriesSubscription;

    /**
     * Ping to test Connection is successful
     */
    public void ping() {
        System.out.println("Executing ping...");
 
        ServerInfo pingResult = cqClient.ping();
        System.out.println("Ping Response: " + pingResult.toString());
    }

    public void createCommandsChannel(String channel) {
          System.out.println("Executing createCommandsChannel...");
        boolean result = cqClient.createCommandsChannel(channel);
        System.out.println("Commands channel created: " + result);
    }

    public void createQueriesChannel(String channel) {
        System.out.println("\nExecuting createQueriesChannel...");
        boolean result = cqClient.createQueriesChannel(channel);
        System.out.println("Queries channel created: " + result);
    }
    
    public void listCommandsChannels(String channelSearch) {
         System.out.println("\nExecuting listCommandsChannels...");
        List<CQChannel> channels = cqClient.listCommandsChannels(channelSearch);
        System.out.println("Command Channels: " + channels);
    }

    public void listQueriesChannels(String channelSearch) {
         System.out.println("\nExecuting listQueriesChannels...");
        List<CQChannel> channels = cqClient.listQueriesChannels(channelSearch);
        System.out.println("Query Channels: " + channels);
    }


   public void subscribeToCommands(String channel) {
         System.out.println("Executing subscribeToCommands...");
        // Consumer for handling received events
        Consumer<CommandMessageReceived> onReceiveCommandCallback = receivedCommand -> {
            System.out.println("Received CommandMessage: " + receivedCommand);
            // Reply this message 
           CommandResponseMessage response= CommandResponseMessage.builder().
                    commandReceived(receivedCommand)
                     .isExecuted(true)
                     .build();
                    
            cqClient.sendResponseMessage(response);
        };

        // Consumer for handling errors
        Consumer<String> onErrorCallback = errorMessage -> {
            System.err.println("Error in Command Subscription: " + errorMessage);
        };

        commandSubscription = CommandsSubscription.builder()
                .channel(channel)
                .onReceiveCommandCallback(onReceiveCommandCallback)
                .onErrorCallback(onErrorCallback)
                .build();

        cqClient.subscribeToCommands(commandSubscription);
        System.out.println("Commands Subscribed");
        
    }

   public void sendCommandRequest(String channel) {
        System.out.println("Executing sendCommandRequest...");
        
        Map<String, String> tags = new HashMap<>();
        tags.put("tag1", "Command Message example");
        tags.put("tag2", "cq1");
        
        CommandMessage commandMessage = CommandMessage.builder()
                .channel(channel)
                .body("Test Command".getBytes())
                .metadata("Metadata add some extra information")
                .tags(tags)
                .timeoutInSeconds(20)
                .build();

            CommandResponseMessage response = cqClient.sendCommandRequest(commandMessage);
            System.out.println("Command Response: " + response);
    }
   
   public void subscribeToQueries(String channel) {
         System.out.println("Executing subscribeToQueries...");
        // Consumer for handling received events
        Consumer<QueryMessageReceived> onReceiveQueryCallback = receivedQuery -> {
            System.out.println("Received Query: " + receivedQuery);
             // Reply this message 
           QueryResponseMessage response= QueryResponseMessage.builder()
                    .queryReceived(receivedQuery)
                     .isExecuted(true)
                     .body("hello kubemq, I'm replying to you!".getBytes())
                     .build();
                    
            cqClient.sendResponseMessage(response);
            
        };

        // Consumer for handling errors
        Consumer<String> onErrorCallback = errorMessage -> {
            System.err.println("Error in Query Subscription: " + errorMessage);
        };
        
        queriesSubscription = QueriesSubscription.builder()
                .channel(channel)
                .onReceiveQueryCallback(onReceiveQueryCallback)
                .onErrorCallback(onErrorCallback)
                .build();

        cqClient.subscribeToQueries(queriesSubscription);
        
        System.out.println("Queries Subscribed");
        
    }
   
    public void sendQueryRequest(String channel) {
        System.out.println("Executing sendQueryRequest...");
        Map<String, String> tags = new HashMap<>();
        tags.put("tag1", "query Message example");
        tags.put("tag2", "query1");
        QueryMessage queryMessage = QueryMessage.builder()
                .channel(channel)
                .body("Test Query".getBytes())
                .metadata("Metadata put some description")
                .tags(tags)
                .timeoutInSeconds(20)
                .build();

            QueryResponseMessage response = cqClient.sendQueryRequest(queryMessage);
            System.out.println("Query Response: " + response);
       
    }
    
    /**
     *  cancel event & eventstore susbscription
     */
    public void cancelSusbscription(){
        System.out.println("Cancelling Commands subscription...");
        // *** cancel command susbscription
        commandSubscription.cancel();
         System.out.println("Cancelling Queries subscription...");
        // *** Cancel queries susbcription
        queriesSubscription.cancel();
    }

   
    public void deleteCommandsChannel(String channel) {
        System.out.println("Executing deleteCommandsChannel...");
        boolean result = cqClient.deleteCommandsChannel(channel);
        System.out.println("Commands channel deleted: " + result);
    }
    
    

    /**
     * Deletes a queries channel.
     *
     * @param channel the name of the channel to delete
     */
    public void deleteQueriesChannel(String channel) {
        System.out.println("Executing deleteQueriesChannel...");
        boolean result = cqClient.deleteQueriesChannel(channel);
        System.out.println("Queries channel deleted: " + result);
    }
   
    
    /**
     * Closes the KubeMQ client connection.
     */
    public void shutdown() {
        try {
            cqClient.close();
        } catch (RuntimeException e) {
            System.err.println("Failed to close KubeMQ client: " + e.getMessage());
        }
    }
   
}
