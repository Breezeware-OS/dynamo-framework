package net.breezeware.dynamo.auth.service.fusionauth.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import net.breezeware.dynamo.auth.service.fusionauth.properties.FusionAuthProperties;

import lombok.extern.slf4j.Slf4j;

import io.fusionauth.client.FusionAuthClient;

/**
 * FusionAuth client configuration.
 */
@Configuration
@Slf4j
@Profile("fusionauth")
public class FusionAuthClientConfiguration {

    @Autowired
    FusionAuthProperties fusionAuthProperties;

    /**
     * Build fusionAuthClient.
     * @return Client that connects to a FusionAuth server and provides access to
     *         the full set of FusionAuth APIs.
     */
    @Bean("fusionAuthClient")
    public FusionAuthClient getFusionAuthClient() {
        log.info("Entering getFusionAuthClient apiKey = {}, url = {}", fusionAuthProperties.getApiKey(),
                fusionAuthProperties.getUrl());
        FusionAuthClient fusionAuthClient =
                new FusionAuthClient(fusionAuthProperties.getApiKey(), fusionAuthProperties.getUrl());
        log.info("Leaving getFusionAuthClient");
        return fusionAuthClient;
    }
}
