package net.breezeware.dynamo.dynamoaisvc.entity;

import java.util.UUID;

import lombok.Builder;
import net.breezeware.dynamo.generics.crud.entity.GenericEntity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/**
 * Represents an AI model entity in the Dynamo AI service. This entity stores
 * information about an AI model, such as its unique identifier, name, system
 * prompt, temperature, top_p, top_k, and status.
 */
@Entity
@Table(name = "model", schema = "dynamo_ai")
@Data
@EqualsAndHashCode(callSuper = false)
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Model extends GenericEntity {

    /**
     * The unique identifier of the AI model.
     */
    @Schema(example = "f29a44c8-2e94-4891-9b3d-44c0dc0d52c6", description = "The unique identifier of the AI model.")
    @Column(name = "unique_id", unique = true, nullable = false)
    private UUID uniqueId;

    /**
     * The name of the AI model.
     */
    @Schema(example = "ALM AI model", description = "The name of the AI model.")
    @Column(name = "name")
    private String name;

    /**
     * A system prompt that provides context, instructions, and guidelines to the AI
     * model before presenting it with a question or task.
     */
    @Schema(example = "You're assisting with questions about Application Lifecycle Management (ALM).", description = """
            A system prompt provides context, instructions, and guidelines to the AI model \
            before presenting it with a question or task.""")
    @Column(name = "system_prompt")
    private String systemPrompt;

    /**
     * The temperature setting controls the creativity of generated text. Higher
     * values make the output more random, while lower values make it more focused
     * and predictable.
     */
    @Schema(example = "0.5", description = """
            The temperature setting controls the creativity of generated text. Higher values make the \
            output more random, while lower values make it more focused and predictable.""")
    @Column(name = "temperature")
    private float temperature;

    /**
     * The top_p setting controls the number of possible words considered for
     * generation. A higher top_p value includes more potential words, increasing
     * the diversity of the generated text.
     */
    @Schema(example = "0.8", description = """
            The top_p setting controls the number of possible words considered for generation. A higher top_p \
            value includes more potential words, increasing the diversity of the generated text.""")
    @Column(name = "top_p")
    private float topP;

    /**
     * The top_k sampling limits text generation to the k most likely next words.
     * Lower k values make the output more focused and deterministic, while higher k
     * values introduce more randomness and creativity.
     */
    @Schema(example = "0.8", description = """
            The top_k sampling limits text generation to the k most likely next words. Lower k values make the output \
            more focused and deterministic, while higher k values introduce more randomness and creativity.""")
    @Column(name = "top_k")
    private float topK;

    /**
     * The current status of the AI model.
     */
    @Schema(example = "Training", description = "The current status of the AI model.")
    @Column(name = "status")
    private String status;
}
