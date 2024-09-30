package net.breezeware.dynamo.dynamoformbuilder.dynamoformbuilderwebbff.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
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
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.querydsl.core.BooleanBuilder;

import net.breezeware.dynamo.aws.ses.exception.DynamoSesException;
import net.breezeware.dynamo.aws.ses.service.api.SesService;
import net.breezeware.dynamo.dynamoformbuilder.dynamoformbuilderwebbff.dto.FormInvitationDto;
import net.breezeware.dynamo.dynamoformbuilder.dynamoformsvc.entity.Form;
import net.breezeware.dynamo.dynamoformbuilder.dynamoformsvc.entity.FormInvitation;
import net.breezeware.dynamo.dynamoformbuilder.dynamoformsvc.service.FormInvitationSvcService;
import net.breezeware.dynamo.dynamoformbuilder.dynamoformsvc.service.FormSvcService;
import net.breezeware.dynamo.utils.exception.DynamoException;

@ExtendWith(MockitoExtension.class)
@DisplayName("Form Invitation Service Test")
@TestMethodOrder(MethodOrderer.DisplayName.class)
class FormInvitationServiceTest {

    private final String formUniqueId = "123450900";
    @Mock
    private FormSvcService formSvcService;

    @Mock
    private FormInvitationSvcService formInvitationSvcService;

    @Mock
    private SesService sesService;

    @Mock
    private Validator fieldValidator;

    @InjectMocks
    private FormInvitationService formInvitationService;

    @Test
    @DisplayName("Retrieve Form invitation with Valid Parameters: Should return a page of form invitation")
    void retrieveFormInvitationWithValidParameters() {
        // Given valid search or filter parameters and pageable settings
        MultiValueMap<String, String> searchOrFilterParameters = new LinkedMultiValueMap<>();
        searchOrFilterParameters.add("sort", "id,ASC");
        Pageable pageable = PageRequest.of(0, 10);
        FormInvitation formInvitation = getFormInvitation();
        formInvitation.setEmail("doe@gmail.com");

        List<FormInvitation> formInvitations = Arrays.asList(getFormInvitation(), formInvitation);

        // Mock form retrieval
        when(formInvitationSvcService.retrievePageEntitiesWithPredicate(any(BooleanBuilder.class), eq(pageable)))
                .thenReturn(new PageImpl<>(formInvitations));

        // When
        Page<FormInvitation> formInvitationPage =
                formInvitationService.retrieveFormInvitations(1000, searchOrFilterParameters, pageable);

        // Then
        verify(formInvitationSvcService).retrievePageEntitiesWithPredicate(any(), eq(pageable));
        assertEquals(formInvitations, formInvitationPage.getContent());
        System.out.println(formInvitationPage.getContent().get(0).toString());

    }

    @Test
    @DisplayName("Retrieve Form invitation with Invalid Parameters: Should throw an exception")
    void retrieveFormInvitationWithInvalidParameters() {
        // Given invalid search or filter parameters (unsupported parameter)
        MultiValueMap<String, String> searchOrFilterParameters = new LinkedMultiValueMap<>();
        searchOrFilterParameters.add("invalid-param", "value");
        Pageable pageable = PageRequest.of(0, 10);

        // When trying to retrieve form invitation with invalid parameters
        assertThrows(DynamoException.class,
                () -> formInvitationService.retrieveFormInvitations(1000, searchOrFilterParameters, pageable));

        // Verify that the form invitation retrieval method is not called for invalid
        // parameters
        verify(formSvcService, never()).retrievePageEntitiesWithPredicate(any(), eq(pageable));
    }

    @Test
    @DisplayName("Retrieve Form invitation with Invalid Sort Criteria: Should throw an exception")
    void retrieveFormsInvitationWithInvalidSortCriteria() {
        // Given invalid sort criteria (incorrect format)
        MultiValueMap<String, String> searchOrFilterParameters = new LinkedMultiValueMap<>();
        searchOrFilterParameters.add("sort", "invalid-sort-criteria");
        Pageable pageable = PageRequest.of(0, 10);

        // When trying to retrieve forms with invalid sort criteria
        assertThrows(DynamoException.class,
                () -> formInvitationService.retrieveFormInvitations(1000, searchOrFilterParameters, pageable));

        // Verify that the form invitation retrieval method is not called for invalid
        // sort
        // criteria
        verify(formInvitationSvcService, never()).retrievePageEntitiesWithPredicate(any(), eq(pageable));
    }

    @Test
    @DisplayName("Retrieve form invitation with Invalid Form Date: Should throw an exception")
    void retrieveFormInvitationWithInvalidFormDate() {
        // Given invalid form date parameter (incorrect format)
        MultiValueMap<String, String> searchOrFilterParameters = new LinkedMultiValueMap<>();
        searchOrFilterParameters.add("response-date", "invalid-date-format");
        Pageable pageable = PageRequest.of(0, 10);

        // When trying to retrieve form invitation with invalid form date
        assertThrows(DynamoException.class,
                () -> formInvitationService.retrieveFormInvitations(1000, searchOrFilterParameters, pageable));

        // Verify that the form response retrieval method is not called for invalid form
        // date
        verify(formInvitationSvcService, never()).retrievePageEntitiesWithPredicate(any(), eq(pageable));
    }

    @Test
    @DisplayName("Validate Sort Criteria with Invalid Sort Order: Should throw an exception")
    void validateSortCriteriaWithInvalidSortOrder() {
        // Given invalid sort criteria with an unsupported sort order
        String invalidSortCriteria = "name,invalid-sort-order";

        // When trying to validate the sort criteria
        DynamoException exception = assertThrows(DynamoException.class,
                () -> formInvitationService.validateSortCriteria(invalidSortCriteria));

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertTrue(exception.getMessage().contains("Invalid sort-order [invalid-sort-order] for sort-by [name]"));
    }

    @Test
    @DisplayName("Delete Form invitation - Valid ID")
    public void testDeleteFormInvitation() {
        // Given
        long formResponseId = 1L;

        // When
        formInvitationService.deleteFormInvitation(formResponseId);

        // Then
        verify(formInvitationSvcService, times(1)).delete(formResponseId);
    }

    @Test
    @DisplayName("Delete Form invitation - Invalid ID")
    public void testDeleteFormInvitation_InvalidId() {
        // Given
        long formResponseId = -1L;

        // When / Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            formInvitationService.deleteFormInvitation(formResponseId);
        });
        assertEquals("Invalid Form Invitation ID: -1. Form Invitation IDs must be greater than or equal to 1.",
                exception.getMessage());

        // Ensure that formResponseService.delete() was not invoked
        verify(formInvitationSvcService, never()).delete(anyLong());
    }

    @Test
    @DisplayName("Save Form Invitation with Valid DTO")
    public void testSaveFormInvitation_ValidDto_FormFound_SendEmail() throws DynamoSesException {
        // Given
        FormInvitationDto formInvitationDto = getFormInvitationDto();

        when(formSvcService.retrieveForm(anyString())).thenReturn(Optional.of(getForm()));
        when(formInvitationSvcService.retrieveFormInvitation(anyString(), anyLong())).thenReturn(Optional.empty());

        // When
        formInvitationService.saveFormInvitation(formInvitationDto);

        // Then
        verify(formSvcService).retrieveForm("validFormUniqueId");
        verify(formInvitationSvcService, times(2)).create(any(FormInvitation.class));
    }

    @Test
    @DisplayName("Save Form Invitation with Valid DTO with Exists Email in the List")
    public void testSaveFormInvitation_EmailAlreadyInvited_SkipSendingEmail() throws DynamoSesException {
        // Given
        FormInvitationDto formInvitationDto = new FormInvitationDto();
        formInvitationDto.setFormUniqueId("validFormUniqueId");
        formInvitationDto.setEmailList(List.of("user1@example.com"));

        when(formSvcService.retrieveForm(anyString())).thenReturn(Optional.of(getForm()));
        when(formInvitationSvcService.retrieveFormInvitation(anyString(), anyLong()))
                .thenReturn(Optional.of(getFormInvitation()));

        // When
        formInvitationService.saveFormInvitation(formInvitationDto);

        // Then
        verify(formInvitationSvcService, never()).create(any(FormInvitation.class));
        verify(sesService, never()).sendTemplatedEmail(anyString(), anyString(), anyString(), anyMap());
    }

    private Form getForm() {
        return Form.builder().id(1000L).uniqueId(formUniqueId).build();
    }

    private FormInvitation getFormInvitation() {
        return FormInvitation.builder().form(getForm()).id(1L).email("joe@gamil.com").build();
    }

    private FormInvitationDto getFormInvitationDto() {
        FormInvitationDto formInvitationDto = new FormInvitationDto();
        formInvitationDto.setFormUniqueId("validFormUniqueId");
        formInvitationDto.setEmailList(List.of("user1@example.com", "user2@example.com"));
        return formInvitationDto;
    }

}