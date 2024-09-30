package net.breezeware.dynamo.dynamoformbuilder.dynamoformbuilderwebbff.dto;

import com.fasterxml.jackson.databind.JsonNode;

import net.breezeware.dynamo.dynamoformbuilder.dynamoformsvc.utils.jsonvalidator.ValidJsonNode;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Data Transfer Object (DTO) representing a form response.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FormResponseDto {
    /**
     * The JSON representation of the form response.
     */
    @Schema(example = "{\"question\": \"What is your name?\"}",
            description = "The JSON representation of the form response")
    @ValidJsonNode(message = "Form JSON is missing or blank")
    private JsonNode responseJson;

    /**
     * The share link key of the form.
     */
    @Schema(example = "6b47e3a3", description = "The share link key of the form.")
    private String formUniqueId;

    /**
     * The email of the user who submitted the form response.
     */
    @Schema(example = "joe@gmail.com", description = "The email of the user who submitted the form response.")
    private String email;

}