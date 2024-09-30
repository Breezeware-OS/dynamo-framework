package net.breezeware.dynamo.auth.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Configuration
@ConfigurationProperties(prefix = "dynamo.auth.http")
public class DynamoAuthHttpProperties {
    private String[] allowedEndpoints;
    private JwtTokenProperties jwtToken;
}
