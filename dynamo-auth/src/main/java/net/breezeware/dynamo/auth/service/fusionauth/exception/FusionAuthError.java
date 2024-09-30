package net.breezeware.dynamo.auth.service.fusionauth.exception;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

/**
 * FusionAuth error response.
 */
@Data
public class FusionAuthError {

    /**
     * Type of the error occurred.
     */
    @JsonProperty("error")
    private String error;

    /**
     * Brief about the error occurred.
     */
    @JsonProperty("error_description")
    private String errorDescription;

    /**
     * Reason for the error occurrence.
     */
    @JsonProperty("error_reason")
    private String errorReason;
}
