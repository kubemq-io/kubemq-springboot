package io.kubemq.example.pubsub;

import io.kubemq.sdk.common.ServerInfo;
import io.kubemq.sdk.pubsub.*;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Example class demonstrating the use of PubSubClient to send and receive messages using KubeMQ.
 */
@Service
public class PubsubService {

    
    @Autowired
    private  PubSubClient pubSubClient;
    private EventsSubscription eventSubscription;
    private EventsStoreSubscription eventStoreSubscription;

    
    /**
     * Ping to test Connection is successful
     */
    public void ping() {
        System.out.println("Executing ping...");

        ServerInfo pingResult = pubSubClient.ping();
        System.out.println("Ping Response: " + pingResult.toString());
    }

    /**
     * Creates an events channel using the PubSubClient.
     */
    public void createEventsChannel(String channelName) {
        try {
            boolean isChannelCreated = pubSubClient.createEventsChannel(channelName);
            System.out.println("EventsChannel created: " + isChannelCreated);
        } catch (RuntimeException e) {
            System.err.println("Failed to create events channel: " + e.getMessage());
        }
    }

    /**
     * Creates an events store channel using the PubSubClient.
     */
    public void createEventsStoreChannel(String channelName) {
        try {
            boolean isChannelCreated = pubSubClient.createEventsStoreChannel(channelName);
            System.out.println("EventsStoreChannel created: " + isChannelCreated);
        } catch (RuntimeException e) {
            System.err.println("Failed to create events store channel: " + e.getMessage());
        }
    }
    
     /**
     * List events channel.
     */
    public void listEventsChannel(String searchQuery) {
        try {
           System.out.println("Events Channel listing");
           List<PubSubChannel> eventChannel = pubSubClient.listEventsChannels(searchQuery);
           eventChannel.forEach(  evt -> {
               System.out.println("Name: "+evt.getName()+" ChannelTYpe: "+evt.getType()+" isActive: "+evt.getIsActive());
           });
           
        } catch (RuntimeException e) {
            System.err.println("Failed to list event channel: " + e.getMessage());
        }
    }

    /**
     * List events store channel.
     */
    public void listEventsStoreChannel(String searchQuery) {
        try {
              System.out.println("Events store Channel listing");
           List<PubSubChannel> eventChannel = pubSubClient.listEventsStoreChannels(searchQuery);
           eventChannel.forEach(  evt -> {
               System.out.println("Name: "+evt.getName()+" ChannelTYpe: "+evt.getType()+" isActive: "+evt.getIsActive());
           });
        } catch (RuntimeException e) {
            System.err.println("Failed to list events store channel: " + e.getMessage());
        }
    }
    
     /**
     * Subscribes to events from the specified channel and processes received events.
     */
    public void subscribeToEvents(String eventChannelName) {
        try {
            // Consumer for handling received events
            Consumer<EventMessageReceived> onReceiveEventCallback = event -> {
                System.out.println("Received event:");
                System.out.println("ID: " + event.getId());
                System.out.println("Channel: " + event.getChannel());
                System.out.println("Metadata: " + event.getMetadata());
                System.out.println("Body: " + new String(event.getBody()));
                System.out.println("Tags: " + event.getTags());
            };
            

            // Consumer for handling errors
            Consumer<String> onErrorCallback = error -> {
                System.err.println("Error Received: " + error);
            };

            eventSubscription= EventsSubscription.builder()
                    .channel(eventChannelName)
                    .onReceiveEventCallback(onReceiveEventCallback)
                    .onErrorCallback(onErrorCallback)
                    .build();

            pubSubClient.subscribeToEvents(eventSubscription);
            System.out.println("Events Subscribed");

        } catch (RuntimeException e) {
            System.err.println("Failed to subscribe to events: " + e.getMessage());
        }
    }

    
    /**
     * Subscribes to events store messages from the specified channel with a specific configuration.
     */
    public void subscribeToEventsStore(String eventStoreChannelName) {
        try {
            // Consumer for handling received event store messages
            Consumer<EventStoreMessageReceived> onReceiveEventCallback = event -> {
                System.out.println("Received event store:");
                System.out.println("ID: " + event.getId());
                System.out.println("Channel: " + event.getChannel());
                System.out.println("Metadata: " + event.getMetadata());
                System.out.println("Body: " + new String(event.getBody()));
                System.out.println("Tags: " + event.getTags());
            };

            // Consumer for handling errors
            Consumer<String> onErrorCallback = error -> {
                System.err.println("Error Received: " + error);
            };

            eventStoreSubscription = EventsStoreSubscription.builder()
                    .channel(eventStoreChannelName)
                    //.group("All IT Team")
                    .eventsStoreType(EventsStoreType.StartAtTime)
                    .eventsStoreStartTime(Instant.now().minus(1, ChronoUnit.HOURS))
                    .onReceiveEventCallback(onReceiveEventCallback)
                    .onErrorCallback(onErrorCallback)
                    .build();

            pubSubClient.subscribeToEventsStore(eventStoreSubscription);
            System.out.println("EventsStore Subscribed");
            
        } catch (RuntimeException e) {
            System.err.println("Failed to subscribe to events store: " + e.getMessage());
        }
    }
    
    /**
     * Sends an event message to the configured events channel.
     */
    public void sendEventMessage(String eventChannelName) {
        try {
            String data = "Any data can be passed in byte, JSON or anything";
            Map<String, String> tags = new HashMap<>();
            tags.put("tag1", "kubemq");
            tags.put("tag2", "kubemq2");

            EventMessage eventMessage = EventMessage.builder()
                    .id(UUID.randomUUID().toString())
                    .channel(eventChannelName)
                    .metadata("something you want to describe")
                    .body(data.getBytes())
                    .tags(tags)
                    .build();
            
            pubSubClient.sendEventsMessage(eventMessage);
            System.out.println("Event message sent ");
        } catch (RuntimeException e) {
            System.err.println("Failed to send event message: " + e.getMessage());
        }
    }

    /**
     * Sends an event store message to the configured events store channel.
     */
    public void sendEventStoreMessage(String eventStoreChannelName) {
        try {
            System.out.println("Sending Events store message");
            String data = "Any data can be passed in byte, JSON or anything";
            Map<String, String> tags = new HashMap<>();
            tags.put("tag1", "kubemq");
            tags.put("tag2", "kubemq2");

            EventStoreMessage eventStoreMessage = EventStoreMessage.builder()
                    .id(UUID.randomUUID().toString())
                    .channel(eventStoreChannelName)
                    .metadata("something you want to describe")
                    .body(data.getBytes())
                    .tags(tags)
                    .build();
            
            EventSendResult result = pubSubClient.sendEventsStoreMessage(eventStoreMessage);
            System.out.println("Send eventstore result: " + result);
        } catch (RuntimeException e) {
            System.err.println("Failed to send event store message: " + e.getMessage());
        }
    }
    
    /**
     * It's logical method to cancel susbscription
     */
    public void cancelSusbscription(){
        // *** cancel event susbscription
        eventSubscription.cancel();
        // *** Cancel eventstore susbcription
        eventStoreSubscription.cancel();
    }

    /**
     * Deletes an events channel with the specified name.
     */
    public void deleteEventsChannel(String eventChannelName) {
        try {
            boolean isChannelDeleted = pubSubClient.deleteEventsChannel(eventChannelName);
            System.out.println("Events Channel deleted: " + isChannelDeleted);
        } catch (RuntimeException e) {
            System.err.println("Failed to delete events channel: " + e.getMessage());
        }
    }

    /**
     * Deletes an events store channel with the specified name.
     */
    public void deleteEventsStoreChannel(String eventStoreChannelName) {
        try {
            boolean isChannelDeleted = pubSubClient.deleteEventsStoreChannel(eventStoreChannelName);
            System.out.println("Events store Channel deleted: " + isChannelDeleted);
        } catch (RuntimeException e) {
            System.err.println("Failed to delete events store channel: " + e.getMessage());
        }
    }

    /**
     * Closes the KubeMQ client connection.
     */
    public void shutdown() {
        try {
            pubSubClient.close();
        } catch (RuntimeException e) {
            System.err.println("Failed to close KubeMQ client: " + e.getMessage());
        }
    }
}
