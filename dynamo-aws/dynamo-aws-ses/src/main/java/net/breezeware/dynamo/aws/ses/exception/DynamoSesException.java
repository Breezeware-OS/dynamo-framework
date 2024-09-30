package net.breezeware.dynamo.aws.ses.exception;

import net.breezeware.dynamo.utils.exception.DynamoSdkException;

/**
 * Checked exception for handling all SES related errors.
 */
public class DynamoSesException extends DynamoSdkException {

    /**
     * Builds an empty {@link DynamoSesException} based on
     * {@link DynamoSdkException}.
     */
    public DynamoSesException() {
        super();
    }

    /**
     * Builds {@link DynamoSesException} with message and cause based on
     * {@link DynamoSdkException}.
     * @param message error detail.
     * @param cause   cause of the exception.
     */
    public DynamoSesException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Builds {@link DynamoSesException} with message based on
     * {@link DynamoSdkException}.
     * @param message error detail.
     */
    public DynamoSesException(String message) {
        super(message);
    }

    /**
     * Builds {@link DynamoSesException} with cause based on
     * {@link DynamoSdkException}.
     * @param cause cause of the exception.
     */
    public DynamoSesException(Throwable cause) {
        super(cause);
    }
}
