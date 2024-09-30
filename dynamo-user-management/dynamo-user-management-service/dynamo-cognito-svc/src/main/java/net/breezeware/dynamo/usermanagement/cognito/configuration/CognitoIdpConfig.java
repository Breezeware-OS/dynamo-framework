package net.breezeware.dynamo.usermanagement.cognito.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import software.amazon.awssdk.core.client.config.ClientOverrideConfiguration;
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;

/**
 * Configuration class for the Cognito Identity Provider client. This class
 * provides a method for building and returning a CognitoIdentityProviderClient
 * object, which can be used to access and manage user pools and other
 * identity-related functionality in Amazon Cognito.
 */
@Configuration
public class CognitoIdpConfig {

    /**
     * Builds and returns a new instance of the{@link CognitoIdentityProviderClient}
     * using the specified AWS region.
     * @param  awsRegion region of the client.
     * @return           a new instance of CognitoIdentityProviderClient configured
     *                   with the specified AWS region and no additional
     *                   configuration.
     */
    @Bean("CognitoIdentityProviderClient")
    public CognitoIdentityProviderClient buildCognitoIdentityProviderClient(@Value("${aws.region}") String awsRegion) {
        return CognitoIdentityProviderClient.builder().region(Region.of(awsRegion))
                .overrideConfiguration(ClientOverrideConfiguration.builder().build())
                .httpClient(UrlConnectionHttpClient.create()).build();
    }
}
