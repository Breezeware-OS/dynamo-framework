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
import org.hibernate.annotations.Array;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.UUID;

/**
 * Represents a vector store entity in the Dynamo AI service. This entity stores
 * information about document embeddings, including content, metadata, and the
 * associated AI model.
 */
@Entity
@Table(name = "vector_store", schema = "dynamo_ai")
@Data
@EqualsAndHashCode(callSuper = false)
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class VectorStore extends GenericEntity {

    /**
     * The unique identifier of the vector store entry.
     */
    @Schema(example = "b3b7a3e0-89a3-4c89-bd26-0d84a658915d",
            description = "The unique identifier of the vector store entry.")
    @Column(name = "unique_id", unique = true, nullable = false)
    private UUID uniqueId;

    /**
     * The content of the document associated with the vector store entry.
     */
    @Schema(example = "This is a sample document content.",
            description = "The content of the document associated with the vector store entry.")
    @Column(name = "content")
    private String content;

    /**
     * The metadata of the document in JSON format.
     */
    @Schema(example = """
            {"file_name": "4f31908a-6d1f-48f9-b7b6-a22b4c9e1e97.pdf", "page_number": 1}
            """, description = "The metadata of the document in JSON format.")
    @Column(name = "metadata")
    @JdbcTypeCode(SqlTypes.JSON)
    private String metadata;

    /**
     * The embedding vector of the document.
     */
    @Schema(example = "[0.123, 0.456, 0.789, ...]", description = "The embedding vector of the document.")
    @Column(name = "embedding")
    @JdbcTypeCode(SqlTypes.VECTOR)
    @Array(length = 1536)
    private float[] embedding;

    /**
     * The AI model associated with the vector store entry.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "model_id", referencedColumnName = "unique_id", nullable = false)
    private Model model;

    /**
     * The knowledge artifact associated with the vector store entry.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "knowledge_artifact_id", referencedColumnName = "unique_id", nullable = false)
    private KnowledgeArtifact knowledgeArtifact;
}