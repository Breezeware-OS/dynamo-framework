package net.breezeware.dynamo.dynamoaiwebbff.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for creating an AI model.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ModelCreateDto {

    /**
     * The name of the model.
     */
    @Schema(example = "ALM model", description = "The name of the model.")
    @NotBlank(message = "Model name is missing or blank.")
    @JsonProperty("model-name")
    private String modelName;
}