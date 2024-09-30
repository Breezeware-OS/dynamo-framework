package net.breezeware.dynamo.dynamoformbuilder.dynamoformbuilderwebbff.service;

import static net.breezeware.dynamo.dynamoformbuilder.dynamoformsvc.enumeration.FormStatus.DRAFT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import jakarta.validation.Validator;

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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import net.breezeware.dynamo.dynamoformbuilder.dynamoformbuilderwebbff.dto.FormDto;
import net.breezeware.dynamo.dynamoformbuilder.dynamoformbuilderwebbff.dto.FormVersionDto;
import net.breezeware.dynamo.dynamoformbuilder.dynamoformbuilderwebbff.mapper.MapperService;
import net.breezeware.dynamo.dynamoformbuilder.dynamoformsvc.entity.Form;
import net.breezeware.dynamo.dynamoformbuilder.dynamoformsvc.entity.FormInvitation;
import net.breezeware.dynamo.dynamoformbuilder.dynamoformsvc.entity.FormVersion;
import net.breezeware.dynamo.dynamoformbuilder.dynamoformsvc.enumeration.FormAccessType;
import net.breezeware.dynamo.dynamoformbuilder.dynamoformsvc.enumeration.FormStatus;
import net.breezeware.dynamo.dynamoformbuilder.dynamoformsvc.service.FormInvitationSvcService;
import net.breezeware.dynamo.dynamoformbuilder.dynamoformsvc.service.FormSvcService;
import net.breezeware.dynamo.dynamoformbuilder.dynamoformsvc.service.FormVersionService;
import net.breezeware.dynamo.usermanagement.entity.User;
import net.breezeware.dynamo.usermanagement.service.UserService;
import net.breezeware.dynamo.utils.exception.DynamoException;

@ExtendWith(MockitoExtension.class)
@DisplayName("Form Builder Service Test")
@TestMethodOrder(MethodOrderer.DisplayName.class)
class FormBuilderServiceTest {

    @Mock
    private FormSvcService formSvcService;

    @Mock
    private FormVersionService formVersionService;

    @Mock
    private MapperService mapperService;

    @Mock
    private UserService userService;
    @Mock
    private FormInvitationSvcService formInvitationSvcService;

    @Mock
    private DynamicFormTableManager dynamicFormTableManager;

    @Mock
    private Validator fieldValidator;
    @InjectMocks
    private FormBuilderService formBuilderService;

    private static User getUser() {
        return User.builder().email("Johndoe@gmail.com").idmUserId("c0011bc2-e505-4914-bdd7-5da95ef4ef01").build();
    }

    @Test
    @DisplayName("Process Form with New Form: Should create form and version")
    void testProcessFormWithNewForm() {
        // Arrange
        ObjectNode jsonNode = new ObjectNode(JsonNodeFactory.instance);
        jsonNode.put("question", "What is your name?");

        FormDto formDTO = FormDto.builder().name("Employee Information")
                .description("A form for collecting employee information.").version("v1.0.0").formJson(jsonNode)
                .build();
        Form form = buildForm();
        FormVersion formVersion = buildFormVersion(form, jsonNode);

        when(mapperService.formDtoToForm(any(FormDto.class), eq(FormStatus.PUBLISHED.getStatus()))).thenReturn(form);
        when(formSvcService.create(any(Form.class))).thenReturn(form);
        when(mapperService.formDtoToFromVersion(any(FormDto.class))).thenReturn(formVersion);
        when(formVersionService.create(any(FormVersion.class))).thenReturn(formVersion);

        // Act
        Form result = formBuilderService.processForm(formDTO, FormStatus.PUBLISHED);

        // Assert
        verify(formSvcService).create(form);
        verify(formVersionService).create(formVersion);
        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("Process Form with Existing Form: Should update form and version")
    void testProcessFormWithExistingForm() {
        // Arrange
        ObjectNode jsonNode = new ObjectNode(JsonNodeFactory.instance);
        jsonNode.put("question", "What is your name?");

        FormDto formDTO = FormDto.builder().id(1L).name("Employee Information")
                .description("A form for collecting employee information.").version("v1.0.0").formJson(jsonNode)
                .build();

        Form form = buildForm();
        FormVersion formVersion = buildFormVersion(form, jsonNode);

        when(formSvcService.retrieveById(formDTO.getId())).thenReturn(Optional.of(form));
        when(mapperService.formDtoToForm(any(FormDto.class), eq(FormStatus.PUBLISHED.getStatus()))).thenReturn(form);
        when(formSvcService.update(any(Form.class))).thenReturn(form);
        when(formVersionService.retrieveFormVersion(form, FormStatus.DRAFT.getStatus()))
                .thenReturn(Optional.of(formVersion));
        when(mapperService.formDtoToFromVersion(any(FormDto.class))).thenReturn(formVersion);
        when(formVersionService.update(any(FormVersion.class))).thenReturn(formVersion);

        // Act
        Form result = formBuilderService.processForm(formDTO, FormStatus.PUBLISHED);

        // Assert
        verify(formSvcService).update(form);
        verify(formVersionService).update(formVersion);
        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("Process Form with Existing Form: Should update form and create new version")
    void testProcessFormWithExistingFormShouldUpdateFormAndCreateVersion() {
        // Arrange
        ObjectNode jsonNode = new ObjectNode(JsonNodeFactory.instance);
        jsonNode.put("question", "What is your name?");

        FormDto formDTO = FormDto.builder().id(1L).name("Employee Information")
                .description("A form for collecting employee information.").version("v1.0.0").formJson(jsonNode)
                .build();

        Form form = buildForm();
        FormVersion formVersion = buildFormVersion(form, jsonNode);

        when(formSvcService.retrieveById(formDTO.getId())).thenReturn(Optional.of(form));
        when(mapperService.formDtoToForm(any(FormDto.class), eq(FormStatus.PUBLISHED.getStatus()))).thenReturn(form);
        when(formSvcService.update(any(Form.class))).thenReturn(form);
        when(formVersionService.retrieveFormVersion(form, FormStatus.DRAFT.getStatus())).thenReturn(Optional.empty());
        when(mapperService.formDtoToFromVersion(any(FormDto.class))).thenReturn(formVersion);

        when(formVersionService.create(any(FormVersion.class))).thenReturn(formVersion);

        // Act
        Form result = formBuilderService.processForm(formDTO, FormStatus.PUBLISHED);

        // Assert
        verify(formSvcService).update(form);
        verify(formVersionService).create(formVersion);
        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("Publish Form with New Form: Should create form and version")
    void testPublishFormWithNewForm() {
        // Arrange
        ObjectNode jsonNode = new ObjectNode(JsonNodeFactory.instance);
        jsonNode.put("question", "What is your name?");

        FormDto formDTO = FormDto.builder().name("Employee Information")
                .description("A form for collecting employee information.").version("v1.0.0").formJson(jsonNode)
                .accessType("private").build();
        Form newForm = buildForm();
        FormVersion newFormVersion = buildFormVersion(newForm, jsonNode);

        when(mapperService.formDtoToForm(formDTO, FormStatus.PUBLISHED.getStatus())).thenReturn(newForm);
        when(formSvcService.create(newForm)).thenReturn(newForm);
        when(mapperService.formDtoToFromVersion(any(FormDto.class))).thenReturn(newFormVersion);
        when(formVersionService.create(newFormVersion)).thenReturn(newFormVersion);
        when(formVersionService.retrieveFormVersions(newForm)).thenReturn(List.of(newFormVersion));

        // Act
        Form result = formBuilderService.publishForm(formDTO);

        // Assert
        verify(formSvcService).create(newForm);
        verify(formVersionService).create(newFormVersion);
        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("Publish Form with Existing Form: Should update form and version")
    void testPublishFormWithExistingForm() {
        // Arrange
        ObjectNode jsonNode = new ObjectNode(JsonNodeFactory.instance);
        jsonNode.put("question", "What is your name?");

        FormDto formDTO = FormDto.builder().id(1L).name("Employee Information")
                .description("A form for collecting employee information.").version("v1.0.0").formJson(jsonNode)
                .accessType("private").build();

        Form existingForm = buildForm();
        FormVersion existingFormVersion = buildFormVersion(existingForm, jsonNode);

        when(formSvcService.retrieveById(formDTO.getId())).thenReturn(Optional.of(existingForm));
        when(mapperService.formDtoToForm(formDTO, FormStatus.PUBLISHED.getStatus())).thenReturn(existingForm);
        when(formSvcService.update(existingForm)).thenReturn(existingForm);
        when(formVersionService.retrieveFormVersion(existingForm, FormStatus.DRAFT.getStatus()))
                .thenReturn(Optional.of(existingFormVersion));
        when(mapperService.formDtoToFromVersion(any(FormDto.class))).thenReturn(existingFormVersion);
        when(formVersionService.update(existingFormVersion)).thenReturn(existingFormVersion);
        when(formVersionService.retrieveFormVersions(existingForm)).thenReturn(List.of(existingFormVersion));

        // Act
        Form result = formBuilderService.publishForm(formDTO);

        // Assert
        verify(formSvcService).update(existingForm);
        verify(formVersionService).update(existingFormVersion);
        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("Publish Form with Existing Form: Should update form and create new version")
    void testPublishFormWithExistingFormShouldUpdateFormAndCreateNewVersion() {
        // Arrange
        ObjectNode jsonNode = new ObjectNode(JsonNodeFactory.instance);
        jsonNode.put("question", "What is your name?");

        FormDto formDTO = FormDto.builder().id(1L).name("Employee Information")
                .description("A form for collecting employee information.").version("v1.0.0").formJson(jsonNode)
                .accessType("public").build();

        Form existingForm = buildForm();
        FormVersion existingFormVersion = buildFormVersion(existingForm, jsonNode);

        when(formSvcService.retrieveById(formDTO.getId())).thenReturn(Optional.of(existingForm));
        when(mapperService.formDtoToForm(formDTO, FormStatus.PUBLISHED.getStatus())).thenReturn(existingForm);
        when(formSvcService.update(existingForm)).thenReturn(existingForm);
        when(formVersionService.retrieveFormVersion(existingForm, FormStatus.DRAFT.getStatus()))
                .thenReturn(Optional.empty());
        when(mapperService.formDtoToFromVersion(any(FormDto.class))).thenReturn(existingFormVersion);
        when(formVersionService.create(existingFormVersion)).thenReturn(existingFormVersion);

        when(formVersionService.retrieveFormVersions(existingForm)).thenReturn(List.of(existingFormVersion));

        // Act
        Form result = formBuilderService.publishForm(formDTO);

        // Assert
        verify(formSvcService).update(existingForm);
        verify(formVersionService).create(existingFormVersion);
        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("Draft Form with New Form: Should create form and version")
    void testDraftFormWithNewForm() {
        // Arrange
        ObjectNode jsonNode = new ObjectNode(JsonNodeFactory.instance);
        jsonNode.put("question", "What is your name?");

        FormDto formDTO = FormDto.builder().name("Employee Information")
                .description("A form for collecting employee information.").version("v1.0.0").formJson(jsonNode)
                .build();
        Form newForm = buildForm();
        FormVersion newFormVersion = buildFormVersion(newForm, jsonNode);

        when(mapperService.formDtoToForm(formDTO, DRAFT.getStatus())).thenReturn(newForm);
        when(formSvcService.create(newForm)).thenReturn(newForm);
        when(mapperService.formDtoToFromVersion(formDTO)).thenReturn(newFormVersion);
        when(formVersionService.create(newFormVersion)).thenReturn(newFormVersion);

        // Act
        Form result = formBuilderService.draftForm(formDTO);

        // Assert
        verify(formSvcService).create(newForm);
        verify(formVersionService).create(newFormVersion);
        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("Draft Form with Existing Form: Should update form and version")
    void testDraftFormWithExistingForm() {
        // Arrange
        ObjectNode jsonNode = new ObjectNode(JsonNodeFactory.instance);
        jsonNode.put("question", "What is your name?");

        FormDto formDTO = FormDto.builder().id(1L).name("Employee Information")
                .description("A form for collecting employee information.").version("v1.0.0").formJson(jsonNode)
                .build();

        Form existingForm = buildForm();
        FormVersion existingFormVersion = buildFormVersion(existingForm, jsonNode);

        when(formSvcService.retrieveById(formDTO.getId())).thenReturn(Optional.of(existingForm));
        when(mapperService.formDtoToForm(formDTO, FormStatus.DRAFT.getStatus())).thenReturn(existingForm);
        when(formSvcService.update(existingForm)).thenReturn(existingForm);
        when(formVersionService.retrieveFormVersion(existingForm, FormStatus.DRAFT.getStatus()))
                .thenReturn(Optional.of(existingFormVersion));
        when(mapperService.formDtoToFromVersion(formDTO)).thenReturn(existingFormVersion);
        when(formVersionService.update(existingFormVersion)).thenReturn(existingFormVersion);

        // Act
        Form result = formBuilderService.draftForm(formDTO);

        // Assert
        verify(formSvcService).update(existingForm);
        verify(formVersionService).update(existingFormVersion);
        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("Draft Form with Existing Form: Should update form and create new version")
    void testDraftFormWithExistingFormShouldUpdateFormAndCreateVersion() {
        // Arrange
        ObjectNode jsonNode = new ObjectNode(JsonNodeFactory.instance);
        jsonNode.put("question", "What is your name?");

        FormDto formDTO = FormDto.builder().id(1L).name("Employee Information")
                .description("A form for collecting employee information.").version("v1.0.0").formJson(jsonNode)
                .build();

        Form existingForm = buildForm();
        FormVersion existingFormVersion = buildFormVersion(existingForm, jsonNode);

        when(formSvcService.retrieveById(formDTO.getId())).thenReturn(Optional.of(existingForm));
        when(mapperService.formDtoToForm(formDTO, FormStatus.DRAFT.getStatus())).thenReturn(existingForm);
        when(formSvcService.update(existingForm)).thenReturn(existingForm);
        when(formVersionService.retrieveFormVersion(existingForm, FormStatus.DRAFT.getStatus()))
                .thenReturn(Optional.empty());
        when(mapperService.formDtoToFromVersion(formDTO)).thenReturn(existingFormVersion);

        when(formVersionService.create(existingFormVersion)).thenReturn(existingFormVersion);

        // Act
        Form result = formBuilderService.draftForm(formDTO);

        // Assert
        verify(formSvcService).update(existingForm);
        verify(formVersionService).create(existingFormVersion);
        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("Archive Form with Valid ID: Should archive the form and its versions")
    void archiveFormWithValidId() {

        // Given
        long formId = 1001L;
        ObjectNode jsonNode = new ObjectNode(JsonNodeFactory.instance);
        jsonNode.put("question", "What is your name?");
        Form form = Form.builder().id(1L).name("Employee Information")
                .description("A form for collecting employee information.").version("v1.0.0").status("Published")
                .createdOn(Instant.now()).createdOn(Instant.now()).modifiedOn(Instant.now()).build();
        form.setId(formId);

        // Mock form retrieval
        when(formSvcService.retrieveById(formId)).thenReturn(Optional.of(form));

        // Mock form version retrieval
        List<FormVersion> formVersions = List.of(new FormVersion(), new FormVersion());
        when(formVersionService.retrieveFormVersions(form)).thenReturn(formVersions);

        // When
        formBuilderService.archiveForm(formId);

        // Then
        verify(formSvcService).retrieveById(formId);
        verify(formSvcService).update(form);

        // Verify that form versions are archived
        verify(formVersionService, times(2)).update(any(FormVersion.class));
    }

    @Test
    @DisplayName("Archive Form with Invalid ID: Should throw an exception")
    void archiveFormWithInvalidId() {
        // Given an invalid form ID (less than 1000)
        long formId = 999L;

        // When trying to archive the form
        assertThrows(DynamoException.class, () -> formBuilderService.archiveForm(formId));

        // Verify that form retrieval is not called for an invalid ID
        verify(formSvcService, never()).retrieveById(formId);

    }

    @Test
    @DisplayName("Retrieve Form by Valid ID: Should return the form")
    void retrieveFormByValidId() throws JsonProcessingException {
        // Given a valid form ID
        long formId = 1001;
        Form form = new Form();
        form.setId(formId);
        ObjectNode jsonNode = new ObjectNode(JsonNodeFactory.instance);
        jsonNode.put("question", "What is your name?");

        when(formSvcService.retrieveById(formId)).thenReturn(Optional.of(form));

        FormVersion formVersion = new FormVersion();
        formVersion.setFormJson(jsonNode);
        when(formVersionService.retrieveFormVersions(form)).thenReturn((List.of(formVersion)));

        FormDto expectedFormDto = new FormDto();
        expectedFormDto.setFormJson(jsonNode);
        when(mapperService.formToFormDto(form)).thenReturn(expectedFormDto);

        // When trying to retrieve the form
        FormDto retrievedFormDto = formBuilderService.retrieveForm(formId);

        // Verify the behavior and result
        assertNotNull(retrievedFormDto);
        assertEquals(expectedFormDto, retrievedFormDto);
        verify(formSvcService, times(1)).retrieveById(formId);
        verify(formVersionService, times(1)).retrieveFormVersions(form);
        verify(mapperService, times(1)).formToFormDto(form);
    }

    @Test
    @DisplayName("Retrieve Form by Invalid ID: Should throw an exception")
    void retrieveFormByInvalidId() {
        // Given an invalid form ID (less than 1000)
        long formId = 999L;

        // When trying to retrieve the form
        assertThrows(IllegalArgumentException.class, () -> formBuilderService.retrieveForm(formId));

        // Verify that form retrieval is not called for an invalid ID
        verify(formSvcService, never()).retrieveById(formId);
    }

    @Test
    @DisplayName("Retrieve Forms with Valid Parameters: Should return a page of forms")
    void retrieveFormsWithValidParameters() {
        // Given valid search or filter parameters and pageable settings
        MultiValueMap<String, String> searchOrFilterParameters = new LinkedMultiValueMap<>();
        searchOrFilterParameters.add("form-name", "Employee Information");
        searchOrFilterParameters.add("status", "all");
        Pageable pageable = PageRequest.of(0, 10);
        List<Form> expectedForms = Arrays.asList(new Form(), new Form());

        // Mock form retrieval
        when(formSvcService.retrievePageEntitiesWithPredicate(any(), eq(pageable)))
                .thenReturn(new PageImpl<>(expectedForms));

        // When
        Page<Form> retrievedForms = formBuilderService.retrieveForms(searchOrFilterParameters, pageable);

        // Then
        verify(formSvcService).retrievePageEntitiesWithPredicate(any(), eq(pageable));
        assertEquals(expectedForms, retrievedForms.getContent());
    }

    @Test
    @DisplayName("Retrieve Forms with Invalid Parameters: Should throw an exception")
    void retrieveFormsWithInvalidParameters() {
        // Given invalid search or filter parameters (unsupported parameter)
        MultiValueMap<String, String> searchOrFilterParameters = new LinkedMultiValueMap<>();
        searchOrFilterParameters.add("invalid-param", "value");
        Pageable pageable = PageRequest.of(0, 10);

        // When trying to retrieve forms with invalid parameters
        assertThrows(DynamoException.class, () -> formBuilderService.retrieveForms(searchOrFilterParameters, pageable));

        // Verify that the form retrieval method is not called for invalid parameters
        verify(formSvcService, never()).retrievePageEntitiesWithPredicate(any(), eq(pageable));
    }

    @Test
    @DisplayName("Retrieve Forms with Invalid Sort Criteria: Should throw an exception")
    void retrieveFormsWithInvalidSortCriteria() {
        // Given invalid sort criteria (incorrect format)
        MultiValueMap<String, String> searchOrFilterParameters = new LinkedMultiValueMap<>();
        searchOrFilterParameters.add("sort", "invalid-sort-criteria");
        Pageable pageable = PageRequest.of(0, 10);

        // When trying to retrieve forms with invalid sort criteria
        assertThrows(DynamoException.class, () -> formBuilderService.retrieveForms(searchOrFilterParameters, pageable));

        // Verify that the form retrieval method is not called for invalid sort criteria
        verify(formSvcService, never()).retrievePageEntitiesWithPredicate(any(), eq(pageable));
    }

    @Test
    @DisplayName("Retrieve Forms with Valid Sort Criteria: Should return a page of forms")
    void retrieveFormsWithValidSortCriteria() {
        // Given valid sort criteria
        MultiValueMap<String, String> searchOrFilterParameters = new LinkedMultiValueMap<>();
        searchOrFilterParameters.add("sort", "name,ASC");
        searchOrFilterParameters.add("status", "all");
        Pageable pageable = PageRequest.of(0, 10);
        List<Form> expectedForms = Arrays.asList(new Form(), new Form());

        // Mock form retrieval
        when(formSvcService.retrievePageEntitiesWithPredicate(any(), eq(pageable)))
                .thenReturn(new PageImpl<>(expectedForms));

        // When
        Page<Form> retrievedForms = formBuilderService.retrieveForms(searchOrFilterParameters, pageable);

        // Then
        verify(formSvcService).retrievePageEntitiesWithPredicate(any(), eq(pageable));
        assertEquals(expectedForms, retrievedForms.getContent());
    }

    @Test
    @DisplayName("Retrieve Forms with Valid Form Date: Should return a page of forms")
    void retrieveFormsWithValidFormDate() {
        // Given valid form date parameter
        MultiValueMap<String, String> searchOrFilterParameters = new LinkedMultiValueMap<>();
        searchOrFilterParameters.add("form-date", "2023-09-20T12:00:00Z");
        searchOrFilterParameters.add("status", "all");
        Pageable pageable = PageRequest.of(0, 10);
        List<Form> expectedForms = Arrays.asList(new Form(), new Form());

        // Mock form retrieval
        when(formSvcService.retrievePageEntitiesWithPredicate(any(), eq(pageable)))
                .thenReturn(new PageImpl<>(expectedForms));

        // When
        Page<Form> retrievedForms = formBuilderService.retrieveForms(searchOrFilterParameters, pageable);

        // Then
        verify(formSvcService).retrievePageEntitiesWithPredicate(any(), eq(pageable));
        assertEquals(expectedForms, retrievedForms.getContent());
    }

    @Test
    @DisplayName("Retrieve Forms with Invalid Form Date: Should throw an exception")
    void retrieveFormsWithInvalidFormDate() {
        // Given invalid form date parameter (incorrect format)
        MultiValueMap<String, String> searchOrFilterParameters = new LinkedMultiValueMap<>();
        searchOrFilterParameters.add("form-date", "invalid-date-format");
        Pageable pageable = PageRequest.of(0, 10);

        // When trying to retrieve forms with invalid form date
        assertThrows(DynamoException.class, () -> formBuilderService.retrieveForms(searchOrFilterParameters, pageable));

        // Verify that the form retrieval method is not called for invalid form date
        verify(formSvcService, never()).retrievePageEntitiesWithPredicate(any(), eq(pageable));
    }

    @Test
    @DisplayName("Validate Sort Criteria with Invalid Sort Order: Should throw an exception")
    void validateSortCriteriaWithInvalidSortOrder() {
        // Given invalid sort criteria with an unsupported sort order
        String invalidSortCriteria = "name,invalid-sort-order";

        // When trying to validate the sort criteria
        DynamoException exception =
                assertThrows(DynamoException.class, () -> formBuilderService.validateSortCriteria(invalidSortCriteria));

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertTrue(exception.getMessage().contains("Invalid sort-order [invalid-sort-order] for sort-by [name]"));
    }

    @Test
    @DisplayName("Retrieve Form Versions with Valid Parameters: Should return a page of form versions")
    void retrieveFormVersionsWithValidParameters() {
        // Given a valid form ID, search or filter parameters, and pageable settings
        long formId = 1001L;
        MultiValueMap<String, String> searchOrFilterParameters = new LinkedMultiValueMap<>();
        searchOrFilterParameters.add("version", "v1.0");
        Pageable pageable = PageRequest.of(0, 10);
        List<FormVersion> expectedFormVersions = Arrays.asList(new FormVersion(), new FormVersion());

        // Mock form version retrieval
        when(formVersionService.retrievePageEntitiesWithPredicate(any(), eq(pageable)))
                .thenReturn(new PageImpl<>(expectedFormVersions));

        // When
        Page<FormVersionDto> retrievedFormVersions =
                formBuilderService.retrieveFormVersions(formId, searchOrFilterParameters, pageable);

        // Then
        verify(formVersionService).retrievePageEntitiesWithPredicate(any(), eq(pageable));
        assertEquals(expectedFormVersions.size(), retrievedFormVersions.getContent().size());
    }

    @Test
    @DisplayName("Retrieve Form Versions with Invalid Parameters: Should throw an exception")
    void retrieveFormVersionsWithInvalidParameters() {
        // Given an invalid form ID (less than 1000)
        long formId = 999L;

        // When trying to retrieve form versions with invalid parameters
        assertThrows(IllegalArgumentException.class, () -> formBuilderService.retrieveFormVersions(formId,
                new LinkedMultiValueMap<>(), PageRequest.of(0, 10)));

        // Verify that form version retrieval is not called for an invalid form ID
        verify(formVersionService, never()).retrievePageEntitiesWithPredicate(any(), any());
    }

    @Test
    @DisplayName("Retrieve Form Versions with Invalid Sort Criteria: Should throw an exception")
    void retrieveFormVersionsWithInvalidSortCriteria() {
        // Given invalid sort criteria (incorrect format)
        long formId = 1001L;
        MultiValueMap<String, String> searchOrFilterParameters = new LinkedMultiValueMap<>();
        searchOrFilterParameters.add("sort", "invalid-sort-criteria");
        Pageable pageable = PageRequest.of(0, 10);

        // When trying to retrieve form versions with invalid sort criteria
        assertThrows(DynamoException.class,
                () -> formBuilderService.retrieveFormVersions(formId, searchOrFilterParameters, pageable));

        // Verify that the form version retrieval method is not called for invalid sort
        // criteria
        verify(formVersionService, never()).retrievePageEntitiesWithPredicate(any(), any());
    }

    @Test
    @DisplayName("Retrieve Form Versions with Valid Form Version: Should return a page of form versions")
    void retrieveFormVersionsWithValidFormVersion() {
        // Given a valid form ID and form version parameter
        long formId = 1001L;
        MultiValueMap<String, String> searchOrFilterParameters = new LinkedMultiValueMap<>();
        searchOrFilterParameters.add("version", "v1.0");
        Pageable pageable = PageRequest.of(0, 10);
        List<FormVersion> expectedFormVersions = Arrays.asList(new FormVersion(), new FormVersion());

        // Mock form version retrieval
        when(formVersionService.retrievePageEntitiesWithPredicate(any(), eq(pageable)))
                .thenReturn(new PageImpl<>(expectedFormVersions));

        // When
        Page<FormVersionDto> retrievedFormVersions =
                formBuilderService.retrieveFormVersions(formId, searchOrFilterParameters, pageable);

        // Then
        verify(formVersionService).retrievePageEntitiesWithPredicate(any(), eq(pageable));
        assertEquals(expectedFormVersions.size(), retrievedFormVersions.getContent().size());
    }

    @Test
    @DisplayName("Retrieve Form by ID - Form Not Found: Should throw an exception")
    void retrieveFormByIdFormNotFound() {
        // Given a non-existent form ID
        long formId = 1001L;

        // Mock form retrieval to return an empty Optional
        when(formSvcService.retrieveById(formId)).thenReturn(Optional.empty());

        // When trying to retrieve the form
        DynamoException exception = assertThrows(DynamoException.class, () -> formBuilderService.retrieveForm(formId));

        // Then verify that the exception is thrown with the expected message and status
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
        assertEquals("Form with ID 1001 not found", exception.getMessage());

        // Verify that form retrieval was called
        verify(formSvcService).retrieveById(formId);
    }

    @Test
    @DisplayName("Retrieve Form count by Status - Should return a map of status and count.")
    void retrieveFormStatusCount() {
        // Mock the behavior of retrieveEntitiesCount method
        when(formSvcService.retrieveEntitiesCount(any())).thenReturn(10L);

        // Call the method under test
        Map<String, Long> statusCounts = formBuilderService.retrieveFormStatusCount();

        // Verify that the correct counts are returned
        assertEquals(10L, statusCounts.get(FormStatus.PUBLISHED.name().toLowerCase()));
        assertEquals(10L, statusCounts.get(FormStatus.DRAFT.name().toLowerCase()));
        assertEquals(10L, statusCounts.get(FormStatus.ARCHIVED.name().toLowerCase()));
        assertEquals(30L, statusCounts.get(FormStatus.ALL.name().toLowerCase()));

        // Verify that retrieveEntitiesCount is called with the correct BooleanBuilders
        verify(formSvcService, times(3)).retrieveEntitiesCount(any());
    }

    @Test
    @DisplayName("Retrieve Form with Valid Unique ID")
    public void testRetrieveForm() throws JsonProcessingException {
        // Given
        String uniqueId = "12345678";
        Form form = buildForm();
        form.setAccessType(FormAccessType.PUBLIC.getValue());
        ObjectNode jsonNode = new ObjectNode(JsonNodeFactory.instance);
        jsonNode.put("question", "What is your name?");
        FormVersion formVersion = buildFormVersion(form, jsonNode);
        List<FormVersion> formVersions = new ArrayList<>();
        formVersions.add(formVersion);

        when(formSvcService.retrieveForm(uniqueId)).thenReturn(Optional.of(form));
        when(formVersionService.retrieveFormVersions(form)).thenReturn(formVersions);
        when(mapperService.formToFormDto(form)).thenReturn(buildFormDto());

        // When
        FormDto result = formBuilderService.retrieveForm(uniqueId);

        // Then
        assertEquals("v2.0.0", result.getVersion());
        assertEquals(jsonNode, result.getFormJson());
        verify(formSvcService, times(1)).retrieveForm(uniqueId);
        verify(formVersionService, times(1)).retrieveFormVersions(form);
        verify(mapperService, times(1)).formToFormDto(form);
    }

    @Test
    @DisplayName("Retrieve Form with Valid Unique ID and authenticated user")
    public void testRetrieveForm_ValidUniqueId_PrivateForm_UserAuthenticated_AccessGranted()
            throws JsonProcessingException {
        // Given
        String uniqueId = "12345678";
        String userId = "c0011bc2-e505-4914-bdd7-5da95ef4ef01";
        Form form = buildForm();
        ObjectNode jsonNode = new ObjectNode(JsonNodeFactory.instance);
        jsonNode.put("question", "What is your name?");
        form.setAccessType(FormAccessType.PRIVATE.getValue());
        FormVersion formVersion = buildFormVersion(form, jsonNode);
        formVersion.setStatus(FormStatus.PUBLISHED.getStatus());
        Authentication authentication = new UsernamePasswordAuthenticationToken(userId, null, null);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        when(formSvcService.retrieveForm(uniqueId)).thenReturn(Optional.of(form));
        when(userService.retrieveUserByIdmUserId(userId)).thenReturn(Optional.of(getUser()));
        when(formInvitationSvcService.retrieveFormInvitation(anyString(), anyLong()))
                .thenReturn(Optional.of(new FormInvitation()));
        when(formVersionService.retrieveFormVersions(form)).thenReturn(List.of(formVersion));
        when(mapperService.formToFormDto(form)).thenReturn(buildFormDto());

        // When
        FormDto formDto = formBuilderService.retrieveForm(uniqueId);

        // Then
        assertNotNull(formDto);
        assertEquals(buildFormDto(), formDto);
    }

    @Test
    @DisplayName("Retrieve Form with Valid Unique ID and access denied")
    public void testRetrieveForm_ValidUniqueId_PrivateForm_UserAuthenticated_AccessDenied() {
        // Given
        String uniqueId = "12345678";
        String userId = "c0011bc2-e505-4914-bdd7-5da95ef4ef01";
        Form form = buildForm();
        form.setAccessType(FormAccessType.PRIVATE.getValue());
        Authentication authentication = new UsernamePasswordAuthenticationToken(userId, null);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        when(formSvcService.retrieveForm(uniqueId)).thenReturn(Optional.of(form));

        // When and Then
        assertThrows(DynamoException.class, () -> formBuilderService.retrieveForm(uniqueId));
    }

    @Test
    @DisplayName("Retrieve Form with Null Unique ID")
    public void testRetrieveForm_WithInvalidUniqueId() {
        // Given
        String uniqueId = null;

        // When / Then
        assertThrows(IllegalArgumentException.class, () -> {
            formBuilderService.retrieveForm(uniqueId);
        });

        verify(formSvcService, never()).retrieveForm(any());
        verify(formVersionService, never()).retrieveFormVersions(any());
        verify(mapperService, never()).formToFormDto(any());
    }

    @Test
    @DisplayName("Retrieve Form with Nonexistent Unique ID")
    public void testRetrieveForm_WithNotFoundForm() {
        // Given
        String uniqueId = "nonExistentUniqueId";

        when(formSvcService.retrieveForm(uniqueId)).thenReturn(Optional.empty());

        // When / Then
        assertThrows(DynamoException.class, () -> {
            formBuilderService.retrieveForm(uniqueId);
        });

        verify(formSvcService, times(1)).retrieveForm(uniqueId);
        verify(formVersionService, never()).retrieveFormVersions(any());
        verify(mapperService, never()).formToFormDto(any());
    }

    public Form buildForm() {
        return Form.builder().id(1000L).name("Employee Information")
                .description("A form for collecting employee information.").version("v1.0.0").uniqueId("12345678")
                .status("Published").createdOn(Instant.now()).createdOn(Instant.now()).modifiedOn(Instant.now())
                .build();
    }

    public FormVersion buildFormVersion(Form form, JsonNode jsonNode) {
        return FormVersion.builder().id(1000L).version("v2.0.0").status("Draft").formJson(jsonNode).form(form)
                .createdOn(Instant.now()).modifiedOn(Instant.now()).build();
    }

    public FormDto buildFormDto() throws JsonProcessingException {
        ObjectNode jsonNode = new ObjectNode(JsonNodeFactory.instance);
        jsonNode.put("question", "What is your name?");
        return FormDto.builder().id(1000L).name("Employee Information")
                .description("A form for collecting employee information.").version("v2.0.0").formJson(jsonNode)
                .build();

    }

    public FormInvitation buildFormInvitation() {
        return FormInvitation.builder().id(1L).email("Johndoe@gmail.com").form(buildForm()).build();
    }
}