package net.breezeware.dynamo.dynamodocswebbff.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import jakarta.validation.constraints.NotBlank;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * DTO (Data Transfer Object) representing a collection. This DTO contains
 * information about a collection, such as its name, description, permission
 * settings, creator, creation timestamp, modification timestamp, and associated
 * documents.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CollectionDto {

    /**
     * The id of the Collection.
     */
    @Schema(example = "1", description = "The id of the Collection.")
    private long id;

    /**
     * The unique identifier of the collection.
     */
    @Schema(example = "123e4567-e89b-12d3-a456-426614174000", description = "The unique identifier of the collection.")
    private UUID uniqueId;

    /**
     * The name of the collection.
     */
    @NotBlank(message = "Name is missing or blank")
    @Schema(example = "My Collection", description = "The name of the collection.")
    private String name;

    /**
     * The description of the collection.
     */
    @Schema(example = "This is a sample collection.", description = "The description of the collection.")
    private String description;

    /**
     * The permission settings of the collection.
     */
    @NotBlank(message = "Permission is missing or blank")
    @Schema(example = "Read/Write", description = "The permission settings of the collection.")
    private String permission;

    /**
     * The unique identifier of the user who created the collection.
     */
    @Schema(example = "4985a786-5b6d-4f90-b599-717c67c3954b",
            description = "The unique identifier of the user who created the collection.")
    private UUID createdByUserId;

    /**
     * The timestamp when the collection was created.
     */
    @Schema(example = "2022-04-21T11:19:42.12Z", description = "The timestamp when the collection was created.")
    private Instant createdOn;

    /**
     * The timestamp when the collection was modified.
     */
    @Schema(example = "2022-04-21T11:19:42.12Z", description = "The timestamp when the collection was modified.")
    private Instant modifiedOn;

    /**
     * The list of documents associated with the collection.
     */
    @Schema(description = "The list of documents associated with the collection.")
    private List<DocumentDto> documentList;

}
