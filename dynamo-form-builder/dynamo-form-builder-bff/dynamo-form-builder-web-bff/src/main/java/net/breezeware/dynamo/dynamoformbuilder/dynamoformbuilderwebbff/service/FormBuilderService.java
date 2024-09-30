package net.breezeware.dynamo.dynamoformbuilder.dynamoformbuilderwebbff.service;

import java.time.DateTimeException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;

import com.fasterxml.jackson.databind.JsonNode;
import com.querydsl.core.BooleanBuilder;

import net.breezeware.dynamo.dynamoformbuilder.dynamoformbuilderwebbff.dto.FormDto;
import net.breezeware.dynamo.dynamoformbuilder.dynamoformbuilderwebbff.dto.FormVersionDto;
import net.breezeware.dynamo.dynamoformbuilder.dynamoformbuilderwebbff.mapper.MapperService;
import net.breezeware.dynamo.dynamoformbuilder.dynamoformsvc.entity.Form;
import net.breezeware.dynamo.dynamoformbuilder.dynamoformsvc.entity.FormVersion;
import net.breezeware.dynamo.dynamoformbuilder.dynamoformsvc.entity.QForm;
import net.breezeware.dynamo.dynamoformbuilder.dynamoformsvc.entity.QFormVersion;
import net.breezeware.dynamo.dynamoformbuilder.dynamoformsvc.enumeration.FormAccessType;
import net.breezeware.dynamo.dynamoformbuilder.dynamoformsvc.enumeration.FormStatus;
import net.breezeware.dynamo.dynamoformbuilder.dynamoformsvc.service.FormInvitationSvcService;
import net.breezeware.dynamo.dynamoformbuilder.dynamoformsvc.service.FormSvcService;
import net.breezeware.dynamo.dynamoformbuilder.dynamoformsvc.service.FormVersionService;
import net.breezeware.dynamo.usermanagement.entity.User;
import net.breezeware.dynamo.usermanagement.service.UserService;
import net.breezeware.dynamo.utils.exception.DynamoException;
import net.breezeware.dynamo.utils.exception.ValidationExceptionUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class FormBuilderService {

    private static final Set<String> DEFAULT_VALID_PARAMETERS = Set.of("page-no", "page-size", "sort", "search");
    private final FormSvcService formSvcService;
    private final FormVersionService formVersionService;
    private final Validator fieldValidator;
    private final MapperService mapperService;
    private final UserService userService;
    private final FormInvitationSvcService formInvitationSvcService;
    private final DynamicFormTableManager dynamicFormTableManager;

    @Value("${form.submission.table.schema}")
    private String schemaName;

    /**
     * Publishes a form based on the provided FormPublishRequest.
     * @param  formDto the request containing form details to be published.
     * @return         the published Form entity.
     */
    public Form publishForm(FormDto formDto) {
        log.info("Entering publishForm(), formDto: {}", formDto);
        if (formDto.getAccessType() == null || formDto.getAccessType().isEmpty()) {
            throw new DynamoException("Form access type must be null or empty.", HttpStatus.BAD_REQUEST);
        }

        Form form = processForm(formDto, FormStatus.PUBLISHED);
        String tableName = generateTableName(form.getName(), form.getUniqueId());

        FormVersion latestFormVersion = formVersionService.retrieveFormVersions(form).stream()
                .filter(formVer -> formVer.getStatus().equalsIgnoreCase(FormStatus.PUBLISHED.getStatus()))
                .max(Comparator.comparing(FormVersion::getModifiedOn))
                .orElseThrow(() -> new DynamoException("Form content not found.", HttpStatus.NOT_FOUND));
        // create or update the form response submission table
        JsonNode formJson = formDto.getFormJson() == null ? latestFormVersion.getFormJson() : formDto.getFormJson();
        dynamicFormTableManager.createOrUpdateFormSubmissionTable(formJson, schemaName, tableName);
        log.info("Leaving publishForm()");
        return form;
    }

    private String generateTableName(String formName, String uniqueKey) {
        log.info("Entering generateTableName() formName= {} ,uniqueKey={} ", formName, uniqueKey);
        String formNameWithoutSpace = formName.replaceAll("\\s+", "_");
        formNameWithoutSpace = formNameWithoutSpace.toLowerCase();
        String tableName = formNameWithoutSpace + "_" + uniqueKey + "_submission";
        log.info("Leaving generateTableName() tableName= {}", tableName);
        return tableName;
    }

    /**
     * Drafts a form based on the provided FormPublishRequest.
     * @param  formDto the request containing form details to be drafted.
     * @return         the drafted Form entity.
     */
    public Form draftForm(FormDto formDto) {
        log.info("Entering draftForm(), formDto: {}", formDto);
        return processForm(formDto, FormStatus.DRAFT);
    }

    /**
     * Processes a form based on the provided {@link FormDto} and target
     * {@link FormStatus}. This method handles field constraint validation, creates
     * or updates the form accordingly, and returns the processed {@link Form}
     * entity.
     * @param  formDto      The request containing form details.
     * @param  targetStatus The target status for the form.
     * @return              The processed {@link Form} entity.
     */
    Form processForm(FormDto formDto, FormStatus targetStatus) {
        log.info("Entering processForm(), formDto: {}", formDto);

        // Field constraint violation validation
        Set<ConstraintViolation<FormDto>> fieldViolations = fieldValidator.validate(formDto);

        // Field constraint violation handling
        ValidationExceptionUtils.handleException(fieldViolations);

        Form processedForm = new Form();

        if (formDto.getId() != null) {
            Optional<Form> retrievedForm = formSvcService.retrieveById(formDto.getId());
            if (retrievedForm.isPresent()) {
                Form form = mapperService.formDtoToForm(formDto, targetStatus.getStatus());
                if ((form.getOwner() == null || form.getOwner().isEmpty()) && formDto.getOwnerEmail() != null) {
                    form.setOwner(formDto.getOwnerEmail());
                }

                processedForm = formSvcService.update(form);
                buildAndSaveFormVersion(formDto, processedForm);
            }

        } else {
            Form form = mapperService.formDtoToForm(formDto, targetStatus.getStatus());
            if ((form.getOwner() == null || form.getOwner().isEmpty()) && formDto.getOwnerEmail() != null) {
                form.setOwner(formDto.getOwnerEmail());
            }

            processedForm = formSvcService.create(form);
            buildAndSaveFormVersion(formDto, processedForm);
        }

        log.info("Leaving processForm(),savedForm: {}", processedForm);
        return processedForm;
    }

    private void buildAndSaveFormVersion(FormDto formDto, Form form) {
        log.info("Entering buildAndSaveFormVersion()");
        FormVersion formVersion = mapperService.formDtoToFromVersion(formDto);
        formVersion.setStatus(form.getStatus());
        formVersion.setForm(form);
        Optional<FormVersion> formVersions = formVersionService.retrieveFormVersion(form, FormStatus.DRAFT.getStatus());
        if (formVersions.isPresent()) {
            formVersion.setId(formVersions.get().getId());
            formVersionService.update(formVersion);
        } else if (form.getStatus().equalsIgnoreCase(FormStatus.PUBLISHED.getStatus())
                && formDto.getFormJson() == null) {
            Optional<FormVersion> latestFormVersion = formVersionService.retrieveFormVersions(form).stream()
                    .filter(formVer -> formVer.getStatus().equalsIgnoreCase(FormStatus.PUBLISHED.getStatus()))
                    .max(Comparator.comparing(FormVersion::getModifiedOn));
            if (latestFormVersion.isPresent()) {
                latestFormVersion.get().setVersion(form.getVersion());
                formVersionService.update(latestFormVersion.get());
            }

        } else {
            formVersionService.create(formVersion);
        }

        log.info("Leaving buildAndSaveFormVersion()");
    }

    /**
     * Retrieves a paginated list of Form entities based on the provided search or
     * filter parameters and pageable settings. This method validates the query
     * parameters and sort criteria, builds a predicate for filtering and searching,
     * and returns the resulting page of Form entities.
     * @param  searchOrFilterParameters A MultiValueMap containing search or filter
     *                                  parameters.
     * @param  pageable                 A Pageable object specifying the pagination
     *                                  settings.
     * @return                          A Page of Form entities matching the search
     *                                  or filter criteria.
     * @throws DynamoException          if invalid query parameters are provided.
     * @throws DynamoException          if an invalid sort criterion is provided.
     */
    public Page<Form> retrieveForms(MultiValueMap<String, String> searchOrFilterParameters, Pageable pageable) {
        log.info("Entering retrieveForms()");

        // Validate query parameters
        Set<String> validParameters = new HashSet<>(DEFAULT_VALID_PARAMETERS);
        validParameters.add("form-name");
        validParameters.add("form-date");
        validParameters.add("status");
        validateParameters(validParameters, searchOrFilterParameters.keySet());

        // Validate sort criteria
        validateSortCriteria(searchOrFilterParameters.getFirst("sort"));

        // Build a predicate for search or filter criteria
        BooleanBuilder booleanBuilder = buildSearchOrFilterPredicateForForms(searchOrFilterParameters);

        // Retrieve a paginated list of Form entities with the predicate
        Page<Form> formPage = formSvcService.retrievePageEntitiesWithPredicate(booleanBuilder, pageable);

        log.info("Leaving retrieveForms()");
        return formPage;
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
     * Validates the sort criteria.
     * @param  sortCriteria    the sort criteria to be validated.
     * @throws DynamoException if there is an error in the sort criteria.
     */
    void validateSortCriteria(String sortCriteria) {
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
    private BooleanBuilder
            buildSearchOrFilterPredicateForForms(MultiValueMap<String, String> searchOrFilterParameters) {
        log.info("Entering buildSearchOrFilterPredicateForForms()");
        BooleanBuilder booleanBuilder = new BooleanBuilder();
        QForm form = QForm.form;

        String status = searchOrFilterParameters.getFirst("status");
        if (Objects.isNull(status) || status.isBlank()) {
            throw new DynamoException("status must not be null or empty", HttpStatus.BAD_REQUEST);
        }

        log.info("Adding filter by form status predicate for value '{}'", status);
        FormStatus formStatus = FormStatus.retrieveFormStatus(status)
                .orElseThrow(() -> new DynamoException("Invalid status '%s' for form".formatted(status),
                        HttpStatus.BAD_REQUEST));
        switch (formStatus) {
            case ARCHIVED:
                booleanBuilder.and(form.status.equalsIgnoreCase(FormStatus.ARCHIVED.getStatus()));
                break;
            case PUBLISHED:
                booleanBuilder.and(form.status.equalsIgnoreCase(FormStatus.PUBLISHED.getStatus()));
                break;
            case DRAFT:
                booleanBuilder.and(form.status.equalsIgnoreCase(FormStatus.DRAFT.getStatus()));
                break;
            case ALL:
                // no builder needed
                break;
            default:
                throw new DynamoException("Invalid status '%s' for form".formatted(status),
                        HttpStatus.BAD_REQUEST);
        }

        String formName = searchOrFilterParameters.getFirst("form-name");
        if (Objects.nonNull(formName) && !formName.isBlank()) {
            log.info("Adding filter by form name predicate for value '{}'", formName);
            booleanBuilder.and(form.name.containsIgnoreCase(formName));
        }

        String formDate = searchOrFilterParameters.getFirst("form-date");
        if (Objects.nonNull(formDate) && !formDate.isBlank()) {
            log.info("Adding filter by form date predicate for value '{}'", formDate);
            try {
                Instant instant = Instant.parse(formDate).truncatedTo(ChronoUnit.DAYS);
                booleanBuilder.and(
                        QForm.form.modifiedOn.goe(instant).and(form.modifiedOn.lt(instant.plus(1, ChronoUnit.DAYS))));
            } catch (DateTimeException e) {
                log.error("Error processing form date: {}", formDate);
                throw new DynamoException(e.getMessage(), HttpStatus.BAD_REQUEST);
            }

        }

        log.info("Leaving buildSearchOrFilterPredicateForForms()");
        return booleanBuilder;
    }

    /**
     * Archives a form by changing its status to ARCHIVE.
     * @param formId The ID of the form to be archived.
     */
    public void archiveForm(long formId) {
        log.info("Entering archiveForm(), formId: {}", formId);
        if (formId < 1000) {
            throw new DynamoException("Form with ID %s not found".formatted(formId), HttpStatus.NOT_FOUND);
        }

        Optional<Form> form = formSvcService.retrieveById(formId);
        form.ifPresent(archiveForm -> {
            archiveForm.setStatus(FormStatus.ARCHIVED.getStatus());
            formSvcService.update(archiveForm);
            List<FormVersion> formVersions = formVersionService.retrieveFormVersions(archiveForm);
            formVersions.forEach(formVersion -> {
                formVersion.setStatus(FormStatus.ARCHIVED.getStatus());
                formVersionService.update(formVersion);
            });
        });

        log.info("Leaving archiveCustomer(), archived form with id: {}", formId);
    }

    /**
     * Retrieves a paginated list of form versions based on search or filter
     * criteria.
     * @param  formId                   The ID of the form to retrieve versions for.
     * @param  searchOrFilterParameters A MultiValueMap containing search or filter
     *                                  parameters.
     * @param  pageable                 The pageable information for pagination.
     * @return                          A Page containing the retrieved form
     *                                  versions.
     */
    public Page<FormVersionDto> retrieveFormVersions(long formId,
            MultiValueMap<String, String> searchOrFilterParameters, Pageable pageable) {
        log.info("Entering retrieveFormVersions()");
        if (formId < 1000) {
            throw new IllegalArgumentException(
                    "Invalid Form ID: %s. Form IDs must be greater than or equal to 1000.".formatted(formId));
        }

        // Validate query parameters
        Set<String> validParameters = new HashSet<>(DEFAULT_VALID_PARAMETERS);
        validParameters.add("version");
        validateParameters(validParameters, searchOrFilterParameters.keySet());

        // Validate sort criteria
        validateSortCriteria(searchOrFilterParameters.getFirst("sort"));

        // Build a predicate for search or filter criteria
        BooleanBuilder booleanBuilder =
                buildSearchOrFilterPredicateForFormVersions(searchOrFilterParameters.getFirst("version"));

        booleanBuilder.and(QFormVersion.formVersion.form.id.eq(formId));

        // Retrieve a paginated list of Form entities with the predicate
        Page<FormVersion> formVersionPage =
                formVersionService.retrievePageEntitiesWithPredicate(booleanBuilder, pageable);

        // convert Page of Site to Page of SiteDto
        List<FormVersionDto> formVersionDtoList =
                formVersionPage.getContent().stream().map(mapperService::formVersionToFormVersionDto).toList();
        PageImpl<FormVersionDto> formVersionDtoPage =
                new PageImpl<>(formVersionDtoList, pageable, formVersionPage.getTotalElements());
        log.info("Leaving retrieveFormVersions()");
        return formVersionDtoPage;
    }

    /**
     * Builds a BooleanBuilder predicate for searching or filtering form versions
     * based on the specified parameters.
     * @param  formVersionToBeFiltered The form version to be filtered.
     * @return                         A BooleanBuilder containing the constructed
     *                                 predicate.
     */
    private BooleanBuilder buildSearchOrFilterPredicateForFormVersions(String formVersionToBeFiltered) {
        log.info("Entering buildSearchOrFilterPredicateForFormVersions()");
        BooleanBuilder booleanBuilder = new BooleanBuilder();
        QFormVersion formVersion = QFormVersion.formVersion;

        if (Objects.nonNull(formVersionToBeFiltered) && !formVersionToBeFiltered.isBlank()) {
            log.info("Adding filter by form version predicate for value '{}'", formVersionToBeFiltered);
            booleanBuilder.and(formVersion.version.containsIgnoreCase(formVersionToBeFiltered));
        }

        log.info("Leaving buildSearchOrFilterPredicateForFormVersions()");
        return booleanBuilder;
    }

    /**
     * Retrieves a form by its ID.
     * @param  formId          The ID of the form to retrieve.
     * @return                 The retrieved form.
     * @throws DynamoException If the form with the specified ID is not found.
     */
    public FormDto retrieveForm(long formId) {
        log.info("Entering retrieveForm(), formId: {}", formId);
        if (formId < 1000) {
            throw new IllegalArgumentException(
                    "Invalid Form ID: %s. Form IDs must be greater than or equal to 1000.".formatted(formId));
        }

        Form form = formSvcService.retrieveById(formId).orElseThrow(
                () -> new DynamoException("Form with ID %s not found".formatted(formId), HttpStatus.NOT_FOUND));

        Optional<FormVersion> latestFormVersion = formVersionService.retrieveFormVersions(form).stream()
                .max(Comparator.comparing(FormVersion::getModifiedOn));

        FormDto formDto = mapperService.formToFormDto(form);
        latestFormVersion.ifPresent(formVersion -> {
            formDto.setFormJson(formVersion.getFormJson());
            formDto.setVersion(formVersion.getVersion());
        });

        log.info("Leaving retrieveForm()");
        return formDto;
    }

    public Map<String, Long> retrieveFormStatusCount() {
        log.info("Entering retrieveFormStatusCount()");
        QForm form = QForm.form;

        // Execute queries to get status counts
        BooleanBuilder publishedCount = new BooleanBuilder();
        publishedCount.and(form.status.equalsIgnoreCase(FormStatus.PUBLISHED.name()));
        long inProgressCount = formSvcService.retrieveEntitiesCount(publishedCount);

        BooleanBuilder draftCountBb = new BooleanBuilder();
        draftCountBb.and(form.status.equalsIgnoreCase(FormStatus.DRAFT.name()));
        long draftCount = formSvcService.retrieveEntitiesCount(draftCountBb);

        BooleanBuilder archivedCountBb = new BooleanBuilder();
        archivedCountBb.and(form.status.equalsIgnoreCase(FormStatus.ARCHIVED.name()));
        long archivedCount = formSvcService.retrieveEntitiesCount(archivedCountBb);

        long allStatusCount = inProgressCount + draftCount + archivedCount;

        // Create and populate the status counts map
        Map<String, Long> statusCounts = new HashMap<>();
        statusCounts.put(FormStatus.PUBLISHED.name().toLowerCase(), inProgressCount);
        statusCounts.put(FormStatus.DRAFT.name().toLowerCase(), draftCount);
        statusCounts.put(FormStatus.ARCHIVED.name().toLowerCase(), archivedCount);
        statusCounts.put(FormStatus.ALL.name().toLowerCase(), allStatusCount);

        log.info("Leaving retrieveFormStatusCount()");
        return statusCounts;
    }

    /**
     * Retrieves a form by its unique ID.
     * @param  uniqueId        The unique ID of the form to retrieve.
     * @return                 The retrieved form.
     * @throws DynamoException If the form with the specified ID is not found.
     */
    public FormDto retrieveForm(String uniqueId) {
        log.info("Entering retrieveForm(), uniqueId: {}", uniqueId);
        if (uniqueId == null || uniqueId.isEmpty()) {
            throw new IllegalArgumentException("Invalid Form Unique ID: %s.".formatted(uniqueId));
        }

        Form form = formSvcService.retrieveForm(uniqueId)
                .orElseThrow(() -> new DynamoException("Form with unique ID %s not found".formatted(uniqueId),
                        HttpStatus.NOT_FOUND));

        if (form.getAccessType().equalsIgnoreCase(FormAccessType.PRIVATE.getValue())) {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                throw new DynamoException("User not authenticated.", HttpStatus.UNAUTHORIZED);
            }

            String userId = authentication.getPrincipal().toString();
            log.info("userId = {}", userId);
            if ("anonymousUser".equalsIgnoreCase(userId)) {
                throw new DynamoException("User not authenticated.", HttpStatus.UNAUTHORIZED);
            }

            User user = userService.retrieveUserByIdmUserId(userId)
                    .orElseThrow(() -> new DynamoException("User not found in the system.", HttpStatus.NOT_FOUND));

            if (!user.getEmail().equalsIgnoreCase(form.getOwner())) {
                formInvitationSvcService.retrieveFormInvitation(user.getEmail(), form.getId()).orElseThrow(
                        () -> new DynamoException("You do not have access to the form.", HttpStatus.FORBIDDEN));
            }

        }

        Optional<FormVersion> latestFormVersion = formVersionService.retrieveFormVersions(form).stream()
                .filter(formVer -> formVer.getStatus().equalsIgnoreCase(FormStatus.PUBLISHED.getStatus()))
                .max(Comparator.comparing(FormVersion::getModifiedOn));

        FormDto formDto = mapperService.formToFormDto(form);
        latestFormVersion.ifPresent(formVersion -> {
            formDto.setFormJson(formVersion.getFormJson());
            formDto.setVersion(formVersion.getVersion());
        });

        log.info("Leaving retrieveForm()");
        return formDto;
    }
}
