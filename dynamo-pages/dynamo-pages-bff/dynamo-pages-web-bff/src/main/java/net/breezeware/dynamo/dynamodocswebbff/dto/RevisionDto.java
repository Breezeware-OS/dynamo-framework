package net.breezeware.dynamo.dynamodocswebbff.dto;

import java.time.Instant;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import io.swagger.v3.oas.annotations.media.Schema;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RevisionDto {

    /**
     * The id of the revision.
     */
    @Schema(example = "123", description = "The id of the revision.")
    private long id;

    /**
     * The unique identifier of the revision.
     */
    @Schema(example = "98d7c2fe-f611-4e09-ae80-2062b895a41b", description = "The unique identifier of the revision.")
    private UUID uniqueId;

    /**
     * The unique identifier of the document associated with the revision.
     */
    @Schema(example = "a5e18a53-79ab-4928-87fc-f189a94e4f12",
            description = "The unique identifier of the document associated with the revision.")
    private UUID documentId;

    /**
     * The unique identifier of the collection to which the document belongs.
     */
    @Schema(example = "7ef6389d-5428-4a02-8307-384f8a228c10",
            description = "The unique identifier of the collection to which the document belongs.")
    private UUID collectionId;

    /**
     * The title of the revision.
     */
    @Schema(example = "Updated Proposal", description = "The title of the revision.")
    private String title;

    /**
     * The content of the revision.
     */
    @Schema(example = "Updated content goes here...", description = "The content of the revision.")
    private String content;

    /**
     * The HTML content of the revision.
     */
    @Schema(example = "<html><body><p>Updated HTML content goes here...</p></body></html>",
            description = "The HTML content of the revision.")
    private String htmlContent;

    /**
     * The version number of the revision.
     */
    @Schema(example = "2", description = "The version number of the revision.")
    private long version;

    /**
     * The status of the revision.
     */
    @Schema(example = "Draft", description = "The status of the revision.")
    private String status;

    /**
     * The timestamp when the revision was edited.
     */
    @Schema(example = "2023-05-15T08:30:00Z", description = "The timestamp when the revision was edited.")
    private Instant editedOn;

    /**
     * The unique identifier of the user who edited the revision.
     */
    @Schema(example = "d9676182-f726-4dd9-8e7e-b41cf195fd4f",
            description = "The unique identifier of the user who edited the revision.")
    private UUID editedByUserId;

    /**
     * The first name of the user who edited the revision.
     */
    @Schema(example = "John", description = "The first name of the user who edited the revision.")
    private String editedUserFirstName;

    /**
     * The last name of the user who edited the revision.
     */
    @Schema(example = "Doe", description = "The last name of the user who edited the revision.")
    private String editedUserLastName;

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
