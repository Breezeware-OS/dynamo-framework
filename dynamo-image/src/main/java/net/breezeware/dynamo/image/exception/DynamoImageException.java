package net.breezeware.dynamo.image.exception;

import net.breezeware.dynamo.utils.exception.DynamoSdkException;

public class DynamoImageException extends DynamoSdkException {

    /**
     * Builds an empty {@link DynamoImageException} based on
     * {@link DynamoSdkException}.
     */
    public DynamoImageException() {
        super();
    }

    /**
     * Builds {@link DynamoImageException} with message and cause based on
     * {@link DynamoSdkException}.
     * @param message error detail.
     * @param cause   cause of the exception.
     */
    public DynamoImageException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Builds {@link DynamoImageException} with message based on
     * {@link DynamoSdkException}.
     * @param message error detail.
     */
    public DynamoImageException(String message) {
        super(message);
    }

    /**
     * Builds {@link DynamoImageException} with cause based on
     * {@link DynamoSdkException}.
     * @param cause cause of the exception.
     */
    public DynamoImageException(Throwable cause) {
        super(cause);
    }
}
