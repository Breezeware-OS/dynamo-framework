package net.breezeware.dynamo.dynamoformbuilder.dynamoformbuilderwebbff.service;

import java.time.DateTimeException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;

import com.querydsl.core.BooleanBuilder;

import net.breezeware.dynamo.aws.ses.exception.DynamoSesException;
import net.breezeware.dynamo.aws.ses.service.api.SesService;
import net.breezeware.dynamo.dynamoformbuilder.dynamoformbuilderwebbff.dto.FormInvitationDto;
import net.breezeware.dynamo.dynamoformbuilder.dynamoformsvc.entity.Form;
import net.breezeware.dynamo.dynamoformbuilder.dynamoformsvc.entity.FormInvitation;
import net.breezeware.dynamo.dynamoformbuilder.dynamoformsvc.entity.QFormInvitation;
import net.breezeware.dynamo.dynamoformbuilder.dynamoformsvc.enumeration.FormInvitationStatus;
import net.breezeware.dynamo.dynamoformbuilder.dynamoformsvc.service.FormInvitationSvcService;
import net.breezeware.dynamo.dynamoformbuilder.dynamoformsvc.service.FormSvcService;
import net.breezeware.dynamo.usermanagement.entity.User;
import net.breezeware.dynamo.usermanagement.service.UserService;
import net.breezeware.dynamo.utils.exception.DynamoException;
import net.breezeware.dynamo.utils.exception.ValidationExceptionUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class FormInvitationService {

    private static final Set<String> DEFAULT_VALID_PARAMETERS = Set.of("page-no", "page-size", "sort", "search");
    private final Validator fieldValidator;
    private final FormInvitationSvcService formInvitationSvcService;
    private final FormSvcService formSvcService;
    private final UserService userService;
    private final SesService sesService;
    @Value("${form.invitation.email.template}")
    public String templateName;
    @Value("${form.invitation.sender.email}")
    public String senderEmail;

    @Value("${form.publish.url}")
    public String publishUrl;

    /**
     * Saves a form invitation using the provided data transfer object.
     * @param formInvitationDto The data transfer object containing form invitation
     *                          details.
     */
    public void saveFormInvitation(FormInvitationDto formInvitationDto) {
        log.info("Entering saveFormInvitation()");
        Set<ConstraintViolation<FormInvitationDto>> fieldViolations = fieldValidator.validate(formInvitationDto);
        ValidationExceptionUtils.handleException(fieldViolations);
        Form form = retrieveFormFromDto(formInvitationDto);
        saveOrUpdateFormInvitations(form, formInvitationDto.getEmailList());
        log.info("Leaving saveFormInvitation()");
    }

    /**
     * Retrieves a Form object from the provided data transfer object.
     * @param  formInvitationDto The data transfer object containing form details.
     * @return                   The retrieved Form object.
     * @throws DynamoException   If the form with the specified unique ID is not
     *                           found.
     */
    private Form retrieveFormFromDto(FormInvitationDto formInvitationDto) {
        log.info("Entering retrieveFormFromDto()");
        Form form = formSvcService.retrieveForm(formInvitationDto.getFormUniqueId())
                .orElseThrow(() -> new DynamoException(
                        String.format("Form with unique id %s is not found", formInvitationDto.getFormUniqueId()),
                        HttpStatus.NOT_FOUND));
        log.info("Leaving retrieveFormFromDto()");
        return form;
    }

    /**
     * Saves or updates form invitations for the specified form and email list.
     * @param form      The Form object for which invitations are being saved or
     *                  updated.
     * @param emailList The list of email addresses to send invitations to.
     */
    private void saveOrUpdateFormInvitations(Form form, List<String> emailList) {
        log.info("Entering saveOrUpdateFormInvitations()");
        for (String email : emailList) {
            boolean invitationNotExists =
                    formInvitationSvcService.retrieveFormInvitation(email, form.getId()).isEmpty();
            if (invitationNotExists) {
                FormInvitation formInvitation = createFormInvitation(email, form);
                formInvitationSvcService.create(formInvitation);
                sendInvitationEmail(form, email);
            }

        }

        log.info("Leaving saveOrUpdateFormInvitations()");
    }

    /**
     * Creates a FormInvitation object with the provided email and form details.
     * @param  email The email address of the recipient.
     * @param  form  The Form object related to the invitation.
     * @return       The created FormInvitation object.
     */
    private FormInvitation createFormInvitation(String email, Form form) {
        log.info("Entering createFormInvitation()");
        FormInvitation formInvitation = new FormInvitation();
        formInvitation.setEmail(email);
        formInvitation.setForm(form);
        formInvitation.setStatus(FormInvitationStatus.INVITED.getValue());
        log.info("Leaving createFormInvitation()");
        return formInvitation;
    }

    /**
     * Sends an invitation email for the specified form and email address.
     * @param  form            The Form object related to the invitation.
     * @param  email           The email address of the recipient.
     * @throws DynamoException If there is an issue sending the email.
     */
    private void sendInvitationEmail(Form form, String email) {
        log.info("Entering sendInvitationEmail()");
        try {
            HashMap<String, Object> templateData = new HashMap<>();
            templateData.put("form-name", form.getName());
            templateData.put("form-link", publishUrl + "/" + form.getUniqueId());
            sesService.sendTemplatedEmail(senderEmail, email, templateName, templateData);
            log.info("Leaving sendInvitationEmail()");
        } catch (DynamoSesException e) {
            throw new DynamoException(e.getMessage(), HttpStatus.BAD_REQUEST);
        }

    }

    /**
     * Retrieves a page of form invitations associated with the specified form ID,
     * applying optional search or filter parameters.
     * @param  formId                   The ID of the form for which invitations are
     *                                  to be retrieved.
     * @param  searchOrFilterParameters A map containing search or filter parameters
     *                                  and their values.
     * @param  pageable                 The pagination information.
     * @return                          A page of FormInvitation objects.
     */
    public Page<FormInvitation> retrieveFormInvitations(long formId,
            MultiValueMap<String, String> searchOrFilterParameters, Pageable pageable) {
        log.info("Entering retrieveFormInvitations()");
        if (formId < 1000) {
            throw new IllegalArgumentException(
                    "Invalid Form ID: %s. Form IDs must be greater than or equal to 1000.".formatted(formId));
        }

        // Validate query parameters
        Set<String> validParameters = new HashSet<>(DEFAULT_VALID_PARAMETERS);
        validParameters.add("form-invitation-date");
        validParameters.add("email");
        validateParameters(validParameters, searchOrFilterParameters.keySet());

        // Validate sort criteria
        validateSortCriteria(searchOrFilterParameters.getFirst("sort"));

        // Build a predicate for search or filter criteria
        BooleanBuilder booleanBuilder = buildSearchOrFilterPredicate(searchOrFilterParameters);
        booleanBuilder.and(QFormInvitation.formInvitation.form.id.eq(formId));
        // Retrieve a paginated list of Form invitation entities with the predicate
        Page<FormInvitation> formInvitations =
                formInvitationSvcService.retrievePageEntitiesWithPredicate(booleanBuilder, pageable);

        log.info("Leaving retrieveFormInvitations()");
        return formInvitations;
    }

    /**
     * Validates that the actual parameters are among the allowed parameters.
     * @param  allowedParameters A Set of allowed parameter names.
     * @param  actualParameters  A Set of actual parameter names.
     * @throws DynamoException   if unknown parameters are found.
     */
    private void validateParameters(Set<String> allowedParameters, Set<String> actualParameters)
            throws DynamoException {
        log.info("Entering validateParameters()");
        List<String> invalidParameters =
                actualParameters.stream().filter(param -> !allowedParameters.contains(param)).toList();
        if (!invalidParameters.isEmpty()) {
            log.error("Parameter(s) {} not supported!", invalidParameters);
            throw new DynamoException("Unknown parameter(s) %s found".formatted(invalidParameters),
                    HttpStatus.BAD_REQUEST);
        }

        log.info("Leaving validateParameters()");
    }

    public void validateSortCriteria(String sortCriteria) {
        log.info("Entering validateSortCriteria()");
        if (Objects.nonNull(sortCriteria) && !sortCriteria.isBlank()) {
            String[] sortSplit = sortCriteria.split(",", 2);
            if (sortSplit.length != 2) {
                throw new DynamoException(
                        "Invalid sort criteria '%s'. Should be something like 'name,ASC' or 'name,asc'".formatted(
                                sortCriteria),
                        HttpStatus.BAD_REQUEST);
            }

            String sortBy = sortSplit[0].trim();
            String sortOrder = sortSplit[1].trim().toLowerCase();

            if (!sortOrder.equalsIgnoreCase("asc") && !sortOrder.equalsIgnoreCase("desc")) {
                throw new DynamoException("Invalid sort-order [%s] for sort-by [%s]".formatted(sortOrder, sortBy),
                        HttpStatus.BAD_REQUEST);
            }

        }

        log.info("Leaving validateSortCriteria()");

    }

    /**
     * Builds a BooleanBuilder predicate for searching or filtering forms based on
     * the specified parameters.
     * @param  searchOrFilterParameters A MultiValueMap containing search or filter
     *                                  parameters.
     * @return                          A BooleanBuilder containing the constructed
     *                                  predicate.
     * @throws DynamoException          If an error occurs during predicate
     *                                  construction.
     */
    private BooleanBuilder buildSearchOrFilterPredicate(MultiValueMap<String, String> searchOrFilterParameters) {
        log.info("Entering buildSearchOrFilterPredicate()");
        BooleanBuilder booleanBuilder = new BooleanBuilder();
        QFormInvitation formInvitation = QFormInvitation.formInvitation;

        String responseId = searchOrFilterParameters.getFirst("form-invitation-id");

        if (Objects.nonNull(responseId) && !responseId.isBlank()) {
            log.info("Adding filter by formInvitation id predicate for value '{}'", responseId);
            booleanBuilder.and(formInvitation.id.eq(Long.valueOf(responseId)));
        }

        String email = searchOrFilterParameters.getFirst("email");

        if (Objects.nonNull(email) && !email.isBlank()) {
            log.info("Adding filter by formInvitation email predicate for value '{}'", responseId);
            booleanBuilder.and(formInvitation.email.equalsIgnoreCase(email));
        }

        String formInvitationDate = searchOrFilterParameters.getFirst("form-invitation-date");
        if (Objects.nonNull(formInvitationDate) && !formInvitationDate.isBlank()) {
            log.info("Adding filter by formInvitation date predicate for value '{}'", formInvitationDate);
            try {
                Instant instant = Instant.parse(formInvitationDate).truncatedTo(ChronoUnit.DAYS);
                booleanBuilder.and(formInvitation.createdOn.goe(instant)
                        .and(formInvitation.createdOn.lt(instant.plus(1, ChronoUnit.DAYS))));
            } catch (DateTimeException e) {
                log.error("Error processing formInvitation date: {}", formInvitationDate);
                throw new DynamoException(e.getMessage(), HttpStatus.BAD_REQUEST);
            }

        }

        log.info("Leaving buildSearchOrFilterPredicate()");
        return booleanBuilder;
    }

    /**
     * Deletes a form invitation by its ID.
     * @param formInvitationId The ID of the form invitation to delete.
     */
    public void deleteFormInvitation(long formInvitationId) {
        log.info("Entering deleteFormInvitation(), formInvitationId: {}", formInvitationId);
        if (formInvitationId < 0) {
            throw new IllegalArgumentException(
                    "Invalid Form Invitation ID: %s. Form Invitation IDs must be greater than or equal to 1.".formatted(
                    formInvitationId));

        }

        formInvitationSvcService.delete(formInvitationId);
        log.info("Leaving deleteFormInvitation()");
    }

    /**
     * Retrieves a list of email addresses for users eligible to receive invitations, based on their roles.
     * Users with roles "user" or "admin" are included in the list.
     *
     * @return A list of email addresses of eligible users.
     */
    public List<String> retrieveUsersEmailForInvitation() {
        log.info("Entering retrieveUsersEmailForInvitation()");
        List<User> users = userService.retrieveUsersByRole(List.of("user", "admin"));
        List<String> emailList = users.stream().map(User::getEmail).collect(Collectors.toList());
        log.info("Leaving retrieveUsersEmailForInvitation()");
        return emailList;
    }
}
