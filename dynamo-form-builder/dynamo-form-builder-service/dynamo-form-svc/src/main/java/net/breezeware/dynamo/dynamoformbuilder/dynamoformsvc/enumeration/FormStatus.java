package net.breezeware.dynamo.dynamoformbuilder.dynamoformsvc.enumeration;

import java.util.Arrays;
import java.util.Optional;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * Enum representing the status of a form, which can be either "Published" or
 * "Draft."
 */
@Getter
@AllArgsConstructor
@Slf4j
public enum FormStatus {
    /**
     * Represents a published form status.
     */
    PUBLISHED("Published"),

    /**
     * Represents a draft form status.
     */
    DRAFT("Draft"),
    /**
     * Represents an archived form status.
     */
    ARCHIVED("archived"),

    /**
     * Represents all form statuses.
     */
    ALL("All");

    private final String status;

    public static Optional<FormStatus> retrieveFormStatus(String value) {
        log.info("Entering retrieveFormStatus(), value: {}", value);
        Optional<FormStatus> optionalFormStatus = Arrays.stream(values())
                .filter(formStatus -> formStatus.status.equalsIgnoreCase(value.toLowerCase())).findFirst();
        log.info("Leaving retrieveFormStatus(), is FormStatus available for value: {} = {}", value,
                optionalFormStatus.isPresent());
        return optionalFormStatus;
    }
}
