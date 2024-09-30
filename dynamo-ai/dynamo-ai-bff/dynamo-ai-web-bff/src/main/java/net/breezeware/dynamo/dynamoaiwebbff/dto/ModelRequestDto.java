package net.breezeware.dynamo.dynamoaiwebbff.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ModelRequestDto {

    /**
     * A system prompt that provides context, instructions, and guidelines to the AI
     * model before presenting it with a question or task.
     */
    @Schema(example = "You're assisting with questions about Application Lifecycle Management (ALM).", description = """
            A system prompt provides context, instructions, and guidelines to the AI model \
            before presenting it with a question or task.""")
    private String systemPrompt;

    /**
     * The temperature setting controls the creativity of generated text. Higher
     * values make the output more random, while lower values make it more focused
     * and predictable.
     */
    @Schema(example = "0.5", description = """
            The temperature setting controls the creativity of generated text. Higher values make the \
            output more random, while lower values make it more focused and predictable.""")
    private float temperature;

    /**
     * The top_p setting controls the number of possible words considered for
     * generation. A higher top_p value includes more potential words, increasing
     * the diversity of the generated text.
     */
    @Schema(example = "0.8", description = """
            The top_p setting controls the number of possible words considered for generation. A higher top_p \
            value includes more potential words, increasing the diversity of the generated text.""")
    private float topP;

    /**
     * The name of the model.
     */
    @Schema(example = "ALM model", description = "The name of the model.")
    @NotBlank(message = "Model name is missing or blank.")
    private String modelName;
}
