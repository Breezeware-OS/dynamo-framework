package net.breezeware.dynamo.utils.exception;

import java.io.Serial;
import java.util.List;

import org.springframework.http.HttpStatus;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * User-defined exception class.
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class DynamoException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * The error message was provided during the exception thrown.
     */
    private String message;

    /**
     * The error messages associated with the exception. Each message represents a
     * specific error or validation failure.
     */
    private List<String> errorMessages;

    /**
     * The HttpStatus for the exception occurred.
     */
    private HttpStatus status;

    public DynamoException(String message, HttpStatus status) {
        super();
        this.message = message;
        this.status = status;
    }

    public DynamoException(List<String> errorMessages, HttpStatus status) {
        super();
        this.errorMessages = errorMessages;
        this.status = status;
    }

}
