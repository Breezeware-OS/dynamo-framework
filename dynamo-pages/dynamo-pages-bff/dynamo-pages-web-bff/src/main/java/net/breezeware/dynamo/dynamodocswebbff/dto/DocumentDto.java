package net.breezeware.dynamo.dynamodocswebbff.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonInclude;

import net.breezeware.dynamo.utils.annotations.ValidUuid;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * DTO (Data Transfer Object) representing a document. This DTO contains
 * information about a document, such as its title, content, version, status,
 * creator, creation timestamp, modification timestamp, and associated metadata.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DocumentDto {

    /**
     * The id of the document.
     */
    @Schema(example = "1", description = "The id of the document.")
    private long id;

    /**
     * The unique identifier of the document.
     */
    @Schema(example = "123e4567-e89b-12d3-a456-426614174000", description = "The unique identifier of the document.")
    private UUID uniqueId;

    /**
     * The unique identifier of the collection to which the document belongs.
     */
    @ValidUuid(message = "Invalid UUID for Collection.")
    @Schema(example = "123e4567-e89b-12d3-a456-426614174001",
            description = "The unique identifier of the collection to which the document belongs.")
    private UUID collectionId;

    /**
     * The unique identifier of the parent document, if applicable.
     */
    @Schema(example = "123e4567-e89b-12d3-a456-426614174002",
            description = "The unique identifier of the parent document, if applicable.")
    private UUID parentId;

    /**
     * The title of the document.
     */
    @Schema(example = "Document Title", description = "The title of the document.")
    private String title;

    /**
     * The content of the document.
     */
    @Schema(example = "Document content goes here.", description = "The content of the document.")
    private String content;

    /**
     * The HTML content of the document.
     */
    @Schema(example = "<html><body><p>HTML content goes here.</p></body></html>",
            description = "The HTML content of the document.")
    private String htmlContent;

    /**
     * The version number of the document.
     */
    @Schema(example = "1", description = "The version number of the document.")
    private long version;

    /**
     * The status of the document.
     */
    @Schema(example = "Draft", description = "The status of the document.")
    private String status;

    /**
     * The unique identifier of the user who created the document.
     */
    @Schema(example = "123e4567-e89b-12d3-a456-426614174003",
            description = "The unique identifier of the user who created the document.")
    private UUID createdByUserId;

    /**
     * The first name of the user who created the document.
     */
    @Schema(example = "John", description = "The first name of the user who created the document.")
    private String createdUserFirstName;

    /**
     * The last name of the user who created the document.
     */
    @Schema(example = "Doe", description = "The last name of the user who created the document.")
    private String createdUserLastName;

    /**
     * The timestamp when the document was published.
     */
    @Schema(example = "2022-04-21T11:19:42.12Z", description = "The timestamp when the document was published.")
    private Instant publishedOn;

    /**
     * The timestamp when the document was archived.
     */
    @Schema(example = "2022-04-21T11:19:42.12Z", description = "The timestamp when the document was archived.")
    private Instant archivedOn;

    /**
     * The timestamp when the document was deleted.
     */
    @Schema(example = "2022-04-21T11:19:42.12Z", description = "The timestamp when the document was deleted.")
    private Instant deletedOn;

    /**
     * The list of child documents, if any.
     */
    @Schema(description = "The list of child documents, if any.")
    private List<DocumentDto> children;

    /**
     * The timestamp when the document was created.
     */
    @Schema(example = "2022-04-21T11:19:42.12Z", description = "The timestamp when the document was created.")
    private Instant createdOn;

    /**
     * The timestamp when the document was last modified.
     */
    @Schema(example = "2022-04-21T11:19:42.12Z", description = "The timestamp when the document was last modified.")
    private Instant modifiedOn;
}
