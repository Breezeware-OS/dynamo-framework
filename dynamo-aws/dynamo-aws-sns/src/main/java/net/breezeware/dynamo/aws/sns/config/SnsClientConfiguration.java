package net.breezeware.dynamo.aws.sns.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import lombok.extern.slf4j.Slf4j;

import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sns.SnsClient;

/**
 * AWS SNS Client configuration.
 */
@Configuration
@Slf4j
public class SnsClientConfiguration {
    @Bean(name = "snsClient")
    public SnsClient buildSnsClient(@Value("${aws.region}") String awsRegion) {
        log.debug("Entering buildSnsClient(), awsRegion = {}", awsRegion);
        SnsClient snsClient =
                SnsClient.builder().httpClient(UrlConnectionHttpClient.create()).region(Region.of(awsRegion)).build();
        log.debug("Leaving buildSnsClient(), awsRegion = {}", awsRegion);
        return snsClient;
    }
}
