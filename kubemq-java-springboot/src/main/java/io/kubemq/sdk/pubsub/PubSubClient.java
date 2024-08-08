package io.kubemq.sdk.pubsub;

import io.kubemq.sdk.client.KubeMQClient;
import io.kubemq.sdk.common.KubeMQUtils;
import kubemq.Kubemq;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * PubSubClient is a specialized client for publishing and subscribing to messages using KubeMQ.
 * It provides methods to send messages, create and delete channels, list channels, and subscribe to events.
 */
@Slf4j
public class PubSubClient extends KubeMQClient {

    @Autowired
    private KubeMQUtils kubeMqUtils;
    private final EventStreamHelper eventStreamHelper;

    @Builder
    public PubSubClient(String address, String clientId, String authToken, boolean tls, String tlsCertFile, String tlsKeyFile,
                        int maxReceiveSize, int reconnectIntervalSeconds, Boolean keepAlive, int pingIntervalInSeconds, int pingTimeoutInSeconds, Level logLevel) {
        super(address, clientId, authToken, tls, tlsCertFile, tlsKeyFile, maxReceiveSize, reconnectIntervalSeconds, keepAlive, pingIntervalInSeconds, pingTimeoutInSeconds, logLevel);
        eventStreamHelper =  new EventStreamHelper();
    }

    /**
     * Sends an event message.
     *
     * @param message the event message to be sent
     * @return the result of sending the event message
     * @throws RuntimeException if sending the message fails
     */
    public void sendEventsMessage(EventMessage message) {
        try {
            log.debug("Sending event message");
            message.validate();
            Kubemq.Event event = message.encode(this.getClientId());
            eventStreamHelper.sendEventMessage(this,event);
        } catch (Exception e) {
            log.error("Failed to send event message", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Sends an event store message.
     *
     * @param message the event store message to be sent
     * @return the result of sending the event store message
     * @throws RuntimeException if sending the message fails
     */
    public EventSendResult sendEventsStoreMessage(EventStoreMessage message) {
        try {
            log.debug("Sending event store message");
            message.validate();
            Kubemq.Event event = message.encode(this.getClientId());
            return eventStreamHelper.sendEventStoreMessage(this,event);
        } catch (Exception e) {
            log.error("Failed to send event store message", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Creates an events channel with the specified name.
     *
     * @param channelName the name of the channel to be created
     * @return true if the channel was created successfully, false otherwise
     * @throws RuntimeException if creating the channel fails
     */
    public boolean createEventsChannel(String channelName) {
        log.debug("Creating events channel: {}", channelName);
        return kubeMqUtils.createChannelRequest(this, this.getClientId(),channelName,"events");
    }

    /**
     * Creates an events store channel with the specified name.
     *
     * @param channelName the name of the channel to be created
     * @return true if the channel was created successfully, false otherwise
     * @throws RuntimeException if creating the channel fails
     */
    public boolean createEventsStoreChannel(String channelName) {
        log.debug("Creating events store channel: {}", channelName);
        return kubeMqUtils.createChannelRequest(this, this.getClientId(),channelName,"events_store");
    }


    /**
     * Lists events channels that match the search criteria.
     *
     * @param search the search criteria for listing channels
     * @return a list of matching events channels
     * @throws RuntimeException if listing channels fails
     */
    public List<PubSubChannel> listEventsChannels(String search) {
        log.debug("Listing events channels with search: {}", search);
        return kubeMqUtils.listPubSubChannels(this, this.getClientId(),"events",search);
    }

    /**
     * Lists events store channels that match the search criteria.
     *
     * @param search the search criteria for listing channels
     * @return a list of matching events store channels
     * @throws RuntimeException if listing channels fails
     */
    public List<PubSubChannel> listEventsStoreChannels(String search) {
        log.debug("Listing events store channels with search: {}", search);
        return kubeMqUtils.listPubSubChannels(this, this.getClientId(),"events_store",search);
    }

    /**
     * Subscribes to events and processes them using the provided subscription.
     *
     * @param subscription the subscription to handle received events
     * @throws RuntimeException if subscribing to events fails
     */
    public void subscribeToEvents(EventsSubscription subscription) {
        try {
            log.debug("Subscribing to events");
            subscription.validate();
            kubemq.Kubemq.Subscribe subscribe = subscription.encode(this.getClientId(),this);
            this.getAsyncClient().subscribeToEvents(subscribe, subscription.getObserver());

        } catch (Exception e) {
            log.error("Failed to subscribe to events", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Subscribes to events store and processes them using the provided subscription.
     *
     * @param subscription the subscription to handle received events store messages
     * @throws RuntimeException if subscribing to events store fails
     */
    public void subscribeToEventsStore(EventsStoreSubscription subscription) {
        try {
            log.debug("Subscribing to events store");
            subscription.validate();
            kubemq.Kubemq.Subscribe subscribe = subscription.encode(this.getClientId(),this);
            this.getAsyncClient().subscribeToEvents(subscribe, subscription.getObserver());

        } catch (Exception e) {
            log.error("Failed to subscribe to events store", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Deletes the events channel with the specified name.
     *
     * @param channelName the name of the channel to be deleted
     * @return true if the channel was deleted successfully, false otherwise
     * @throws RuntimeException if deleting the channel fails
     */
    public boolean deleteEventsChannel(String channelName) {
        log.debug("Deleting events channel: {}", channelName);
        return kubeMqUtils.deleteChannelRequest(this, this.getClientId(),channelName,"events");
    }

    /**
     * Deletes the events store channel with the specified name.
     *
     * @param channelName the name of the channel to be deleted
     * @return true if the channel was deleted successfully, false otherwise
     * @throws RuntimeException if deleting the channel fails
     */
    public boolean deleteEventsStoreChannel(String channelName) {
        log.debug("Deleting events store channel: {}", channelName);
        return kubeMqUtils.deleteChannelRequest(this, this.getClientId(),channelName,"events_store");
    }

}
