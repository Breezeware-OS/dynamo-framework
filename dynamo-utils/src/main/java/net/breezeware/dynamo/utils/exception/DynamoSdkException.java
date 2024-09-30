package net.breezeware.dynamo.utils.exception;

public class DynamoSdkException extends Exception {

    public DynamoSdkException() {
        super();
    }

    public DynamoSdkException(String message, Throwable cause) {
        super(message, cause);
    }

    public DynamoSdkException(String message) {
        super(message);
    }

    public DynamoSdkException(Throwable cause) {
        super(cause);
    }

}
