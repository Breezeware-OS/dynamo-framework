package net.breezeware.dynamo.utils.exception;

import java.util.List;

import lombok.Data;

/**
 * Error response provided to client.
 */
@Data
public class ErrorResponse {

    /**
     * The status code for the exception occurred.
     */
    private int statusCode;

    /**
     * The status code name for the exception occurred.
     */
    private String message;

    /**
     * List of error messages with the brief notes.
     */
    private List<String> details;

}