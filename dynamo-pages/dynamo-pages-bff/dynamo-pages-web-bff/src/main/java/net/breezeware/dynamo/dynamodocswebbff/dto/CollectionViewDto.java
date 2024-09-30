package net.breezeware.dynamo.dynamodocswebbff.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO (Data Transfer Object) representing a view of collections. This DTO
 * contains information about collections, such as whether the result is paged
 * and the data representing the collections.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CollectionViewDto {

    /**
     * Flag indicating whether the result is paged. Default is true.
     */
    @Builder.Default
    private Boolean paged = Boolean.TRUE;

    /**
     * The data representing the collections.
     */
    private Object data;
}
