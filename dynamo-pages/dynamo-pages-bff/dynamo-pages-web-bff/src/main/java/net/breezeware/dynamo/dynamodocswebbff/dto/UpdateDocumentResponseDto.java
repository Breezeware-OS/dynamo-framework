package net.breezeware.dynamo.dynamodocswebbff.dto;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import io.swagger.v3.oas.annotations.media.Schema;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UpdateDocumentResponseDto {

    /**
     * The updated document DTO.
     */
    @Schema(description = "The updated document DTO")
    private DocumentDto documentDto;

    /**
     * Flag indicating whether the document was forked during the update.
     */
    @Schema(description = "Flag indicating whether the document was forked during the update", example = "true")
    private boolean isForked;

    /**
     * The unique identifier of the user who edited the document.
     */
    @Schema(description = "The unique identifier of the user who edited the document",
            example = "4985a786-5b6d-4f90-b599-717c67c3954b")
    private UUID editedUserId;

    /**
     * The username of the user who edited the document.
     */
    @Schema(description = "The username of the user who edited the document", example = "john_doe")
    private String editedUsername;
}
