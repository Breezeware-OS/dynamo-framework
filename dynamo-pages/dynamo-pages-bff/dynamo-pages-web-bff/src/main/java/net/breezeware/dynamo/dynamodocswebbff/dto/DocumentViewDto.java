package net.breezeware.dynamo.dynamodocswebbff.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import io.swagger.v3.oas.annotations.media.Schema;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentViewDto {

    /**
     * Flag indicating whether the result is paged. Default is true.
     */
    @Builder.Default
    @Schema(description = "Flag indicating whether the result is paged. Default is true.")
    private Boolean paged = Boolean.TRUE;

    /**
     * The data representing the collections.
     */
    @Schema(description = "The data representing the collections.")
    private Object data;
}
