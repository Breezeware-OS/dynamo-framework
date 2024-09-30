package net.breezeware.dynamo.aws.s3.exception;

import net.breezeware.dynamo.utils.exception.DynamoSdkException;

/**
 * Checked exception for handling all S3 related errors.
 */
public class DynamoS3Exception extends DynamoSdkException {

    /**
     * Builds {@link DynamoS3Exception} based on {@link DynamoSdkException}.
     */
    public DynamoS3Exception() {
        super();
    }

    /**
     * Builds {@link DynamoS3Exception} with message and cause based on
     * {@link DynamoSdkException}.
     * @param message error detail.
     * @param cause   cause of the exception.
     */
    public DynamoS3Exception(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Builds {@link DynamoS3Exception} with message based on
     * {@link DynamoSdkException}.
     * @param message error detail.
     */
    public DynamoS3Exception(String message) {
        super(message);
    }

    /**
     * Builds {@link DynamoS3Exception} with cause based on
     * {@link DynamoSdkException}.
     * @param cause cause of the exception.
     */
    public DynamoS3Exception(Throwable cause) {
        super(cause);
    }
}
