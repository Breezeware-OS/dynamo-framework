package net.breezeware.dynamo.auth.service.activedirectory.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

@Data
@Configuration
@ConfigurationProperties(prefix = "active-directory", ignoreInvalidFields = true)
public class ActiveDirectoryProperties {
    private String tenantId;
    private String tokenUrl;
    private String domainName;

}
