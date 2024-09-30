package net.breezeware.dynamo.dynamodocssvc.enumeration;

import java.util.Arrays;
import java.util.Optional;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * Enumeration representing the status of a document. This enum defines various
 * statuses that a document can have, such as DRAFTED, PUBLISHED, etc.
 */
@Getter
@AllArgsConstructor
@Slf4j
public enum DocumentStatus {

    /**
     * The document is in a drafted state, indicating that it is being worked on but
     * not yet finalized.
     */
    DRAFTED("drafted"),

    /**
     * The document has been updated with changes.
     */
    UPDATED("updated"),

    /**
     * The document has been published and is available to be viewed or accessed by
     * others.
     */
    PUBLISHED("published"),

    /**
     * The document has been deleted and is no longer accessible.
     */
    DELETED("deleted"),

    /**
     * The document has been archived for future reference but is not actively used.
     */
    ARCHIVED("archived"),

    /**
     * The document has been created but not yet processed further.
     */
    CREATED("created"),

    /**
     * The document has been forked from another document.
     */
    FORKED("forked");

    private final String value;

    /**
     * Retrieves the DocumentStatus enum based on the given string value.
     * @param  value The string value representing the document status.
     * @return       An Optional containing the corresponding DocumentStatus enum if
     *               found, or empty if not found.
     */
    public static Optional<DocumentStatus> retrieveDocumentStatus(String value) {
        log.info("Entering retrieveDocumentStatus(), value: {}", value);
        Optional<DocumentStatus> optionalDocumentStatus = Arrays.stream(values())
                .filter(permission -> permission.name().equalsIgnoreCase(value.toLowerCase())).findFirst();
        log.info("Leaving retrieveDocumentStatus(), is DocumentStatus available for value: {} = {}", value,
                optionalDocumentStatus.isPresent());
        return optionalDocumentStatus;
    }
}
