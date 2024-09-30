package net.breezeware.dynamo.dynamoformbuilder.dynamoformbuilderwebbff.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.querydsl.core.BooleanBuilder;

import net.breezeware.dynamo.dynamoformbuilder.dynamoformbuilderwebbff.dto.FormResponseDto;
import net.breezeware.dynamo.dynamoformbuilder.dynamoformbuilderwebbff.mapper.MapperService;
import net.breezeware.dynamo.dynamoformbuilder.dynamoformsvc.entity.Form;
import net.breezeware.dynamo.dynamoformbuilder.dynamoformsvc.entity.FormInvitation;
import net.breezeware.dynamo.dynamoformbuilder.dynamoformsvc.entity.FormResponse;
import net.breezeware.dynamo.dynamoformbuilder.dynamoformsvc.enumeration.FormStatus;
import net.breezeware.dynamo.dynamoformbuilder.dynamoformsvc.service.FormInvitationSvcService;
import net.breezeware.dynamo.dynamoformbuilder.dynamoformsvc.service.FormResponseSvcService;
import net.breezeware.dynamo.dynamoformbuilder.dynamoformsvc.service.FormSvcService;
import net.breezeware.dynamo.utils.exception.DynamoException;

@ExtendWith(MockitoExtension.class)
@DisplayName("Form Response Service Test")
@TestMethodOrder(MethodOrderer.DisplayName.class)
class FormResponseServiceTest {

    private final String formUniqueId = "123450900";
    @Mock
    private FormSvcService formSvcService;

    @Mock
    private FormResponseSvcService formResponseSvcService;

    @Mock
    private MapperService mapperService;
    @Mock
    private FormInvitationSvcService formInvitationSvcService;

    @Mock
    private DynamicFormTableManager dynamicFormTableManager;

    @InjectMocks
    private FormResponseService formResponseService;

    private static ObjectNode getJsonNodes() {
        ObjectNode jsonNode = new ObjectNode(JsonNodeFactory.instance);
        jsonNode.put("question", "What is your name?");
        return jsonNode;
    }

    @Test
    @DisplayName("Save Form Response - Success")
    public void testSaveFormResponse() {
        // Given
        FormResponseDto formResponseDto = new FormResponseDto();
        formResponseDto.setFormUniqueId("uniqueId");

        Form form = getForm();
        form.setStatus(FormStatus.PUBLISHED.getStatus());
        form.setVersion("1.0.0");
        form.setName("test");
        when(formSvcService.retrieveForm(formResponseDto.getFormUniqueId())).thenReturn(Optional.of(form));
        when(formResponseSvcService.create(any())).thenReturn(getFormResponse());
        when(mapperService.formResponseDtoToFormResponse(any(FormResponseDto.class))).thenReturn(getFormResponse());

        // When
        FormResponse savedFormResponse = formResponseService.saveFormResponse(formResponseDto);

        // Then
        assertNotNull(savedFormResponse);
        verify(formSvcService, times(1)).retrieveForm(formResponseDto.getFormUniqueId());
        verify(formResponseSvcService, times(1)).create(any());
        verify(mapperService, times(1)).formResponseDtoToFormResponse(formResponseDto);
    }

    @Test
    @DisplayName("Save Form Response - Form Not Found")
    public void testSaveFormResponse_FormNotFound() {
        // Given
        FormResponseDto formResponseDto = new FormResponseDto();
        formResponseDto.setFormUniqueId("nonExistentUniqueId");

        when(formSvcService.retrieveForm(formResponseDto.getFormUniqueId())).thenReturn(Optional.empty());

        // When / Then
        DynamoException exception = assertThrows(DynamoException.class, () -> {
            formResponseService.saveFormResponse(formResponseDto);
        });
        assertEquals("Form with unique id nonExistentUniqueId is not found", exception.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());

        verify(formSvcService, times(1)).retrieveForm(formResponseDto.getFormUniqueId());
        verify(formResponseSvcService, never()).create(any());
        verify(mapperService, never()).formResponseDtoToFormResponse(formResponseDto);
    }

    @Test
    @DisplayName("Save Form Response - Form Archived")
    public void testSaveFormResponse_FormArchived() {
        // Given
        FormResponseDto formResponseDto = new FormResponseDto();
        formResponseDto.setFormUniqueId("archivedFormId");

        Form form = new Form();
        form.setStatus(FormStatus.ARCHIVED.getStatus());

        when(formSvcService.retrieveForm(formResponseDto.getFormUniqueId())).thenReturn(Optional.of(form));

        // When / Then
        DynamoException exception = assertThrows(DynamoException.class, () -> {
            formResponseService.saveFormResponse(formResponseDto);
        });
        assertEquals("Cannot submit the form as it is archived", exception.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());

        verify(formSvcService, times(1)).retrieveForm(formResponseDto.getFormUniqueId());
        verify(formResponseSvcService, never()).create(any());
        verify(mapperService, never()).formResponseDtoToFormResponse(formResponseDto);
    }

    @Test
    @DisplayName("Save Form Response - Form In Draft")
    public void testSaveFormResponse_FormInDraft() {
        // Given
        FormResponseDto formResponseDto = getFormResponseDto();
        formResponseDto.setFormUniqueId("draftFormId");

        Form form = new Form();
        form.setStatus("draft");
        form.setVersion(null);

        when(formSvcService.retrieveForm(formResponseDto.getFormUniqueId())).thenReturn(Optional.of(form));

        // When / Then
        DynamoException exception = assertThrows(DynamoException.class, () -> {
            formResponseService.saveFormResponse(formResponseDto);
        });
        assertEquals("Cannot submit the form as it is still in draft", exception.getMessage());

        verify(formSvcService, times(1)).retrieveForm(formResponseDto.getFormUniqueId());
        verify(formResponseSvcService, never()).create(any());
        verify(mapperService, never()).formResponseDtoToFormResponse(formResponseDto);
    }

    @Test
    @DisplayName("Retrieve Forms with Valid Parameters: Should return a page of form Responses")
    void retrieveFormsWithValidParameters() {
        // Given valid search or filter parameters and pageable settings
        MultiValueMap<String, String> searchOrFilterParameters = new LinkedMultiValueMap<>();
        searchOrFilterParameters.add("sort", "id,ASC");
        Pageable pageable = PageRequest.of(0, 10);
        FormResponse formResponse = getFormResponse();
        formResponse.setId(2000L);
        List<FormResponse> formResponses = Arrays.asList(getFormResponse(), formResponse);

        // Mock form retrieval
        when(formResponseSvcService.retrievePageEntitiesWithPredicate(any(BooleanBuilder.class), eq(pageable)))
                .thenReturn(new PageImpl<>(formResponses));

        // When
        Page<FormResponse> retrievedFormResponses =
                formResponseService.retrieveFormResponses(1000, searchOrFilterParameters, pageable);

        // Then
        verify(formResponseSvcService).retrievePageEntitiesWithPredicate(any(), eq(pageable));
        assertEquals(formResponses, retrievedFormResponses.getContent());
        System.out.println(retrievedFormResponses.getContent().get(0).toString());

    }

    @Test
    @DisplayName("Retrieve Form Responses with Invalid Parameters: Should throw an exception")
    void retrieveFormsWithInvalidParameters() {
        // Given invalid search or filter parameters (unsupported parameter)
        MultiValueMap<String, String> searchOrFilterParameters = new LinkedMultiValueMap<>();
        searchOrFilterParameters.add("invalid-param", "value");
        Pageable pageable = PageRequest.of(0, 10);

        // When trying to retrieve form response with invalid parameters
        assertThrows(DynamoException.class,
                () -> formResponseService.retrieveFormResponses(1000, searchOrFilterParameters, pageable));

        // Verify that the form response retrieval method is not called for invalid
        // parameters
        verify(formSvcService, never()).retrievePageEntitiesWithPredicate(any(), eq(pageable));
    }

    @Test
    @DisplayName("Retrieve Form Responses with Invalid Sort Criteria: Should throw an exception")
    void retrieveFormsWithInvalidSortCriteria() {
        // Given invalid sort criteria (incorrect format)
        MultiValueMap<String, String> searchOrFilterParameters = new LinkedMultiValueMap<>();
        searchOrFilterParameters.add("sort", "invalid-sort-criteria");
        Pageable pageable = PageRequest.of(0, 10);

        // When trying to retrieve forms with invalid sort criteria
        assertThrows(DynamoException.class,
                () -> formResponseService.retrieveFormResponses(1000, searchOrFilterParameters, pageable));

        // Verify that the form response retrieval method is not called for invalid sort
        // criteria
        verify(formSvcService, never()).retrievePageEntitiesWithPredicate(any(), eq(pageable));
    }

    @Test
    @DisplayName("Retrieve form Response with Invalid Form Date: Should throw an exception")
    void retrieveFormsWithInvalidFormDate() {
        // Given invalid form date parameter (incorrect format)
        MultiValueMap<String, String> searchOrFilterParameters = new LinkedMultiValueMap<>();
        searchOrFilterParameters.add("response-date", "invalid-date-format");
        Pageable pageable = PageRequest.of(0, 10);

        // When trying to retrieve form response with invalid form date
        assertThrows(DynamoException.class,
                () -> formResponseService.retrieveFormResponses(1000, searchOrFilterParameters, pageable));

        // Verify that the form response retrieval method is not called for invalid form
        // date
        verify(formSvcService, never()).retrievePageEntitiesWithPredicate(any(), eq(pageable));
    }

    @Test
    @DisplayName("Validate Sort Criteria with Invalid Sort Order: Should throw an exception")
    void validateSortCriteriaWithInvalidSortOrder() {
        // Given invalid sort criteria with an unsupported sort order
        String invalidSortCriteria = "name,invalid-sort-order";

        // When trying to validate the sort criteria
        DynamoException exception = assertThrows(DynamoException.class,
                () -> formResponseService.validateSortCriteria(invalidSortCriteria));

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertTrue(exception.getMessage().contains("Invalid sort-order [invalid-sort-order] for sort-by [name]"));
    }

    @Test
    @DisplayName("Delete Form Response - Valid ID")
    public void testDeleteFormResponse() {
        // Given
        long formResponseId = 1L;

        // When
        formResponseService.deleteFormResponse(formResponseId);

        // Then
        verify(formResponseSvcService, times(1)).delete(formResponseId);
    }

    @Test
    @DisplayName("Delete Form Response - Invalid ID")
    public void testDeleteFormResponse_InvalidId() {
        // Given
        long formResponseId = -1L;

        // When / Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            formResponseService.deleteFormResponse(formResponseId);
        });
        assertEquals("Invalid Form Response ID: -1. Form Response IDs must be greater than or equal to 1.",
                exception.getMessage());

        // Ensure that formResponseService.delete() was not invoked
        verify(formResponseSvcService, never()).delete(anyLong());
    }

    private Form getForm() {
        return Form.builder().id(1000L).uniqueId(formUniqueId).accessType("public").build();
    }

    private FormResponse getFormResponse() {
        return FormResponse.builder().id(1000L).responseJson(getJsonNodes()).form(getForm()).build();
    }

    private FormResponseDto getFormResponseDto() {
        ObjectNode jsonNode = getJsonNodes();
        FormResponseDto formResponseDto = new FormResponseDto();
        formResponseDto.setFormUniqueId(formUniqueId);
        formResponseDto.setResponseJson(jsonNode);
        return formResponseDto;
    }

    private FormInvitation getFormInvitation() {
        return FormInvitation.builder().form(getForm()).id(1L).email("joe@gamil.com").build();
    }

}