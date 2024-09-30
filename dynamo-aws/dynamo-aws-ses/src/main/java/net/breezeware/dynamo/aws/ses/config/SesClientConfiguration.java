package net.breezeware.dynamo.aws.ses.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import lombok.extern.slf4j.Slf4j;

import software.amazon.awssdk.core.client.config.ClientOverrideConfiguration;
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ses.SesClient;

/**
 * AWS SES Client configuration.
 */
@Slf4j
@Configuration
public class SesClientConfiguration {

    /**
     * Builds an {@link SesClient} pointing to cloud.
     * @param  awsRegion region of the client.
     * @return           {@link SesClient}.
     */
    // @Profile("!development")
    @Bean("sesClient")
    public SesClient buildSesClient(@Value("${aws.region}") String awsRegion) {
        log.debug("Entering buildS3Client(), awsRegion = {}", awsRegion);

        SesClient sesClient =
                SesClient.builder().httpClient(UrlConnectionHttpClient.create()).region(Region.of(awsRegion))
                        .overrideConfiguration(ClientOverrideConfiguration.builder().build()).build();

        log.debug("Leaving buildS3Client(), sesClient = {}", sesClient);
        return sesClient;
    }
}
