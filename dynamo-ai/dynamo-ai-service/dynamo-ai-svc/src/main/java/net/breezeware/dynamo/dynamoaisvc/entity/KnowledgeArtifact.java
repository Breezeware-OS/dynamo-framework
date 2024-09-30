package net.breezeware.dynamo.dynamoaisvc.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import net.breezeware.dynamo.generics.crud.entity.GenericEntity;

import java.util.UUID;

/**
 * Represents a knowledge artifact entity in the Dynamo AI service. This entity
 * stores information about a knowledge artifact, such as its unique identifier,
 * associated AI model, document key, and status.
 */
@Entity
@Table(name = "knowledge_artifact", schema = "dynamo_ai")
@Data
@EqualsAndHashCode(callSuper = false)
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class KnowledgeArtifact extends GenericEntity {

    /**
     * The unique identifier of the knowledge artifact.
     */
    @Schema(example = "f29a44c8-2e94-4891-9b3d-44c0dc0d52c6",
            description = "The unique identifier of the knowledge artifact.")
    @Column(name = "unique_id", unique = true, nullable = false)
    private UUID uniqueId;

    /**
     * The key identifying the S3 object associated with the knowledge artifact.
     */
    @Schema(example = "attachments/sample_attachment.pdf",
            description = "The key identifying the S3 object associated with the knowledge artifact.")
    @Column(name = "key")
    private String key;

    /**
     * The name of the uploaded knowledge artifact.
     */
    @Schema(example = "sample_attachment.pdf", description = "The name of the uploaded knowledge artifact.")
    @Column(name = "name")
    private String name;

    /**
     * The type of the knowledge artifact.
     */
    @Schema(example = "application/pdf", description = "The type of the knowledge artifact.")
    @Column(name = "type")
    private String type;

    /**
     * The numeric value of the size of the knowledge artifact in bytes.
     */
    @Schema(example = "1024", description = "The numeric value of the size of the knowledge artifact in bytes.")
    @Column(name = "size")
    private long size;

    /**
     * The status of the knowledge artifact.
     */
    @Schema(example = "Uploaded", description = "The status of the knowledge artifact.")
    @Column(name = "status", length = 20)
    private String status;

    /**
     * The AI model associated with the knowledge artifact.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "model_id", referencedColumnName = "unique_id", nullable = false)
    private Model model;
}