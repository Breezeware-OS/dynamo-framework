package net.breezeware.dynamo.aws.dynamodb;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import software.amazon.awssdk.auth.credentials.EnvironmentVariableCredentialsProvider;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

@Component
public class AwsDynamoDbConfiguration {

    Region region = Region.US_EAST_1;

    /**
     * Creates an Amazon S3 client from the AWS credentials.
     * @return AmazonS3 Returns an Amazon S3 client.
     */
    @Bean(name = "dynamoDbClient")
    public DynamoDbClient buildDynamoDbClient() {
        DynamoDbClient ddb = DynamoDbClient.builder().region(region)
                .credentialsProvider(EnvironmentVariableCredentialsProvider.create()).build();

        return ddb;
    }

    /**
     * Creates an Amazon S3 client from the AWS credentials.
     * @return AmazonS3 Returns an Amazon S3 client.
     */
    @Bean(name = "dynamoDbEnhancedClient")
    public DynamoDbEnhancedClient buildDynamoDbEnhancedClient(@Autowired DynamoDbClient ddb) {
        DynamoDbEnhancedClient enhancedClient = DynamoDbEnhancedClient.builder().dynamoDbClient(ddb).build();
        return enhancedClient;
    }
}