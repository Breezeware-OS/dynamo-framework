package net.breezeware.dynamo.auth.service.activedirectory.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

@Data
@Configuration
@ConfigurationProperties(prefix = "active-directory.resource-server", ignoreInvalidFields = true)
public class ActiveDirectoryResourceServer {
    private String clientId;
    private String clientSecret;
    private String resourceId;

}
