package net.breezeware.dynamo.dynamoaiwebbff.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ModelUpdateDto {
    /**
     * The name of the model.
     */
    @Schema(example = "ALM model", description = "The name of the model.")
    @JsonProperty("model-name")
    private String modelName;
}