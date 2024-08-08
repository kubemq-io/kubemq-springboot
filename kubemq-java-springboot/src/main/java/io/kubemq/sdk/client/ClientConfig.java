package io.kubemq.sdk.client;

import io.kubemq.sdk.common.KubeMQUtils;
import io.kubemq.sdk.cq.CQClient;
import io.kubemq.sdk.pubsub.PubSubClient;
import io.kubemq.sdk.queues.QueuesClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ClientConfig {

    private final KubeMQClientProperties properties;

    public ClientConfig(KubeMQClientProperties properties) {
        this.properties = properties;
    }

    @Bean
    public CQClient cqClient() {
        return CQClient.builder()
                .address(properties.getAddress())
                .clientId(properties.getClientId())
                .authToken(properties.getAuthToken())
                .tls(properties.isTls())
                .tlsCertFile(properties.getTlsCertFile())
                .tlsKeyFile(properties.getTlsKeyFile())
                .maxReceiveSize(properties.getMaxReceiveSize())
                .reconnectIntervalSeconds(properties.getReconnectIntervalSeconds())
                .keepAlive(properties.isKeepAlive())
                .pingIntervalInSeconds(properties.getPingIntervalInSeconds())
                .pingTimeoutInSeconds(properties.getPingTimeoutInSeconds())
                .logLevel(properties.getLogLevel())
                .build();
    }

    @Bean
    public QueuesClient queuesClient() {
        return QueuesClient.builder()
                .address(properties.getAddress())
                .clientId(properties.getClientId())
                .authToken(properties.getAuthToken())
                .tls(properties.isTls())
                .tlsCertFile(properties.getTlsCertFile())
                .tlsKeyFile(properties.getTlsKeyFile())
                .maxReceiveSize(properties.getMaxReceiveSize())
                .reconnectIntervalSeconds(properties.getReconnectIntervalSeconds())
                .keepAlive(properties.isKeepAlive())
                .pingIntervalInSeconds(properties.getPingIntervalInSeconds())
                .pingTimeoutInSeconds(properties.getPingTimeoutInSeconds())
                .logLevel(properties.getLogLevel())
                .build();
    }

    @Bean
    public PubSubClient pubSubClient() {
        return PubSubClient.builder()
                .address(properties.getAddress())
                .clientId(properties.getClientId())
                .authToken(properties.getAuthToken())
                .tls(properties.isTls())
                .tlsCertFile(properties.getTlsCertFile())
                .tlsKeyFile(properties.getTlsKeyFile())
                .maxReceiveSize(properties.getMaxReceiveSize())
                .reconnectIntervalSeconds(properties.getReconnectIntervalSeconds())
                .keepAlive(properties.isKeepAlive())
                .pingIntervalInSeconds(properties.getPingIntervalInSeconds())
                .pingTimeoutInSeconds(properties.getPingTimeoutInSeconds())
                .logLevel(properties.getLogLevel())
                .build();
    }

    @Bean
    public KubeMQUtils kubeMQUtils() {
        return new KubeMQUtils();
    }
}

