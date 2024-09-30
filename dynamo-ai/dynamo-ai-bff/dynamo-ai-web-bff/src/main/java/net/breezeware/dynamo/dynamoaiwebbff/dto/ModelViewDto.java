package net.breezeware.dynamo.dynamoaiwebbff.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * Data Transfer Object for representing an AI model in view layer.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ModelViewDto {
    /**
     * The unique identifier of the model.
     */
    @Schema(example = "f29a44c8-2e94-4891-9b3d-44c0dc0d52c6", description = "The unique identifier of the model.")
    private UUID uniqueId;

    /**
     * The name of the model.
     */
    @Schema(example = "ALM AI model", description = "The name of the model.")
    private String name;

    /**
     * The current status of the model.
     */
    @Schema(example = "Training", description = "The current status of the model.")
    private String status;

    /**
     * The timestamp when the model was created.
     */
    @Schema(example = "2022-04-21T11:19:42.12Z", description = "The timestamp when the model was created.")
    private Instant createdOn;
}