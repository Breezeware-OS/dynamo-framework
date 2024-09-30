package net.breezeware.dynamo.dynamoaiwebbff.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * Data Transfer Object for representing the view of a knowledge artifact.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class KnowledgeArtifactViewDto {
    /**
     * The unique identifier of the knowledge artifact.
     */
    @Schema(example = "f29a44c8-2e94-4891-9b3d-44c0dc0d52c6",
            description = "The unique identifier of the knowledge artifact.")
    private UUID uniqueId;

    /**
     * The name of the uploaded knowledge artifact.
     */
    @Schema(example = "sample_attachment.pdf", description = "The name of the uploaded knowledge artifact.")
    private String name;

    /**
     * The type of the knowledge artifact.
     */
    @Schema(example = "application/pdf", description = "The type of the knowledge artifact.")
    private String type;

    /**
     * The size of the knowledge artifact.
     */
    @Schema(example = "1024 KB", description = "The size of the knowledge artifact.")
    private String size;

    /**
     * The status of the knowledge artifact.
     */
    @Schema(example = "Uploaded", description = "The status of the knowledge artifact.")
    private String status;

    /**
     * The timestamp when the knowledge artifact was created.
     */
    @Schema(example = "2022-04-21T11:19:42.12Z", description = "The timestamp when the knowledge artifact was created.")
    private Instant createdOn;

}
