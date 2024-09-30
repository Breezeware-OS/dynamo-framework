package net.breezeware.dynamo.dynamoaiwebbff.mapper;

import net.breezeware.dynamo.dynamoaisvc.entity.KnowledgeArtifact;
import net.breezeware.dynamo.dynamoaiwebbff.dto.KnowledgeArtifactViewDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface KnowledgeArtifactMapper {
    /**
     * Converts a KnowledgeArtifact entity to a KnowledgeArtifactViewDto.
     * @param  knowledgeArtifact the KnowledgeArtifact entity to convert
     * @return                   the corresponding KnowledgeArtifactViewDto
     */
    @Mapping(target = "size", expression = "java(formatFileSize(knowledgeArtifact.getSize()))")
    KnowledgeArtifactViewDto knowledgeArtifactToKnowledgeArtifactViewDto(KnowledgeArtifact knowledgeArtifact);

    /**
     * Formats a file size in bytes into a human-readable string with appropriate
     * units.
     * @param  size the size of the file in bytes
     * @return      a human-readable string representing the file size with
     *              appropriate units
     */
    default String formatFileSize(long size) {
        if (size <= 0) {
            return "0 B";
        }

        final String[] units = new String[] { "B", "KB", "MB", "GB", "TB" };
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return new java.text.DecimalFormat("#,##0.#").format(size / Math.pow(1024, digitGroups)) + " "
                + units[digitGroups];
    }

}
