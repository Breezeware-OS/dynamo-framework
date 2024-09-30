package net.breezeware.dynamo.dynamoaiwebbff.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ConversationDto {
    /**
     * The message for the AI model.
     */
    @Schema(example = "How can I help you today?", description = "The message for the AI model.")
    @NotBlank(message = "Message cannot be blank.")
    private String message;
}