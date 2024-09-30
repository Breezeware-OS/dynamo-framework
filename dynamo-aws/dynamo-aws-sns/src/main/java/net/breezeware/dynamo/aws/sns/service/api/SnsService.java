package net.breezeware.dynamo.aws.sns.service.api;

import net.breezeware.dynamo.aws.sns.exception.DynamoSnsException;

import software.amazon.awssdk.services.sns.model.GetEndpointAttributesResponse;

/**
 * AWS simple notification service.
 */
public interface SnsService {

    /**
     * Publishes an SMS text message.
     * @param  message            The message you want to send.
     * @param  phoneNumber        The phone number to which you want to deliver an
     *                            SMS message.
     * @return                    MessageId if SMS successfully sent.
     * @throws DynamoSnsException if error while sending SMS.
     */
    String publishTextSms(String message, String phoneNumber) throws DynamoSnsException;

    /**
     * Creates a platform endpoint for push notifications.
     * @param  token                  Unique identifier created by the notification
     *                                service for an app on a device.
     * @param  platformApplicationArn Returned from CreatePlatformApplication is
     *                                used to create an endpoint.
     * @return                        EndpointArn if platform endpoint successfully
     *                                created.
     * @throws DynamoSnsException     if error while creating platform endpoint.
     */
    String createPlatformEndpoint(String token, String platformApplicationArn) throws DynamoSnsException;

    /**
     * Publishes push notification.
     * @param  message            The message you want to send.
     * @param  targetArn          TargetArn of endpoint to publish notification.
     * @return                    MessageId if notification successfully sent.
     * @throws DynamoSnsException if error while sending notification.
     */
    String publishNotification(String message, String targetArn) throws DynamoSnsException;

    /**
     * Gets a platform endpoint.
     * @param  endpointArn        EndpointArn of endpoint to retrieve.
     * @return                    {@link GetEndpointAttributesResponse}
     * @throws DynamoSnsException if error while retrieving platform endpoint.
     */
    GetEndpointAttributesResponse getPlatformEndpoint(String endpointArn) throws DynamoSnsException;

    /**
     * Deletes a platform endpoint.
     * @param  endpointArn        EndpointArn of endpoint to delete.
     * @throws DynamoSnsException if error while deleting platform endpoint.
     */
    void deletePlatformEndpoint(String endpointArn) throws DynamoSnsException;

}
