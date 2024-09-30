package net.breezeware.dynamo.aws.s3.config;

import java.net.URI;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import lombok.extern.slf4j.Slf4j;

import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.core.client.config.ClientOverrideConfiguration;
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

/**
 * AWS S3 Client configuration class.
 */
@Slf4j
@Configuration
public class S3ClientConfiguration {

    /**
     * Builds an {@link S3Client} pointing to development endpoint.<br>
     * Overrides credential provider to {@link ProfileCredentialsProvider} and
     * endpoint to <a href="http://localhost:4566">Localstack Local URI</a> for
     * <b>development</b> profile.
     * @param  awsRegion region of the client.
     * @return           {@link S3Client}.
     */
    @Profile("development")
    @Bean("s3Client")
    public S3Client buildS3DevClient(@Value("${aws.region}") String awsRegion) {
        log.debug("Entering buildS3DevClient(), awsRegion = {}", awsRegion);

        S3Client s3Client = S3Client.builder().httpClient(UrlConnectionHttpClient.create()).region(Region.of(awsRegion))
                .overrideConfiguration(ClientOverrideConfiguration.builder().build())
                .credentialsProvider(ProfileCredentialsProvider.create())
                .endpointOverride(URI.create("http://localhost:4566")).build();

        log.debug("Leaving buildS3DevClient(), s3Client = {}", s3Client);
        return s3Client;
    }

    /**
     * Builds an {@link S3Client} pointing to cloud.
     * @param  awsRegion region of the client.
     * @return           {@link S3Client}.
     */
    @Profile("!development")
    @Bean("s3Client")
    public S3Client buildS3Client(@Value("${aws.region}") String awsRegion) {
        log.debug("Entering buildS3Client(), awsRegion = {}", awsRegion);

        S3Client s3Client = S3Client.builder().httpClient(UrlConnectionHttpClient.create()).region(Region.of(awsRegion))
                .overrideConfiguration(ClientOverrideConfiguration.builder().build()).build();

        log.debug("Leaving buildS3Client(), s3Client = {}", s3Client);
        return s3Client;
    }
}
