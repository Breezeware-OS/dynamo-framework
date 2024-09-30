package net.breezeware.dynamo.dynamoformbuilder.dynamoformbuilderwebbff.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

import net.breezeware.dynamo.dynamoformbuilder.dynamoformbuilderwebbff.dto.FormDto;
import net.breezeware.dynamo.dynamoformbuilder.dynamoformbuilderwebbff.dto.FormResponseDto;
import net.breezeware.dynamo.dynamoformbuilder.dynamoformbuilderwebbff.dto.FormVersionDto;
import net.breezeware.dynamo.dynamoformbuilder.dynamoformsvc.entity.Form;
import net.breezeware.dynamo.dynamoformbuilder.dynamoformsvc.entity.FormResponse;
import net.breezeware.dynamo.dynamoformbuilder.dynamoformsvc.entity.FormVersion;

/**
 * Service for mapping between different objects and entities, including Form
 * and FormVersion mappings.
 */
@Mapper(componentModel = "spring")
public interface MapperService {
    /**
     * Maps a {@link FormDto} object to a {@link Form} entity with the provided
     * status.
     * @param  formDto The request containing form details.
     * @param  status  The status to set for the Form entity.
     * @return         The mapped Form entity.
     */
    @Mapping(source = "status", target = "status")
    Form formDtoToForm(FormDto formDto, String status);

    /**
     * Maps a {@link Form} entity to a {@link FormVersion} entity.
     * @param  form The Form entity to be mapped to a FormVersion.
     * @return      The mapped FormVersion entity.
     */
    @Mappings({ @Mapping(source = "form", target = "form"), @Mapping(target = "id", ignore = true) })
    FormVersion formToFormVersion(Form form);

    /**
     * Maps a {@link FormVersion} entity to a {@link FormVersionDto} DTO.
     * @param  formVersion The FormVersion entity to be mapped to a FormVersionDto.
     * @return             The mapped FormVersionDto.
     */
    FormVersionDto formVersionToFormVersionDto(FormVersion formVersion);

    /**
     * Converts a FormResponseDto object to a FormResponse object.
     * @param  formResponseDto The FormResponseDto object to convert.
     * @return                 The converted FormResponse object.
     */
    @Mappings({ @Mapping(target = "id", ignore = true), @Mapping(target = "form", ignore = true) })
    FormResponse formResponseDtoToFormResponse(FormResponseDto formResponseDto);

    /**
     * Converts a FormDto object to a FormVersion object.
     * @param  formDto The FormDto object to convert.
     * @return         The converted FormVersion object.
     */
    FormVersion formDtoToFromVersion(FormDto formDto);

    /**
     * Converts a Form object to a FormDto object.
     * @param  form The Form object to convert.
     * @return      The converted FormDto object.
     */
    FormDto formToFormDto(Form form);

}
