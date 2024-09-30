package net.breezeware.dynamo.dynamoaisvc.enumeration;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * Enum representing the various statuses an AI model can have within the Dynamo
 * AI service.
 */
@Getter
@AllArgsConstructor
@Slf4j
public enum ModelStatus {
    /**
     * The model is currently being trained.
     */
    TRAINING("training"),
    /**
     * The model is being tested on a validation dataset.
     */
    VALIDATING("validating"),
    /**
     * Training has finished successfully.
     */
    COMPLETED("completed");

    private final String value;
}