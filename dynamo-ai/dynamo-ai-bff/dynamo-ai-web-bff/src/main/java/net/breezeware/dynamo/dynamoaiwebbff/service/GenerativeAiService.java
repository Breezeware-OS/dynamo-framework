package net.breezeware.dynamo.dynamoaiwebbff.service;

import com.querydsl.core.BooleanBuilder;
import jakarta.transaction.Transactional;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.breezeware.dynamo.aws.s3.exception.DynamoS3Exception;
import net.breezeware.dynamo.aws.s3.service.api.S3Service;
import net.breezeware.dynamo.dynamoaisvc.entity.Model;
import net.breezeware.dynamo.dynamoaisvc.entity.KnowledgeArtifact;
import net.breezeware.dynamo.dynamoaisvc.entity.QKnowledgeArtifact;
import net.breezeware.dynamo.dynamoaisvc.entity.QModel;
import net.breezeware.dynamo.dynamoaisvc.enumeration.KnowledgeArtifactStatus;
import net.breezeware.dynamo.dynamoaisvc.enumeration.ModelStatus;
import net.breezeware.dynamo.dynamoaisvc.service.KnowledgeArtifactService;
import net.breezeware.dynamo.dynamoaisvc.service.VectorStoreService;
import net.breezeware.dynamo.dynamoaiwebbff.dto.ConversationDto;
import net.breezeware.dynamo.dynamoaiwebbff.dto.KnowledgeArtifactViewDto;
import net.breezeware.dynamo.dynamoaiwebbff.dto.ModelCreateDto;
import net.breezeware.dynamo.dynamoaiwebbff.dto.ModelTestRequestDto;
import net.breezeware.dynamo.dynamoaiwebbff.dto.ModelRequestDto;
import net.breezeware.dynamo.dynamoaiwebbff.dto.ModelUpdateDto;
import net.breezeware.dynamo.dynamoaiwebbff.dto.ModelViewDto;
import net.breezeware.dynamo.dynamoaiwebbff.mapper.KnowledgeArtifactMapper;
import net.breezeware.dynamo.dynamoaiwebbff.mapper.ModelMapper;
import net.breezeware.dynamo.utils.exception.DynamoException;
import net.breezeware.dynamo.utils.exception.ValidationExceptionUtils;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.DateTimeException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class GenerativeAiService {

    private final Validator fieldValidator;
    private final net.breezeware.dynamo.dynamoaisvc.service.ModelService modelService;
    private final KnowledgeArtifactService knowledgeArtifactService;
    private final ModelMapper modelMapper;
    private final KnowledgeArtifactMapper knowledgeArtifactMapper;
    private final S3Service s3Service;
    private final VectorStoreService vectorStoreService;
    @Value("${aws.s3-bucket}")
    String bucketName;
    @Value("${document.cdn-url}")
    String documentCdnUrl;

    private static final Set<String> DEFAULT_VALID_PARAMETERS = Set.of("page-no", "page-size", "sort", "search");

    /**
     * Creates a new AI model based on the provided {@link ModelCreateDto}.
     * @param  modelCreateDto Data Transfer Object containing information for
     *                        creating the model.
     * @return                A {@link ModelViewDto} representing the created model.
     */
    @Transactional
    public ModelViewDto createModel(ModelCreateDto modelCreateDto) {
        log.info("Entering createModel()");

        // Field constraint violation validation
        Set<ConstraintViolation<ModelCreateDto>> fieldViolations = fieldValidator.validate(modelCreateDto);

        // Field constraint violation handling
        ValidationExceptionUtils.handleException(fieldViolations);

        Model model = Model.builder().uniqueId(UUID.randomUUID()).name(modelCreateDto.getModelName())
                .status(ModelStatus.TRAINING.getValue()).build();

        Model createdModel = modelService.create(model);

        ModelViewDto modelViewDto = modelMapper.modelToModelDto(createdModel);
        log.info("Leaving createModel()");

        return modelViewDto;
    }

    /**
     * Retrieves a paginated list of AI models based on the provided pagination and
     * search parameters.
     * @param  pageable         Pagination information.
     * @param  searchParameters Parameters to filter the search results.
     * @return                  A paginated list of {@link ModelViewDto}.
     */
    public Page<ModelViewDto> retrieveModels(Pageable pageable, MultiValueMap<String, String> searchParameters) {
        log.info("Entering retrieveModels()");

        // Set of valid parameters for validation
        Set<String> validParameters = new HashSet<>(DEFAULT_VALID_PARAMETERS);
        validParameters.add("model-name");
        validParameters.add("model-status");
        validParameters.add("model-created-date");

        // Validate the provided parameters
        validateParameters(validParameters, searchParameters.keySet());

        // Build a BooleanBuilder predicate based on the provided search parameters
        BooleanBuilder booleanBuilder = buildSearchOrFilterPredicateForModel(searchParameters);

        Page<Model> pageOfDocuments = modelService.retrievePageEntitiesWithPredicate(booleanBuilder, pageable);

        List<ModelViewDto> modelViewDtos =
                pageOfDocuments.getContent().stream().map(modelMapper::modelToModelDto).toList();

        Page<ModelViewDto> pageOfModelViewDtos =
                new PageImpl<>(modelViewDtos, pageable, pageOfDocuments.getTotalElements());

        log.info("Leaving retrieveModels()");

        return pageOfModelViewDtos;
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
     * Builds a BooleanBuilder predicate based on the provided search or filter
     * parameters.
     * @param  searchOrFilterParameters Parameters for searching or filtering the AI
     *                                  models.
     * @return                          A BooleanBuilder predicate for the search or
     *                                  filter conditions.
     * @throws DynamoException          If an error occurs while parsing the
     *                                  parameters.
     */
    private BooleanBuilder buildSearchOrFilterPredicateForModel(MultiValueMap<String, String> searchOrFilterParameters)
            throws DynamoException {
        log.info("Entering buildSearchOrFilterPredicate()");

        QModel model = QModel.model;
        BooleanBuilder booleanBuilder = new BooleanBuilder();

        // filter by model name
        String filterByModelName = searchOrFilterParameters.getFirst("model-name");
        if (Objects.nonNull(filterByModelName) && !filterByModelName.isBlank()) {
            log.info("Adding filter by model name predicate for value '{}'", filterByModelName);
            booleanBuilder.and(model.name.containsIgnoreCase(filterByModelName));
        }

        // Filter by model status
        String filterByModelStatus = searchOrFilterParameters.getFirst("model-status");
        if (Objects.nonNull(filterByModelStatus) && !filterByModelStatus.isBlank()) {
            log.info("Adding filter by model status predicate for value '{}'", filterByModelStatus);
            booleanBuilder.and(model.status.containsIgnoreCase(filterByModelStatus));
        }

        String modelCreatedDate = searchOrFilterParameters.getFirst("model-created-date");
        if (Objects.nonNull(modelCreatedDate) && !modelCreatedDate.isBlank()) {

            try {
                Instant createdDate = Instant.parse(modelCreatedDate).truncatedTo(ChronoUnit.DAYS);
                log.info("Adding filter by model created date predicate for value '{}'", filterByModelStatus);
                booleanBuilder.and(model.createdOn.goe(createdDate))
                        .and(model.createdOn.lt(createdDate.plus(1, ChronoUnit.DAYS)));
            } catch (DateTimeException e) {
                log.error("Error processing model created date: {}", modelCreatedDate);
                throw new DynamoException(e.getMessage(), HttpStatus.BAD_REQUEST);
            }

        }

        log.info("Leaving buildSearchOrFilterPredicate()");
        return booleanBuilder;
    }

    /**
     * Uploads knowledge artifacts related to a specific AI model to an S3 bucket.
     * @param  modelUniqueId      the unique ID of the AI model
     * @param  knowledgeArtifacts the list of knowledge artifacts to be uploaded
     * @throws DynamoException    if the AI model is not found or there is an error
     *                            during upload
     */
    public void uploadKnowledgeArtifact(UUID modelUniqueId, List<MultipartFile> knowledgeArtifacts) {
        log.info("Entering uploadKnowledgeArtifact()");
        Model model = retrieveModelOrThrow(modelUniqueId);
        knowledgeArtifacts.forEach(multipartFile -> {
            try {
                String documentKey =
                        "%s/%s/%s".formatted("knowledge Artifact", modelUniqueId, multipartFile.getOriginalFilename());
                s3Service.uploadObject(bucketName, documentKey, multipartFile.getBytes());

                KnowledgeArtifact knowledgeArtifact = KnowledgeArtifact.builder().uniqueId(UUID.randomUUID())
                        .name(multipartFile.getOriginalFilename()).type(multipartFile.getContentType())
                        .size(multipartFile.getSize()).key(documentKey).model(model)
                        .status(KnowledgeArtifactStatus.UPLOADED.getValue()).build();

                knowledgeArtifactService.create(knowledgeArtifact);

            } catch (DynamoS3Exception | IOException e) {
                throw new DynamoException(
                        "Error while uploading document '%s'".formatted(multipartFile.getOriginalFilename()),
                        HttpStatus.INTERNAL_SERVER_ERROR);
            }

        });
        log.info("Leaving uploadKnowledgeArtifact()");
    }

    /**
     * Retrieves knowledge artifacts for a specific AI model.
     * @param  pageable         the pagination information.
     * @param  modelUniqueId    the unique identifier of the AI model.
     * @param  searchParameters the search parameters for filtering knowledge
     *                          artifacts.
     * @return                  a page of {@link KnowledgeArtifactViewDto} objects.
     * @throws DynamoException  if the AI model is not found or if there are invalid
     *                          search parameters.
     */
    public Page<KnowledgeArtifactViewDto> retrieveKnowledgeArtifacts(Pageable pageable, UUID modelUniqueId,
            MultiValueMap<String, String> searchParameters) {
        log.info("Entering retrieveKnowledgeArtifacts()");

        modelService.retrieveModelByUniqueId(modelUniqueId)
                .orElseThrow(() -> new DynamoException("AI Model not found with unique ID: %s".formatted(modelUniqueId),
                        HttpStatus.NOT_FOUND));

        // Set of valid parameters for validation
        Set<String> validParameters = new HashSet<>(DEFAULT_VALID_PARAMETERS);
        validParameters.add("knowledge-artifact-name");
        validParameters.add("knowledge-artifact-status");
        validParameters.add("knowledge-artifact-created-date");

        // Validate the provided parameters
        validateParameters(validParameters, searchParameters.keySet());

        // Build a BooleanBuilder predicate based on the provided search parameters
        BooleanBuilder booleanBuilder =
                buildSearchOrFilterPredicateForKnowledgeArtifact(modelUniqueId, searchParameters);

        Page<KnowledgeArtifact> pageOfDocuments =
                knowledgeArtifactService.retrievePageEntitiesWithPredicate(booleanBuilder, pageable);

        List<KnowledgeArtifactViewDto> knowledgeArtifactViewDtos = pageOfDocuments.getContent().stream()
                .map(knowledgeArtifactMapper::knowledgeArtifactToKnowledgeArtifactViewDto).toList();

        Page<KnowledgeArtifactViewDto> pageOfKnowledgeArtifactViewDtos =
                new PageImpl<>(knowledgeArtifactViewDtos, pageable, pageOfDocuments.getTotalElements());

        log.info("Leaving retrieveKnowledgeArtifacts()");

        return pageOfKnowledgeArtifactViewDtos;
    }

    /**
     * Builds a search or filter predicate for knowledge artifacts based on provided
     * parameters.
     * @param  modelUniqueId            the unique identifier of the AI model.
     * @param  searchOrFilterParameters the search or filter parameters.
     * @return                          a {@link BooleanBuilder} object representing
     *                                  the predicate.
     * @throws DynamoException          if there is an error processing the filter
     *                                  parameters.
     */
    private BooleanBuilder buildSearchOrFilterPredicateForKnowledgeArtifact(UUID modelUniqueId,
            MultiValueMap<String, String> searchOrFilterParameters) throws DynamoException {
        log.info("Entering buildSearchOrFilterPredicateForKnowledgeArtifact()");

        QKnowledgeArtifact knowledgeArtifact = QKnowledgeArtifact.knowledgeArtifact;
        BooleanBuilder booleanBuilder = new BooleanBuilder();
        booleanBuilder.and(knowledgeArtifact.model.uniqueId.eq(modelUniqueId));

        // filter by document name
        String filterByDocumentName = searchOrFilterParameters.getFirst("knowledge-artifact-name");
        if (Objects.nonNull(filterByDocumentName) && !filterByDocumentName.isBlank()) {
            log.info("Adding filter by document name predicate for value '{}'", filterByDocumentName);
            booleanBuilder.and(knowledgeArtifact.name.containsIgnoreCase(filterByDocumentName));
        }

        // Filter by document status
        String filterByDocumentStatus = searchOrFilterParameters.getFirst("knowledge-artifact-status");
        if (Objects.nonNull(filterByDocumentStatus) && !filterByDocumentStatus.isBlank()) {
            log.info("Adding filter by document status predicate for value '{}'", filterByDocumentStatus);
            booleanBuilder.and(knowledgeArtifact.status.containsIgnoreCase(filterByDocumentStatus));
        }

        String knowledgeArtifactCreatedDate = searchOrFilterParameters.getFirst("knowledge-artifact-created-date");
        if (Objects.nonNull(knowledgeArtifactCreatedDate) && !knowledgeArtifactCreatedDate.isBlank()) {

            try {
                Instant createdDate = Instant.parse(knowledgeArtifactCreatedDate).truncatedTo(ChronoUnit.DAYS);
                log.info("Adding filter by knowledge artifact created date predicate for value '{}'",
                        filterByDocumentStatus);
                booleanBuilder.and(knowledgeArtifact.createdOn.goe(createdDate))
                        .and(knowledgeArtifact.createdOn.lt(createdDate.plus(1, ChronoUnit.DAYS)));
            } catch (DateTimeException e) {
                log.error("Error processing knowledge artifact created date: {}", knowledgeArtifactCreatedDate);
                throw new DynamoException(e.getMessage(), HttpStatus.BAD_REQUEST);
            }

        }

        log.info("Leaving buildSearchOrFilterPredicateForKnowledgeArtifact()");
        return booleanBuilder;
    }

    /**
     * Deletes a knowledge artifact identified by its unique ID and associated model
     * ID.
     * @param modelUniqueId             the unique ID of the model to which the
     *                                  knowledge artifact belongs
     * @param knowledgeArtifactUniqueId the unique ID of the knowledge artifact to
     *                                  be deleted
     */
    @Transactional
    public void deleteKnowledgeArtifact(UUID modelUniqueId, UUID knowledgeArtifactUniqueId) {
        log.info("Entering deleteKnowledgeArtifact()");

        try {
            KnowledgeArtifact knowledgeArtifact = knowledgeArtifactService
                    .retrieveKnowledgeArtifactByUniqueIdAndModelUniqueId(knowledgeArtifactUniqueId, modelUniqueId)
                    .orElseThrow(() -> new DynamoException(
                            "Knowledge artifact not found with unique ID: %s".formatted(knowledgeArtifactUniqueId),
                            HttpStatus.NOT_FOUND));

            s3Service.deleteObject(bucketName, knowledgeArtifact.getKey());

            if (knowledgeArtifact.getStatus().equalsIgnoreCase(KnowledgeArtifactStatus.EMBEDDED.getValue())) {
                vectorStoreService.deleteDocumentsByModelAndArtifact(modelUniqueId, knowledgeArtifactUniqueId);
            }

            knowledgeArtifactService.deleteKnowledgeArtifactByUniqueIdAndModelUniqueId(knowledgeArtifactUniqueId,
                    modelUniqueId);
        } catch (DynamoS3Exception e) {
            throw new RuntimeException(e);
        }

        log.info("Leaving deleteKnowledgeArtifact()");
    }

    /**
     * Embeds knowledge artifacts for a specific AI model. Retrieves the knowledge
     * artifacts, processes them based on their type (PDF or other), and updates
     * their status.
     * @param modelUniqueId Unique ID of the AI model
     */
    public void embedKnowledgeArtifactsForModel(UUID modelUniqueId) {
        log.info("Entering embedKnowledgeArtifactsForModel()");

        // Retrieve the knowledge artifacts associated with the AI model that are in
        // UPLOADED status
        List<KnowledgeArtifact> knowledgeArtifacts = knowledgeArtifactService
                .findByModelUniqueIdAndStatus(modelUniqueId, KnowledgeArtifactStatus.UPLOADED.getValue());

        // Retrieve the AI model using its unique ID, throw exception if not found
        Model model = retrieveModelOrThrow(modelUniqueId);

        // Process each knowledge artifact
        knowledgeArtifacts.forEach(knowledgeArtifact -> {
            knowledgeArtifact.setStatus(KnowledgeArtifactStatus.EMBEDDING.getValue());
            knowledgeArtifactService.update(knowledgeArtifact);
            vectorStoreService.addResource(documentCdnUrl + "/" + knowledgeArtifact.getKey(), knowledgeArtifact, model);

            // Update the status of the knowledge artifact to EMBEDDED
            knowledgeArtifact.setStatus(KnowledgeArtifactStatus.EMBEDDED.getValue());
            knowledgeArtifactService.update(knowledgeArtifact);

        });
        // Update the status of the AI model to VALIDATING
        if (model.getStatus().equalsIgnoreCase(ModelStatus.TRAINING.getValue())) {
            model.setStatus(ModelStatus.VALIDATING.getValue());
            modelService.update(model);
        }

        log.info("Leaving embedKnowledgeArtifactsForModel()");
    }

    /**
     * Tests the chatbot by creating a chat response based on the given model unique
     * ID and request DTO.
     * @param  modelUniqueId       The unique ID of the model.
     * @param  modelTestRequestDto The model request DTO containing the message,
     *                             system prompt, temperature, and top P values.
     * @return                     The chatbot's response output content.
     */
    @Transactional
    public ConversationDto testConversation(UUID modelUniqueId, ModelTestRequestDto modelTestRequestDto) {
        log.info("Entering testConversation()");
        // Field constraint violation validation
        Set<ConstraintViolation<ModelTestRequestDto>> fieldViolations = fieldValidator.validate(modelTestRequestDto);

        // Field constraint violation handling
        ValidationExceptionUtils.handleException(fieldViolations);
        // Create a chat response using the VectorStoreService
        ChatResponse chatResponse = vectorStoreService.createChatResponse(modelUniqueId,
                modelTestRequestDto.getMessage(), modelTestRequestDto.getSystemPrompt(),
                modelTestRequestDto.getTemperature(), modelTestRequestDto.getTopP());
        String generatedMessage = chatResponse.getResults().stream().map(generation -> {
            return generation.getOutput().getContent();
        }).collect(Collectors.joining("/n"));

        ConversationDto conversationDto = new ConversationDto();
        conversationDto.setMessage(generatedMessage);
        log.info("Leaving testConversation()");
        return conversationDto;
    }

    /**
     * Completes the training of an AI model with the provided parameters.
     * @param modelUniqueId   The unique ID of the AI model.
     * @param modelRequestDto The DTO containing the model parameters.
     */
    @Transactional
    public void completeAiModelTraining(UUID modelUniqueId, ModelRequestDto modelRequestDto) {
        log.info("Entering completeAiModelTraining()");
        // Retrieve the AI model using its unique ID, throw exception if not found
        Model model = retrieveModelOrThrow(modelUniqueId);

        model.setTemperature(modelRequestDto.getTemperature());
        model.setTopP(modelRequestDto.getTopP());
        model.setSystemPrompt(modelRequestDto.getSystemPrompt());
        model.setStatus(ModelStatus.COMPLETED.getValue());
        // Model model1 = modelMapper.modelRequestDtoToModel(modelRequestDto);
        modelService.update(model);
        log.info("Leaving completeAiModelTraining()");
    }

    /**
     * Initiates a conversation with the AI model based on the provided input
     * message and model parameters.
     * @param  modelUniqueId   The unique ID of the AI model.
     * @param  conversationDto The DTO containing the conversation details.
     * @return                 The DTO containing the conversation response.
     */
    @Transactional
    public ConversationDto conversation(UUID modelUniqueId, ConversationDto conversationDto) {
        log.info("Entering conversation()");
        // Field constraint violation validation
        Set<ConstraintViolation<ConversationDto>> fieldViolations = fieldValidator.validate(conversationDto);

        // Field constraint violation handling
        ValidationExceptionUtils.handleException(fieldViolations);

        // Retrieve the AI model using its unique ID, throw exception if not found
        Model model = retrieveModelOrThrow(modelUniqueId);

        // Create a chat response using the VectorStoreService
        ChatResponse chatResponse = vectorStoreService.createChatResponse(modelUniqueId, conversationDto.getMessage(),
                model.getSystemPrompt(), model.getTemperature(), model.getTopP());
        String generatedMessage = chatResponse.getResults().stream().map(generation -> {
            return generation.getOutput().getContent();
        }).collect(Collectors.joining("/n"));

        conversationDto.setMessage(generatedMessage);

        log.info("Leaving conversation()");

        return conversationDto;
    }

    /**
     * Retrieves the details of an AI model based on its unique ID.
     * @param  modelUniqueId The unique ID of the AI model.
     * @return               The DTO containing the model details.
     */
    @Transactional
    public ModelRequestDto retrieveModel(UUID modelUniqueId) {
        log.info("Entering retrieveModel()");

        // Retrieve the AI model using its unique ID, throw exception if not found
        Model model = retrieveModelOrThrow(modelUniqueId);

        ModelRequestDto modelRequestDto = modelMapper.modelToModelRequestDto(model);
        log.info("Leaving retrieveModel()");
        return modelRequestDto;
    }

    /**
     * Retrieves an AI model by its unique ID from the model service, throwing an
     * exception if not found.
     * @param  modelUniqueId   The unique ID of the AI model to retrieve.
     * @return                 The retrieved AI model.
     * @throws DynamoException If the AI model is not found.
     */
    @Transactional
    private Model retrieveModelOrThrow(UUID modelUniqueId) {
        log.info("Entering retrieveModelOrThrow()");
        Model model = modelService.retrieveModelByUniqueId(modelUniqueId)
                .orElseThrow(() -> new DynamoException("AI Model not found with unique ID: %s".formatted(modelUniqueId),
                        HttpStatus.NOT_FOUND));
        log.info("Leaving retrieveModelOrThrow()");
        return model;
    }

    /**
     * Checks if all documents related to a specific AI model are embedded.
     * @param  modelUniqueId The unique ID of the AI model.
     * @return               True if all documents are embedded, false otherwise.
     */
    @Transactional
    public boolean isAllDocumentsEmbedded(UUID modelUniqueId) {
        log.info("Entering isAllDocumentsEmbedded()");
        // Retrieve the AI model using its unique ID, throw exception if not found
        Model model = retrieveModelOrThrow(modelUniqueId);

        List<KnowledgeArtifact> knowledgeArtifacts = knowledgeArtifactService
                .findByModelUniqueIdAndStatus(model.getUniqueId(), KnowledgeArtifactStatus.UPLOADED.getValue());

        boolean isAllDocumentsEmbedded = knowledgeArtifacts.isEmpty();
        log.info("Leaving isAllDocumentsEmbedded()");
        return isAllDocumentsEmbedded;
    }

    /**
     * Updates the name of a model identified by its unique ID.
     * @param modelUniqueId  the unique ID of the model to be updated
     * @param modelUpdateDto the DTO containing the updated model name
     */
    @Transactional
    public void updateModel(UUID modelUniqueId, ModelUpdateDto modelUpdateDto) {
        log.info("Entering updateModelName()");
        // Retrieve the AI model using its unique ID, throw exception if not found
        Model model = retrieveModelOrThrow(modelUniqueId);
        if (!modelUpdateDto.getModelName().isEmpty()) {
            model.setName(modelUpdateDto.getModelName());
            modelService.update(model);
        }

        log.info("Leaving updateModelName()");
    }

    /**
     * Checks if there are any documents available for a specific AI model.
     * @param  modelUniqueId the unique identifier of the AI model
     * @return               true if documents are available, false otherwise.
     */
    @Transactional
    public boolean isDocumentsAvailable(UUID modelUniqueId) {
        log.info("Entering isDocumentsAvailable()");
        // Retrieve the AI model using its unique ID, throw exception if not found
        Model model = retrieveModelOrThrow(modelUniqueId);

        List<KnowledgeArtifact> knowledgeArtifacts = knowledgeArtifactService.findByModelUniqueId(model.getUniqueId());

        boolean isDocumentsAvailable = !knowledgeArtifacts.isEmpty();
        log.info("Leaving isDocumentsAvailable()");
        return isDocumentsAvailable;
    }

    /**
     * Retrieves the status of knowledge artifacts associated with a given model
     * unique identifier.
     * @param  modelUniqueId The unique identifier of the model.
     * @return               The status of knowledge artifacts: "EMBEDDING" if at
     *                       least one artifact is in embedding status, "UPLOADED"
     *                       if at least one artifact is uploaded but none are in
     *                       embedding status, or "EMBEDDED" if no artifacts are in
     *                       embedding or uploaded status.
     */
    @Transactional
    public String retrieveModelKnowledgeArtifactStatus(UUID modelUniqueId) {
        log.info("Entering retrieveModelKnowledgeArtifactStatus()");
        List<KnowledgeArtifact> knowledgeArtifacts = knowledgeArtifactService.findByModelUniqueId(modelUniqueId);
        int uploadedKnowledgeArtifacts = knowledgeArtifacts.stream().filter(knowledgeArtifact -> knowledgeArtifact
                .getStatus().equalsIgnoreCase(KnowledgeArtifactStatus.UPLOADED.getValue())).toList().size();
        int embeddingKnowledgeArtifacts = knowledgeArtifacts.stream().filter(knowledgeArtifact -> knowledgeArtifact
                .getStatus().equalsIgnoreCase(KnowledgeArtifactStatus.EMBEDDING.getValue())).toList().size();

        log.info("Exiting retrieveModelKnowledgeArtifactStatus()");
        return embeddingKnowledgeArtifacts >= 1 ? KnowledgeArtifactStatus.EMBEDDING.getValue()
                : uploadedKnowledgeArtifacts >= 1 ? KnowledgeArtifactStatus.UPLOADED.getValue()
                : KnowledgeArtifactStatus.EMBEDDED.getValue();
    }

}
