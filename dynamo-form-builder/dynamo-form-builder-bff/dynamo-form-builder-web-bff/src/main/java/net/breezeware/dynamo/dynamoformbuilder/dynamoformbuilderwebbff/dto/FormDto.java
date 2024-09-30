package net.breezeware.dynamo.dynamoformbuilder.dynamoformbuilderwebbff.dto;

import jakarta.validation.constraints.NotBlank;

import com.fasterxml.jackson.databind.JsonNode;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Represents a request object for publishing or updating a Form entity. This
 * DTO contains the necessary fields for defining the form's attributes.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FormDto {
    /**
     * The unique identifier of the form.
     */
    @Schema(example = "1000", description = "The unique identifier of the form")
    private Long id;

    /**
     * The name of the form.
     */
    @Schema(example = "Employee Information", description = "The name of the form")
    @NotBlank(message = "Form name is missing or blank")
    private String name;

    /**
     * The description of the form.
     */
    @Schema(example = "A form for collecting employee information.", description = "The description of the form")
    @NotBlank(message = "Form description is missing or blank")
    private String description;

    /**
     * The JSON representation of the form.
     */
    @Schema(example = "{\"question\": \"What is your name?\"}", description = "The JSON representation of the form")
    private JsonNode formJson;

    /**
     * The version of the form.
     */
    @Schema(example = "v1.0.0", description = "The version of the form")
    private String version;

    /**
     * The unique identifier of the form.
     */
    @Schema(example = "ascnfhe6", description = "The version of the form")
    private String uniqueId;

    /**
     * The status of the form.
     */
    @Schema(example = "Published", description = "The status of the form")
    private String status;

    /**
     * Represents the access type of the form.
     */
    @Schema(example = "public", description = "Represents the accessType  of the form.")
    private String accessType;

    /**
     * Represents the owner of the form.
     */
    @Schema(example = "owner@gmail.com", description = "Represents the owner  of the form.")
    private String ownerEmail;
}
