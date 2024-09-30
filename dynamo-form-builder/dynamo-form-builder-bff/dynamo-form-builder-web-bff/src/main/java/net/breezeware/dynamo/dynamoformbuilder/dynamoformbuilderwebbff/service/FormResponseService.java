package net.breezeware.dynamo.dynamoformbuilder.dynamoformbuilderwebbff.service;

import java.time.DateTimeException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;

import com.querydsl.core.BooleanBuilder;

import net.breezeware.dynamo.dynamoformbuilder.dynamoformbuilderwebbff.dto.FormResponseDto;
import net.breezeware.dynamo.dynamoformbuilder.dynamoformbuilderwebbff.mapper.MapperService;
import net.breezeware.dynamo.dynamoformbuilder.dynamoformsvc.entity.Form;
import net.breezeware.dynamo.dynamoformbuilder.dynamoformsvc.entity.FormInvitation;
import net.breezeware.dynamo.dynamoformbuilder.dynamoformsvc.entity.FormResponse;
import net.breezeware.dynamo.dynamoformbuilder.dynamoformsvc.entity.QFormResponse;
import net.breezeware.dynamo.dynamoformbuilder.dynamoformsvc.enumeration.FormAccessType;
import net.breezeware.dynamo.dynamoformbuilder.dynamoformsvc.enumeration.FormInvitationStatus;
import net.breezeware.dynamo.dynamoformbuilder.dynamoformsvc.enumeration.FormStatus;
import net.breezeware.dynamo.dynamoformbuilder.dynamoformsvc.service.FormInvitationSvcService;
import net.breezeware.dynamo.dynamoformbuilder.dynamoformsvc.service.FormResponseSvcService;
import net.breezeware.dynamo.dynamoformbuilder.dynamoformsvc.service.FormSvcService;
import net.breezeware.dynamo.utils.exception.DynamoException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class FormResponseService {

    private static final Set<String> DEFAULT_VALID_PARAMETERS = Set.of("page-no", "page-size", "sort", "search");
    private final FormResponseSvcService formResponseSvcService;
    private final FormSvcService formSvcService;
    private final FormInvitationSvcService formInvitationSvcService;
    private final MapperService mapperService;
    private final DynamicFormTableManager dynamicFormTableManager;

    @Value("${form.submission.table.schema}")
    private String schemaName;

    /**
     * Saves a form response.
     * @param  formResponseDto The DTO containing the form response data.
     * @return                 The saved form response entity.
     * @throws DynamoException If the form is archived, still in draft, or if an
     *                         error occurs during processing.
     */
    public FormResponse saveFormResponse(FormResponseDto formResponseDto) {
        log.info("Entering saveFormResponse()");

        Form form = formSvcService.retrieveForm(formResponseDto.getFormUniqueId())
                .orElseThrow(() -> new DynamoException(
                        String.format("Form with unique id %s is not found", formResponseDto.getFormUniqueId()),
                        HttpStatus.NOT_FOUND));

        // Check if the form is archived or still in draft
        if (form.getStatus().equalsIgnoreCase(FormStatus.ARCHIVED.getStatus())) {
            throw new DynamoException("Cannot submit the form as it is archived", HttpStatus.BAD_REQUEST);
        } else if (form.getVersion() == null) {
            throw new DynamoException("Cannot submit the form as it is still in draft", HttpStatus.BAD_REQUEST);
        }

        // Create the form response from the DTO
        FormResponse formResponse = mapperService.formResponseDtoToFormResponse(formResponseDto);
        formResponse.setForm(form);
        if (form.getAccessType().equalsIgnoreCase(FormAccessType.PRIVATE.getValue())
                && !form.getOwner().equalsIgnoreCase(formResponseDto.getEmail())) {
            FormInvitation formInvitation =
                    formInvitationSvcService.retrieveFormInvitation(formResponse.getEmail(), form.getId())
                            .orElseThrow(() -> new DynamoException(
                                    String.format("Form invitation email %s is not found", formResponseDto.getEmail()),
                                    HttpStatus.NOT_FOUND));
            formInvitation.setStatus(FormInvitationStatus.SUBMITTED.getValue());
            formInvitationSvcService.update(formInvitation);
        }

        // Create the form response and log the saved form response ID
        FormResponse savedFormResponse = formResponseSvcService.create(formResponse);
        String tableName = generateTableName(form.getName(), form.getUniqueId());
        dynamicFormTableManager.insertDataIntoTable(savedFormResponse.getResponseJson(), schemaName, tableName,
                form.getId(), form.getVersion());
        log.info("Leaving saveFormResponse() savedFormResponse #Id {}", savedFormResponse.getId());
        return savedFormResponse;
    }

    /**
     * Retrieves form responses based on the specified form ID, search/filter
     * parameters, and pagination.
     * @param  formId                   The ID of the form for which responses are
     *                                  to be retrieved.
     * @param  searchOrFilterParameters A MultiValueMap containing search or filter
     *                                  parameters.
     * @param  pageable                 The pagination information.
     * @return                          A Page object containing a list of form
     *                                  responses, paginated according to the
     *                                  provided Pageable.
     */
    public Page<FormResponse> retrieveFormResponses(long formId, MultiValueMap<String, String> searchOrFilterParameters,
            Pageable pageable) {
        log.info("Entering retrieveFormResponses()");
        if (formId < 1000) {
            throw new IllegalArgumentException(
                    "Invalid Form ID: %s. Form IDs must be greater than or equal to 1000.".formatted(formId));
        }

        // Validate query parameters
        Set<String> validParameters = new HashSet<>(DEFAULT_VALID_PARAMETERS);
        validParameters.add("response-date");
        validParameters.add("response-id");
        validateParameters(validParameters, searchOrFilterParameters.keySet());

        // Validate sort criteria
        validateSortCriteria(searchOrFilterParameters.getFirst("sort"));

        // Build a predicate for search or filter criteria
        BooleanBuilder booleanBuilder = buildSearchOrFilterPredicate(searchOrFilterParameters);

        booleanBuilder.and(QFormResponse.formResponse.form.id.eq(formId));

        // Retrieve a paginated list of Form Response entities with the predicate
        Page<FormResponse> formResponses =
                formResponseSvcService.retrievePageEntitiesWithPredicate(booleanBuilder, pageable);

        log.info("Leaving retrieveFormResponses()");
        return formResponses;
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

    /**
     * Validates the sort criteria provided as a string.
     * @param  sortCriteria    The sort criteria string to validate.
     * @throws DynamoException If the sort criteria is invalid.
     */
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
        QFormResponse formResponse = QFormResponse.formResponse;

        String responseId = searchOrFilterParameters.getFirst("response-id");

        if (Objects.nonNull(responseId) && !responseId.isBlank()) {
            log.info("Adding filter by formResponse id predicate for value '{}'", responseId);
            booleanBuilder.and(formResponse.id.eq(Long.valueOf(responseId)));
        }

        String responseDate = searchOrFilterParameters.getFirst("response-date");
        if (Objects.nonNull(responseDate) && !responseDate.isBlank()) {
            log.info("Adding filter by formResponse date predicate for value '{}'", responseDate);
            try {
                Instant instant = Instant.parse(responseDate).truncatedTo(ChronoUnit.DAYS);
                booleanBuilder.and(formResponse.createdOn.goe(instant)
                        .and(formResponse.createdOn.lt(instant.plus(1, ChronoUnit.DAYS))));
            } catch (DateTimeException e) {
                log.error("Error processing formResponse date: {}", responseDate);
                throw new DynamoException(e.getMessage(), HttpStatus.BAD_REQUEST);
            }

        }

        log.info("Leaving buildSearchOrFilterPredicate()");
        return booleanBuilder;
    }

    /**
     * Retrieves a form response by its ID.
     * @param  formResponseId  The ID of the form response to retrieve.
     * @return                 The retrieved form response.
     * @throws DynamoException If the form response with the specified ID is not
     *                         found.
     */
    public FormResponse retrieveFormResponse(long formResponseId) {
        log.info("Entering retrieveFormResponse(), formResponseId: {}", formResponseId);
        if (formResponseId < 0) {
            throw new IllegalArgumentException(
                    "Invalid Form Response ID: %s. Form Response IDs must be greater than or equal to 1.".formatted(
                            formResponseId));
        }

        FormResponse formResponse = formResponseSvcService.retrieveById(formResponseId).orElseThrow(
                () -> new DynamoException("Form Response with ID %s not found".formatted(formResponseId),
                        HttpStatus.NOT_FOUND));
        log.info("Leaving retrieveFormResponse()");
        return formResponse;
    }

    /**
     * Deletes a form response by its ID.
     * @param formResponseId The ID of the form response to delete.
     */
    public void deleteFormResponse(long formResponseId) {
        log.info("Entering deleteFormResponse(), formResponseId: {}", formResponseId);
        if (formResponseId < 0) {
            throw new IllegalArgumentException(
                    "Invalid Form Response ID: %s. Form Response IDs must be greater than or equal to 1.".formatted(
                            formResponseId));

        }

        formResponseSvcService.delete(formResponseId);
        log.info("Leaving deleteFormResponse()");
    }

    /**
     * Retrieves form submissions based on the specified form ID, search/filter
     * parameters, and pagination.
     * @param  formId                   The ID of the form for which submissions are
     *                                  to be retrieved.
     * @param  searchOrFilterParameters A MultiValueMap containing search or filter
     *                                  parameters.
     * @param  pageable                 The pagination information.
     * @return                          A Page object containing a list of form
     *                                  submissions as Maps, paginated according to
     *                                  the provided Pageable.
     */
    public Page<Map<String, Object>> retrieveFormSubmission(long formId,
            MultiValueMap<String, String> searchOrFilterParameters, Pageable pageable) {
        log.info("Entering retrieveFormSubmission() pageNo = {} ,pageSize = {}", pageable.getPageNumber(),
                pageable.getPageSize());
        if (formId < 1000) {
            throw new IllegalArgumentException(
                    "Invalid Form ID: %s. Form IDs must be greater than or equal to 1000.".formatted(formId));
        }

        Form form = formSvcService.retrieveById(formId).orElseThrow(
                () -> new DynamoException("Form with ID %s not found".formatted(formId), HttpStatus.NOT_FOUND));

        String tableName = generateTableName(form.getName(), form.getUniqueId());
        boolean isTableCreated = dynamicFormTableManager.isTableCreated(tableName);

        if (!isTableCreated) {
            throw new DynamoException("Form still in draft.", HttpStatus.NOT_FOUND);
        }

        // Validate query parameters
        Set<String> validParameters = new HashSet<>(DEFAULT_VALID_PARAMETERS);
        validParameters.add("response-date");
        validParameters.add("response-id");
        validateParameters(validParameters, searchOrFilterParameters.keySet());

        // Validate sort criteria
        validateSortCriteria(searchOrFilterParameters.getFirst("sort"));
        String sortBy = searchOrFilterParameters.getFirst("sort");
        String sortOrder = null;

        // Split the sortBy parameter into field and order
        if (sortBy != null && !sortBy.isEmpty()) {
            String[] sortSplit = sortBy.split(",");
            sortBy = sortSplit[0].trim();
            if (sortSplit.length > 1) {
                sortOrder = sortSplit[1].trim().toLowerCase();
            }

        }

        Map<String, String> searchTerms = new HashMap<>();
        String search = searchOrFilterParameters.getFirst("search");
        if (search != null && !search.isEmpty()) {
            // Split the search parameter into field and value
            String[] searchParams = search.split(",");
            if (searchParams.length == 2) {
                String searchField = searchParams[0].trim();
                String searchValue = searchParams[1].trim();
                searchTerms.put(searchField, searchValue);
            }

        }

        // Check specific parameters
        if (searchOrFilterParameters.containsKey("response-date")) {
            String responseDate = searchOrFilterParameters.getFirst("response-date");
            if (responseDate != null && !responseDate.isEmpty()) {
                searchTerms.put("submission_date", searchOrFilterParameters.getFirst("response-date"));
            }

        }

        if (searchOrFilterParameters.containsKey("response-id")) {
            String responseId = searchOrFilterParameters.getFirst("response-id");
            if (responseId != null && !responseId.isEmpty()) {
                searchTerms.put("id", searchOrFilterParameters.getFirst("response-id"));
            }

        }

        // Retrieve data from the table
        List<Map<String, Object>> paginatedData = dynamicFormTableManager.retrieveDataFromTable(schemaName, tableName,
                searchTerms, sortBy, sortOrder, pageable.getPageNumber(), pageable.getPageSize());

        int totalElements = dynamicFormTableManager.retrieveTotalElements(schemaName, tableName, searchTerms);

        Page<Map<String, Object>> page = new PageImpl<>(paginatedData, pageable, totalElements);

        log.info("Leaving retrieveFormSubmission()");
        return page;
    }

    /**
     * Generates a table name based on the form name and unique key.
     * @param  formName  The name of the form.
     * @param  uniqueKey A unique key associated with the form.
     * @return           The generated table name.
     */
    private String generateTableName(String formName, String uniqueKey) {
        log.info("Entering generateTableName()");
        String formNameWithoutSpace = formName.replaceAll("\\s+", "_");
        formNameWithoutSpace = formNameWithoutSpace.toLowerCase();
        String tableName = formNameWithoutSpace + "_" + uniqueKey + "_submission";
        log.info("Leaving generateTableName()");
        return tableName;
    }

    /**
     * Retrieves the labels associated with form submissions.
     * @param  formId          The ID of the form.
     * @return                 A set of strings representing the submission labels.
     * @throws DynamoException If the form with the specified ID is not found.
     */
    public Set<String> retrieveFormSubmissionLabels(long formId) {
        log.info("Entering retrieveFormSubmissionLabels()");
        Form form = formSvcService.retrieveById(formId).orElseThrow(
                () -> new DynamoException("Form with ID %s not found".formatted(formId), HttpStatus.NOT_FOUND));
        String tableName = generateTableName(form.getName(), form.getUniqueId());
        boolean isTableCreated = dynamicFormTableManager.isTableCreated(tableName);
        if (!isTableCreated) {
            throw new DynamoException("Form still in draft.", HttpStatus.NOT_FOUND);
        }

        Set<String> existingColumns = dynamicFormTableManager.getExistingColumns(schemaName, tableName);
        log.info("Leaving retrieveFormSubmissionLabels()");
        return existingColumns;
    }

}
