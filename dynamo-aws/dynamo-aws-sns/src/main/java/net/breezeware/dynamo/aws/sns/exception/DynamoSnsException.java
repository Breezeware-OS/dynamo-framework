package net.breezeware.dynamo.aws.sns.exception;

import net.breezeware.dynamo.utils.exception.DynamoSdkException;

/**
 * Checked exception for handling all SNS related errors.
 */
public class DynamoSnsException extends DynamoSdkException {

    /**
     * Builds an empty {@link DynamoSnsException} based on
     * {@link DynamoSdkException}.
     */
    public DynamoSnsException() {
        super();
    }

    /**
     * Builds {@link DynamoSnsException} with message and cause based on
     * {@link DynamoSdkException}.
     * @param message error detail.
     * @param cause   cause of the exception.
     */
    public DynamoSnsException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Builds {@link DynamoSnsException} with message based on
     * {@link DynamoSdkException}.
     * @param message error detail.
     */
    public DynamoSnsException(String message) {
        super(message);
    }

    /**
     * Builds {@link DynamoSnsException} with cause based on
     * {@link DynamoSdkException}.
     * @param cause cause of the exception.
     */
    public DynamoSnsException(Throwable cause) {
        super(cause);
    }
}
