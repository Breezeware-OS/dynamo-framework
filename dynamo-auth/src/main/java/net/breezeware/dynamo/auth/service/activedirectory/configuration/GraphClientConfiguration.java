package net.breezeware.dynamo.auth.service.activedirectory.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import com.azure.identity.ClientSecretCredential;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.microsoft.graph.authentication.TokenCredentialAuthProvider;
import com.microsoft.graph.requests.GraphServiceClient;

import net.breezeware.dynamo.auth.service.activedirectory.properties.ActiveDirectoryProperties;
import net.breezeware.dynamo.auth.service.activedirectory.properties.ActiveDirectoryResourceServer;

/**
 * Active directory client configuration.
 */
@Configuration
@Profile("active-directory")
public class GraphClientConfiguration {

    @Autowired
    ActiveDirectoryProperties activeDirectoryProperties;

    @Autowired
    ActiveDirectoryResourceServer activeDirectoryResourceServer;

    @Bean("ResourceServerGraphClient")
    public GraphServiceClient graphServiceClient() {
        ClientSecretCredential clientSecretCredential = getClientSecretCredential();
        TokenCredentialAuthProvider tokenCredentialAuthProvider =
                getTokenCredentialAuthProvider(clientSecretCredential);
        GraphServiceClient graphClient =
                GraphServiceClient.builder().authenticationProvider(tokenCredentialAuthProvider).buildClient();
        return graphClient;
    }

    private TokenCredentialAuthProvider getTokenCredentialAuthProvider(ClientSecretCredential clientSecretCredential) {
        TokenCredentialAuthProvider tokenCredentialAuthProvider =
                new TokenCredentialAuthProvider(clientSecretCredential);
        return tokenCredentialAuthProvider;
    }

    private ClientSecretCredential getClientSecretCredential() {
        ClientSecretCredential clientSecretCredential =
                new ClientSecretCredentialBuilder().clientId(activeDirectoryResourceServer.getClientId())
                        .clientSecret(activeDirectoryResourceServer.getClientSecret())
                        .tenantId(activeDirectoryProperties.getTenantId()).build();
        return clientSecretCredential;
    }

}
