package net.breezeware.dynamo.dynamoaiwebbff.mapper;

import net.breezeware.dynamo.dynamoaisvc.entity.Model;
import net.breezeware.dynamo.dynamoaiwebbff.dto.ModelRequestDto;
import net.breezeware.dynamo.dynamoaiwebbff.dto.ModelViewDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ModelMapper {
    /**
     * Converts an {@link Model} entity to a {@link ModelViewDto}.
     * @param  model The {@link Model} entity to convert.
     * @return       The resulting {@link ModelViewDto}.
     */
    ModelViewDto modelToModelDto(Model model);

    /**
     * Converts a {@link Model} entity to a {@link ModelRequestDto}.
     * @param  model the {@link Model} entity to convert
     * @return       the resulting {@link ModelRequestDto}
     */
    @Mapping(target = "modelName", source = "model.name")
    ModelRequestDto modelToModelRequestDto(Model model);

}