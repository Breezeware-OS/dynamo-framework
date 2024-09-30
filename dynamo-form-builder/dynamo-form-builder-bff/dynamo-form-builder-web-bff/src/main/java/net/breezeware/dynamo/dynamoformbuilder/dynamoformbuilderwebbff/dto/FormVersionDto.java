package net.breezeware.dynamo.dynamoformbuilder.dynamoformbuilderwebbff.dto;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Data Transfer Object (DTO) representing a form version.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FormVersionDto {
    /**
     * The version of the form.
     */
    @Schema(example = "v1.0.0", description = "The version of the form")
    private String version;

    /**
     * The date when the form was last modified.
     */
    @Schema(description = "The date when the form was last modified")
    private Instant modifiedOn;
}