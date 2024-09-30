package net.breezeware.dynamo.dynamoaisvc.enumeration;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * Enum representing the status of a knowledge artifact.
 */
@Getter
@AllArgsConstructor
@Slf4j
public enum KnowledgeArtifactStatus {
    /**
     * Status indicating that the knowledge artifact has been uploaded.
     */
    UPLOADED("uploaded"),

    EMBEDDING("embedding"),

    /**
     * Status indicating that the knowledge artifact has been trained.
     */
    EMBEDDED("embedded");

    private final String value;
}