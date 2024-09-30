package net.breezeware.dynamo.auth.service.fusionauth.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

@Data
@Configuration
@ConfigurationProperties(prefix = "fusionauth")
public class FusionAuthProperties {
    private String apiKey;
    private String url;
    private String clientId;
    private String clientSecret;
    private String tokenUrl;
}
