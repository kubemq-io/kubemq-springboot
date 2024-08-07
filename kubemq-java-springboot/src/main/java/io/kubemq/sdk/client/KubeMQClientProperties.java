package io.kubemq.sdk.client;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Setter
@Getter
@Configuration
@ConfigurationProperties(prefix = "kubemq.client")
public class KubeMQClientProperties {
    private String address;
    private String clientId;
    private String authToken;
    private boolean tls;
    private String tlsCertFile;
    private String tlsKeyFile;
    private int maxReceiveSize = 1024 * 1024 * 100; // 100MB
    private int reconnectIntervalSeconds = 5;
    private boolean keepAlive;
    private int pingIntervalInSeconds = 180;
    private int pingTimeoutInSeconds = 20;
    private KubeMQClient.Level logLevel = KubeMQClient.Level.INFO;
}
