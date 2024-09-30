package net.breezeware.dynamo.aws.sns.service.impl;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import net.breezeware.dynamo.aws.sns.exception.DynamoSnsException;
import net.breezeware.dynamo.aws.sns.service.api.SnsService;

import lombok.extern.slf4j.Slf4j;

import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.CreatePlatformEndpointRequest;
import software.amazon.awssdk.services.sns.model.CreatePlatformEndpointResponse;
import software.amazon.awssdk.services.sns.model.DeleteEndpointRequest;
import software.amazon.awssdk.services.sns.model.GetEndpointAttributesRequest;
import software.amazon.awssdk.services.sns.model.GetEndpointAttributesResponse;
import software.amazon.awssdk.services.sns.model.MessageAttributeValue;
import software.amazon.awssdk.services.sns.model.PublishRequest;
import software.amazon.awssdk.services.sns.model.PublishResponse;

/**
 * AWS simple notification service implementation.
 */
@Service
@Slf4j
public class SnsServiceImpl implements SnsService {

    @Autowired
    SnsClient snsClient;

    @Value("${aws.sns.sms.sender-id}")
    private String awsSnsSmsSenderId;

    @Value("${aws.sns.sms.sms-type}")
    private String awsSnsSmsSmsType;

    @Override
    public String publishTextSms(String message, String phoneNumber) throws DynamoSnsException {
        log.debug("Entering publishTextSMS, message = {}, phoneNumber = {}", message, phoneNumber);

        Map<String, MessageAttributeValue> smsAttributes = new HashMap<String, MessageAttributeValue>();
        smsAttributes.put("AWS.SNS.SMS.SenderID",
                MessageAttributeValue.builder().stringValue(awsSnsSmsSenderId).dataType("String").build());
        smsAttributes.put("AWS.SNS.SMS.SMSType",
                MessageAttributeValue.builder().stringValue(awsSnsSmsSmsType).dataType("String").build());

        try {
            PublishRequest publishRequest = PublishRequest.builder().phoneNumber(phoneNumber).message(message)
                    .messageAttributes(smsAttributes).build();
            PublishResponse publishResponse = snsClient.publish(publishRequest);
            log.info("{} Message sent. Status was {}", publishResponse.messageId(),
                    publishResponse.sdkHttpResponse().statusCode());
            log.debug("Leaving publishTextSMS, messageId = {}", publishResponse.messageId());
            return publishResponse.messageId();
        } catch (Exception e) {
            log.error("Error while publishing text SMS = {}", e.getMessage());
            throw new DynamoSnsException(e.getMessage(), e.getCause());
        }

    }

    @Override
    public String createPlatformEndpoint(String token, String platformApplicationArn) throws DynamoSnsException {
        log.debug("Entering  createPlatformEndpoint, token = {}, platformApplicationArn = {}", token,
                platformApplicationArn);
        try {
            CreatePlatformEndpointRequest createPlatformEndpointRequest = CreatePlatformEndpointRequest.builder()
                    .platformApplicationArn(platformApplicationArn).token(token).build();
            CreatePlatformEndpointResponse createPlatformEndpointResponse =
                    snsClient.createPlatformEndpoint(createPlatformEndpointRequest);
            log.info("The ARN of the endpoint is {}", createPlatformEndpointResponse.endpointArn());
            log.debug("Leaving  createPlatformEndpoint, endpointArn = {}",
                    createPlatformEndpointResponse.endpointArn());
            return createPlatformEndpointResponse.endpointArn();
        } catch (Exception e) {
            log.error("Error while creating platform endpoint = {}", e.getMessage());
            throw new DynamoSnsException(e.getMessage(), e.getCause());
        }

    }

    @Override
    public String publishNotification(String message, String targetArn) throws DynamoSnsException {
        log.debug("Entering publishNotification, message = {}, targetArn = {} ", message, targetArn);
        try {
            PublishRequest publishRequest =
                    PublishRequest.builder().targetArn(targetArn).message(message).messageStructure("json").build();
            PublishResponse publishResponse = snsClient.publish(publishRequest);
            log.info("{} Message sent. Status was {}", publishResponse.messageId(),
                    publishResponse.sdkHttpResponse().statusCode());
            log.debug("Leaving publishNotification, messageId = {}", publishResponse.messageId());
            return publishResponse.messageId();
        } catch (Exception e) {
            log.error("Error while publishing notification = {}", e.getMessage());
            throw new DynamoSnsException(e.getMessage(), e.getCause());
        }

    }

    @Override
    public GetEndpointAttributesResponse getPlatformEndpoint(String endpointArn) throws DynamoSnsException {
        log.debug("Entering getPlatformEndpoint, endpointArn = {}", endpointArn);
        try {
            GetEndpointAttributesRequest getEndpointAttributesRequest =
                    GetEndpointAttributesRequest.builder().endpointArn(endpointArn).build();
            GetEndpointAttributesResponse getEndpointAttributesResponse =
                    snsClient.getEndpointAttributes(getEndpointAttributesRequest);
            log.debug("Leaving getPlatformEndpoint");
            return getEndpointAttributesResponse;
        } catch (Exception e) {
            log.error("Error while deleting platform endpoint = {}", e.getMessage());
            throw new DynamoSnsException(e.getMessage(), e.getCause());
        }

    }

    @Override
    public void deletePlatformEndpoint(String endpointArn) throws DynamoSnsException {
        log.debug("Entering deletePlatformEndpoint, endpointArn = {}", endpointArn);
        try {
            DeleteEndpointRequest deleteEndpointRequest =
                    DeleteEndpointRequest.builder().endpointArn(endpointArn).build();
            snsClient.deleteEndpoint(deleteEndpointRequest);
            log.debug("Leaving deletePlatformEndpoint");
        } catch (Exception e) {
            log.error("Error while deleting platform endpoint = {}", e.getMessage());
            throw new DynamoSnsException(e.getMessage(), e.getCause());
        }

    }

}
