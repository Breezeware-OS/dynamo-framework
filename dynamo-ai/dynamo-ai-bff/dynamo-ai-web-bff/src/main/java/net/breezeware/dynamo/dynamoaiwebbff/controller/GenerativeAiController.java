package net.breezeware.dynamo.dynamoaiwebbff.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.enums.ParameterStyle;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.breezeware.dynamo.dynamoaiwebbff.dto.ConversationDto;
import net.breezeware.dynamo.dynamoaiwebbff.dto.KnowledgeArtifactViewDto;
import net.breezeware.dynamo.dynamoaiwebbff.dto.ModelCreateDto;
import net.breezeware.dynamo.dynamoaiwebbff.dto.ModelRequestDto;
import net.breezeware.dynamo.dynamoaiwebbff.dto.ModelTestRequestDto;
import net.breezeware.dynamo.dynamoaiwebbff.dto.ModelUpdateDto;
import net.breezeware.dynamo.dynamoaiwebbff.dto.ModelViewDto;
import net.breezeware.dynamo.dynamoaiwebbff.service.GenerativeAiService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@Tag(name = "Generative AI")
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping(value = "/api/model")
public class GenerativeAiController {

    private final GenerativeAiService generativeAiService;

    @PostMapping
    @Operation(summary = "Create a Model", description = "Creates a new model.")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            content = @Content(mediaType = "application/json", schema = @Schema(example = """
                    {
                        "model-name": "ALM Model"
                    }
                    """)))
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Created - Model created successfully",
                content = @Content(mediaType = "application/json", schema = @Schema(example = """
                        {
                            "name": "ALM Model",
                            "status": "training",
                            "uniqueId": "803a0737-2337-4335-9ae9-73ae4944013f",
                            "createdOn": "2024-05-24T06:32:26.403337961Z"
                        }
                        """))),
        @ApiResponse(responseCode = "400", description = "Bad Request - Invalid Model details",
                content = @Content(mediaType = "application/json", schema = @Schema(example = """
                        {
                            "statusCode": 400,
                            "message": "BAD_REQUEST",
                            "details": [
                                "Model name is missing or blank."
                            ]
                        }
                        """))),
        @ApiResponse(responseCode = "401", description = "Unauthorized request",
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(example = """
                        {
                            "statusCode": 401,
                            "message": "UNAUTHORIZED",
                            "details": [
                                "Full authentication is required to access this resource"
                            ]
                        }
                        """))) })
    public ModelViewDto createModel(@RequestBody ModelCreateDto modelCreateDto) {
        log.info("Entering createModel()");
        ModelViewDto modelViewDto = generativeAiService.createModel(modelCreateDto);
        log.info("Leaving createModel()");
        return modelViewDto;
    }

    @GetMapping
    @Operation(summary = "Retrieve Models", description = "Retrieve Models record.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Retrieve Page of Models with default pagination and sorting",
                content = @Content(mediaType = "application/json", schema = @Schema(example = """
                        {
                             "content": [
                                 {
                                     "name": "RPC Model",
                                     "status": "training",
                                     "uniqueId": "803a0737-2337-4335-9ae9-73ae4944013f",
                                     "createdOn": "2024-05-24T06:32:26.403338Z"
                                 },
                                 {
                                     "name": "ALM Model",
                                     "status": "completed",
                                     "uniqueId": "65074721-2978-4f34-bd7f-ede11ff8a438",
                                     "createdOn": "2024-05-22T12:31:38.625106Z"
                                 }
                             ],
                             "pageable": {
                                 "pageNumber": 0,
                                 "pageSize": 10,
                                 "sort": {
                                     "sorted": true,
                                     "empty": false,
                                     "unsorted": false
                                 },
                                 "offset": 0,
                                 "paged": true,
                                 "unpaged": false
                             },
                             "last": true,
                             "totalElements": 2,
                             "totalPages": 1,
                             "size": 10,
                             "number": 0,
                             "sort": {
                                 "sorted": true,
                                 "empty": false,
                                 "unsorted": false
                             },
                             "first": true,
                             "numberOfElements": 2,
                             "empty": false
                         }
                        """))),
        @ApiResponse(responseCode = "400", description = "Bad Request - Invalid sort name",
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(example = """
                        {
                            "statusCode": 400,
                            "message": "BAD_REQUEST",
                            "details": [
                                "Invalid sort 'name,ascending'. Allowed sort-orders are asc,ASC,desc,DESC"
                            ]
                        }
                        """))),
        @ApiResponse(responseCode = "401", description = "Unauthorized request",
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(example = """
                        {
                            "statusCode": 401,
                            "message": "UNAUTHORIZED",
                            "details": [
                                "Full authentication is required to access this resource"
                            ]
                        }
                        """))) })
    public Page<ModelViewDto> retrieveModels(
            @Parameter(hidden = true, in = ParameterIn.QUERY, style = ParameterStyle.FORM)
            @PageableDefault(sort = "modifiedOn", direction = Sort.Direction.DESC) Pageable pageable,
            @RequestParam(required = false) MultiValueMap<String, String> searchParameters) {
        log.info("Entering retrieveModels()");
        Page<ModelViewDto> modelViewDtoPage = generativeAiService.retrieveModels(pageable, searchParameters);
        log.info("Leaving retrieveModels()");
        return modelViewDtoPage;
    }

    @PostMapping("/{model-id}/upload-knowledge-artifacts")
    @Operation(summary = "Upload Knowledge Artifacts",
            description = "Uploads knowledge artifacts for a specific AI model.")
    @Parameter(description = "Model unique identifier", example = "c38b2827-d3d4-4fc1-b508-90b7f96c58c9",
            required = true, in = ParameterIn.PATH)
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            content = @Content(mediaType = "multipart/form-data", schema = @Schema(example = """
                    {
                        "knowledge-artifacts": [
                            {
                                "name": "document1.pdf",
                                "type": "application/pdf",
                                "size": 1048576
                            },
                            {
                                "name": "document2.md",
                                "type": "text/markdown",
                                "size": 2097152
                            }
                        ]
                    }
                    """)))
    @ApiResponses(
            value = { @ApiResponse(responseCode = "200", description = "Knowledge artifacts uploaded successfully"),
                @ApiResponse(responseCode = "404", description = "Not Found - AI Model not found",
                        content = @Content(mediaType = "application/json", schema = @Schema(example = """
                                {
                                    "statusCode": 404,
                                    "message": "NOT_FOUND",
                                    "details": [
                                        "AI Model not found with unique ID: b3d95904-4883-4117-9adc-68577341f5c2"
                                    ]
                                }
                                """))),
                @ApiResponse(responseCode = "401", description = "Unauthorized request",
                        content = @Content(mediaType = "application/json", schema = @Schema(example = """
                                {
                                    "statusCode": 401,
                                    "message": "UNAUTHORIZED",
                                    "details": [
                                        "Full authentication is required to access this resource"
                                    ]
                                }
                                """))),
                @ApiResponse(responseCode = "500",
                        description = "Internal Server Error - Error while uploading knowledge artifacts",
                        content = @Content(mediaType = "application/json", schema = @Schema(example = """
                                {
                                    "statusCode": 500,
                                    "message": "INTERNAL_SERVER_ERROR",
                                    "details": [
                                        "Error while uploading document 'document1.pdf'."
                                    ]
                                }
                                """))) })
    public void uploadKnowledgeArtifacts(@PathVariable("model-id") UUID modelUniqueId,
            @RequestPart(value = "knowledge-artifacts") List<MultipartFile> knowledgeArtifacts) {
        log.info("Entering uploadKnowledgeArtifacts()");
        generativeAiService.uploadKnowledgeArtifact(modelUniqueId, knowledgeArtifacts);
        log.info("Leaving uploadKnowledgeArtifacts()");
    }

    @GetMapping("/{model-id}/knowledge-artifacts")
    @Operation(summary = "Retrieve Knowledge Artifacts",
            description = "Retrieve knowledge artifacts for a specific model.")
    @Parameter(description = "Model unique identifier", example = "c38b2827-d3d4-4fc1-b508-90b7f96c58c9",
            required = true, in = ParameterIn.PATH)
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200",
                description = "Retrieve Page of knowledge artifacts with default pagination and sorting",
                content = @Content(mediaType = "application/json", schema = @Schema(example = """
                        {
                            "content": [
                                {
                                    "uniqueId": "3a148cc5-a8e9-4d76-9a25-ea48544eec0c",
                                    "name": "Create misc cloud resources to support CI_CD pipeline\
                                    development (Infra Engr).md",
                                    "type": "text/markdown",
                                    "size": "4.8 KB",
                                    "status": "uploaded",
                                    "createdOn": "2024-05-21T05:12:32.198813Z"
                                },
                                {
                                    "uniqueId": "28949dc2-6258-4590-92c8-9ea5aecf0a7d",
                                    "name": "Setup AWS cloud deployment infrastructure.md",
                                    "type": "text/markdown",
                                    "size": "2.7 KB",
                                    "status": "uploaded",
                                    "createdOn": "2024-05-21T05:12:31.721559Z"
                                },
                                {
                                    "uniqueId": "cbed273f-f5a9-4b21-b37c-7c390dbb7cb0",
                                    "name": "Setup AWS config rules (Infra Engr).md",
                                    "type": "text/markdown",
                                    "size": "460 B",
                                    "status": "uploaded",
                                    "createdOn": "2024-05-21T05:12:31.169080Z"
                                },
                                {
                                    "uniqueId": "909b7a82-597e-4656-aa76-89a6887b14fc",
                                    "name": "Setup CI_CD pipeline in deployment account using\
                                     CDK (Infra Engr).md",
                                    "type": "text/markdown",
                                    "size": "4.3 KB",
                                    "status": "uploaded",
                                    "createdOn": "2024-05-21T05:12:30.697572Z"
                                },
                                {
                                    "uniqueId": "5257d338-4c94-43ab-aec1-141f142f4b6c",
                                    "name": "Setup CI stage using codebuild for development\
                                    branches using CDK (Infra Engr).md",
                                    "type": "text/markdown",
                                    "size": "3.4 KB",
                                    "status": "uploaded",
                                    "createdOn": "2024-05-21T05:12:30.222722Z"
                                },
                                {
                                    "uniqueId": "b184c2c6-ffba-489e-a4eb-fe2184c07e98",
                                    "name": "Setup functional QA environment in cloud (Infra Engr).md",
                                    "type": "text/markdown",
                                    "size": "1.8 KB",
                                    "status": "uploaded",
                                    "createdOn": "2024-05-21T05:12:29.747871Z"
                                },
                                {
                                    "uniqueId": "6b436945-3ba3-448f-b837-0b1c6c806d90",
                                    "name": "Setup notifications for CI_CD events (Infra Engr).md",
                                    "type": "text/markdown",
                                    "size": "1.8 KB",
                                    "status": "uploaded",
                                    "createdOn": "2024-05-21T05:12:29.250137Z"
                                }
                            ],
                            "pageable": {
                                "pageNumber": 0,
                                "pageSize": 10,
                                "sort": {
                                    "sorted": true,
                                    "empty": false,
                                    "unsorted": false
                                },
                                "offset": 0,
                                "paged": true,
                                "unpaged": false
                            },
                            "totalElements": 7,
                            "totalPages": 1,
                            "last": true,
                            "size": 10,
                            "number": 0,
                            "sort": {
                                "sorted": true,
                                "empty": false,
                                "unsorted": false
                            },
                            "numberOfElements": 7,
                            "first": true,
                            "empty": false
                        }
                        """))),
        @ApiResponse(responseCode = "404", description = "Model not found",
                content = @Content(mediaType = "application/json", schema = @Schema(example = """
                        {
                            "statusCode": 404,
                            "message": "NOT_FOUND",
                            "details": [
                                "AI Model not found with unique ID: b3d95904-4883-4117-9adc-68577341f5c2"
                            ]
                        }
                        """))),
        @ApiResponse(responseCode = "400", description = "Bad Request",
                content = @Content(mediaType = "application/json", schema = @Schema(example = """
                        {
                            "statusCode": 400,
                            "message": "BAD_REQUEST",
                            "details": [
                                "Invalid sort 'name,ascending'. Allowed sort-orders are asc,ASC,desc,DESC"
                            ]
                        }
                        """))),
        @ApiResponse(responseCode = "401", description = "Unauthorized request",
                content = @Content(mediaType = "application/json", schema = @Schema(example = """
                        {
                            "statusCode": 401,
                            "message": "UNAUTHORIZED",
                            "details": [
                                "Full authentication is required to access this resource"
                            ]
                        }
                        """))) })
    public Page<KnowledgeArtifactViewDto> retrieveKnowledgeArtifacts(@PathVariable("model-id") UUID modelUniqueId,
            @Parameter(hidden = true, in = ParameterIn.QUERY, style = ParameterStyle.FORM)
            @PageableDefault(sort = "modifiedOn", direction = Sort.Direction.DESC) Pageable pageable,
            @RequestParam(required = false) MultiValueMap<String, String> searchParameters) {
        log.info("Entering retrieveKnowledgeArtifacts()");
        Page<KnowledgeArtifactViewDto> knowledgeArtifactViewDtos =
                generativeAiService.retrieveKnowledgeArtifacts(pageable, modelUniqueId, searchParameters);
        log.info("Leaving retrieveKnowledgeArtifacts()");
        return knowledgeArtifactViewDtos;
    }

    @DeleteMapping("/{model-id}/knowledge-artifacts/{knowledge-artifact}")
    @Operation(summary = "Delete Knowledge Artifact",
            description = "Delete a specific knowledge artifact for a given model.")
    @Parameter(description = "Model unique identifier", example = "c38b2827-d3d4-4fc1-b508-90b7f96c58c9",
            required = true, in = ParameterIn.PATH)
    @ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Knowledge Artifact deleted successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized request",
                content = @Content(mediaType = "application/json", schema = @Schema(example = """
                        {
                            "statusCode": 401,
                            "message": "UNAUTHORIZED",
                            "details": [
                                "Full authentication is required to access this resource"
                            ]
                        }
                        """))) })
    public void deleteKnowledgeArtifact(@PathVariable("model-id") UUID modelUniqueId,
            @PathVariable("knowledge-artifact") UUID knowledgeArtifactUniqueId) {
        log.info("Entering deleteKnowledgeArtifact()");
        generativeAiService.deleteKnowledgeArtifact(modelUniqueId, knowledgeArtifactUniqueId);
        log.info("Leaving deleteKnowledgeArtifact()");
    }

    @PutMapping("/{model-id}/embed-knowledge-artifacts")
    @Operation(summary = "Embed Knowledge Artifacts",
            description = "Embeds knowledge artifacts for a specific AI model.")
    @Parameter(description = "Model unique identifier", example = "c38b2827-d3d4-4fc1-b508-90b7f96c58c9",
            required = true, in = ParameterIn.PATH)
    @ApiResponses(
            value = { @ApiResponse(responseCode = "200", description = "Knowledge artifacts embedded successfully"),
                @ApiResponse(responseCode = "404", description = "Not Found - AI Model not found",
                        content = @Content(mediaType = "application/json", schema = @Schema(example = """
                                {
                                    "statusCode": 404,
                                    "message": "NOT_FOUND",
                                    "details": [
                                        "AI Model not found with unique ID: b3d95904-4883-4117-9adc-68577341f5c2"
                                    ]
                                }
                                """))),
                @ApiResponse(responseCode = "401", description = "Unauthorized request",
                        content = @Content(mediaType = "application/json", schema = @Schema(example = """
                                {
                                    "statusCode": 401,
                                    "message": "UNAUTHORIZED",
                                    "details": [
                                        "Full authentication is required to access this resource"
                                    ]
                                }
                                """))) })
    public void embedKnowledgeArtifact(@PathVariable("model-id") UUID modelUniqueId) {
        log.info("Entering embedKnowledgeArtifact()");
        generativeAiService.embedKnowledgeArtifactsForModel(modelUniqueId);
        log.info("Leaving embedKnowledgeArtifact()");
    }

    @PostMapping("/{model-id}/test-conversation")
    @Operation(summary = "Test Conversation", description = "Test conversation with a specific AI model.")
    @Parameter(description = "Model unique identifier", example = "c38b2827-d3d4-4fc1-b508-90b7f96c58c9",
            required = true, in = ParameterIn.PATH)
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            content = @Content(mediaType = "application/json", schema = @Schema(example = """
                    {
                        "systemPrompt": "You are a ChatBot.",
                        "temperature": "0.2",
                        "topP": "0.1",
                        "message": "Can You help me?"
                    }
                    """)))
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Conversation tested successfully",
                content = @Content(mediaType = "application/json", schema = @Schema(example = """
                        {
                            "message": "Of course! I am here to help. How can I assist you today?"
                        }
                        """))),
        @ApiResponse(responseCode = "400", description = "Bad Request",
                content = @Content(mediaType = "application/json", schema = @Schema(example = """
                        {
                            "statusCode": 400,
                            "message": "BAD_REQUEST",
                            "details": [
                                "Message is missing or blank."
                            ]
                        }
                        """))),
        @ApiResponse(responseCode = "401", description = "Unauthorized request",
                content = @Content(mediaType = "application/json", schema = @Schema(example = """
                        {
                            "statusCode": 401,
                            "message": "UNAUTHORIZED",
                            "details": [
                                "Full authentication is required to access this resource"
                            ]
                        }
                        """))) })
    public ConversationDto testConversation(@PathVariable("model-id") UUID modelUniqueId,
            @RequestBody ModelTestRequestDto modelTestRequestDto) {
        log.info("Entering testConversation");
        ConversationDto conversationDto = generativeAiService.testConversation(modelUniqueId, modelTestRequestDto);
        log.info("Leaving testConversation");
        return conversationDto;
    }

    @PutMapping("/{model-id}/complete-training")
    @Operation(summary = "Complete AI Model Training", description = "Complete the training for a specific AI model.")
    @Parameter(description = "Model unique identifier", example = "c38b2827-d3d4-4fc1-b508-90b7f96c58c9",
            required = true, in = ParameterIn.PATH)
    @ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Training completed successfully"),
        @ApiResponse(responseCode = "404", description = "Not Found - AI Model not found",
                content = @Content(mediaType = "application/json", schema = @Schema(example = """
                        {
                            "statusCode": 404,
                            "message": "NOT_FOUND",
                            "details": [
                                "AI Model not found with unique ID: b3d95904-4883-4117-9adc-68577341f5c2"
                            ]
                        }
                        """))),
        @ApiResponse(responseCode = "401", description = "Unauthorized request",
                content = @Content(mediaType = "application/json", schema = @Schema(example = """
                        {
                            "statusCode": 401,
                            "message": "UNAUTHORIZED",
                            "details": [
                                "Full authentication is required to access this resource"
                            ]
                        }
                        """))) })
    public void completeAiModelTraining(@PathVariable("model-id") UUID modelUniqueId,
            @RequestBody ModelRequestDto modelRequestDto) {
        log.info("Entering completeAiModelTraining");
        generativeAiService.completeAiModelTraining(modelUniqueId, modelRequestDto);
        log.info("Leaving completeAiModelTraining");
    }

    @PostMapping("/{model-id}/conversation")
    @Operation(summary = "Start Conversation", description = "Start a conversation with a specific AI model.")
    @Parameter(description = "Model unique identifier", example = "c38b2827-d3d4-4fc1-b508-90b7f96c58c9",
            required = true, in = ParameterIn.PATH)
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            content = @Content(mediaType = "application/json", schema = @Schema(example = """
                    {
                        "message": "Can You help me?"
                    }
                    """)))
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Conversation started successfully",
                content = @Content(mediaType = "application/json", schema = @Schema(example = """
                        {
                            "message": "Of course! I am here to help. How can I assist you today?"
                        }
                        """))),
        @ApiResponse(responseCode = "400", description = "Bad Request",
                content = @Content(mediaType = "application/json", schema = @Schema(example = """
                        {
                            "statusCode": 400,
                            "message": "BAD_REQUEST",
                            "details": [
                                "Message is missing or blank."
                            ]
                        }
                        """))),
        @ApiResponse(responseCode = "404", description = "Not Found - AI Model not found",
                content = @Content(mediaType = "application/json", schema = @Schema(example = """
                        {
                            "statusCode": 404,
                            "message": "NOT_FOUND",
                            "details": [
                                "AI Model not found with unique ID: b3d95904-4883-4117-9adc-68577341f5c2"
                            ]
                        }
                        """))),
        @ApiResponse(responseCode = "401", description = "Unauthorized request",
                content = @Content(mediaType = "application/json", schema = @Schema(example = """
                        {
                            "statusCode": 401,
                            "message": "UNAUTHORIZED",
                            "details": [
                                "Full authentication is required to access this resource"
                            ]
                        }
                        """))) })
    public ConversationDto conversation(@PathVariable("model-id") UUID modelUniqueId,
            @RequestBody ConversationDto conversationDto) {
        log.info("Entering conversation");
        ConversationDto conversation = generativeAiService.conversation(modelUniqueId, conversationDto);
        log.info("Leaving conversation");
        return conversation;
    }

    @GetMapping("/{model-id}")
    @Operation(summary = "Retrieve AI Model", description = "Retrieve the details of a specific AI model.")
    @Parameter(description = "Model unique identifier", example = "c38b2827-d3d4-4fc1-b508-90b7f96c58c9",
            required = true, in = ParameterIn.PATH)
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Model retrieved successfully",
                content = @Content(mediaType = "application/json", schema = @Schema(example = """
                        {
                            "systemPrompt": "You are a ALM ChatBot.",
                            "temperature": 0.2,
                            "topP": 0.4
                        }
                        """))),
        @ApiResponse(responseCode = "404", description = "Not Found - AI Model not found",
                content = @Content(mediaType = "application/json", schema = @Schema(example = """
                        {
                            "statusCode": 404,
                            "message": "NOT_FOUND",
                            "details": [
                                "AI Model not found with unique ID: b3d95904-4883-4117-9adc-68577341f5c2"
                            ]
                        }
                        """))),
        @ApiResponse(responseCode = "401", description = "Unauthorized request",
                content = @Content(mediaType = "application/json", schema = @Schema(example = """
                        {
                            "statusCode": 401,
                            "message": "UNAUTHORIZED",
                            "details": [
                                "Full authentication is required to access this resource"
                            ]
                        }
                        """))) })
    public ModelRequestDto retrieveModel(@PathVariable("model-id") UUID modelUniqueId) {
        log.info("Entering retrieveModel");
        ModelRequestDto modelRequestDto = generativeAiService.retrieveModel(modelUniqueId);
        log.info("Leaving retrieveModel");
        return modelRequestDto;
    }

    @GetMapping("/{model-id}/is-all-documents-embedded")
    @Operation(summary = "Check if All Documents are Embedded",
            description = "Check if all documents for a specific AI model are embedded.")
    @Parameter(description = "Model unique identifier", example = "c38b2827-d3d4-4fc1-b508-90b7f96c58c9",
            required = true, in = ParameterIn.PATH)
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Status of document embedding retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Not Found - AI Model not found",
                content = @Content(mediaType = "application/json", schema = @Schema(example = """
                        {
                            "statusCode": 404,
                            "message": "NOT_FOUND",
                            "details": [
                                "AI Model not found with unique ID: b3d95904-4883-4117-9adc-68577341f5c2"
                            ]
                        }
                        """))),
        @ApiResponse(responseCode = "401", description = "Unauthorized request",
                content = @Content(mediaType = "application/json", schema = @Schema(example = """
                        {
                            "statusCode": 401,
                            "message": "UNAUTHORIZED",
                            "details": [
                                "Full authentication is required to access this resource"
                            ]
                        }
                        """))) })
    public boolean isAllDocumentsEmbedded(@PathVariable("model-id") UUID modelUniqueId) {
        log.info("Entering isAllDocumentsEmbedded");
        boolean isAllDocumentsEmbedded = generativeAiService.isAllDocumentsEmbedded(modelUniqueId);
        log.info("Leaving isAllDocumentsEmbedded");
        return isAllDocumentsEmbedded;
    }

    @PatchMapping("/{model-id}")
    @Operation(summary = "Update Model", description = "Update the details of a specific AI model.")
    @Parameter(description = "Model unique identifier", example = "c38b2827-d3d4-4fc1-b508-90b7f96c58c9",
            required = true, in = ParameterIn.PATH)
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            content = @Content(mediaType = "application/json", schema = @Schema(example = """
                    {
                        "modelName": "Updated Model Name"
                    }
                    """)))
    @ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Model updated successfully"),
        @ApiResponse(responseCode = "404", description = "Not Found - AI Model not found",
                content = @Content(mediaType = "application/json", schema = @Schema(example = """
                        {
                            "statusCode": 404,
                            "message": "NOT_FOUND",
                            "details": [
                                "AI Model not found with unique ID: b3d95904-4883-4117-9adc-68577341f5c2"
                            ]
                        }
                        """))),
        @ApiResponse(responseCode = "401", description = "Unauthorized request",
                content = @Content(mediaType = "application/json", schema = @Schema(example = """
                        {
                            "statusCode": 401,
                            "message": "UNAUTHORIZED",
                            "details": [
                                "Full authentication is required to access this resource"
                            ]
                        }
                        """))) })
    public void updateModel(@PathVariable("model-id") UUID modelUniqueId, @RequestBody ModelUpdateDto modelUpdateDto) {
        log.info("Entering updateModel");
        generativeAiService.updateModel(modelUniqueId, modelUpdateDto);
        log.info("Leaving updateModel");
    }

    @GetMapping("/{model-id}/is-documents-available")
    @Operation(summary = "Check Documents Availability",
            description = "Check if there are documents available for a specific AI model.")
    @Parameter(description = "Model unique identifier", example = "c38b2827-d3d4-4fc1-b508-90b7f96c58c9",
            required = true, in = ParameterIn.PATH)
    @ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Check completed successfully"),
        @ApiResponse(responseCode = "404", description = "Not Found - AI Model not found",
                content = @Content(mediaType = "application/json", schema = @Schema(example = """
                        {
                            "statusCode": 404,
                            "message": "NOT_FOUND",
                            "details": [
                                "AI Model not found with unique ID: b3d95904-4883-4117-9adc-68577341f5c2"
                            ]
                        }
                        """))),
        @ApiResponse(responseCode = "401", description = "Unauthorized request",
                content = @Content(mediaType = "application/json", schema = @Schema(example = """
                        {
                            "statusCode": 401,
                            "message": "UNAUTHORIZED",
                            "details": [
                                "Full authentication is required to access this resource"
                            ]
                        }
                        """))) })
    public boolean isDocumentsAvailable(@PathVariable("model-id") UUID modelUniqueId) {
        log.info("Entering isDocumentsAvailable");
        boolean isDocumentsAvailable = generativeAiService.isDocumentsAvailable(modelUniqueId);
        log.info("Leaving isDocumentsAvailable");
        return isDocumentsAvailable;
    }

    @GetMapping("{model-id}/knowledge-artifacts-status")
    @Operation(summary = "Retrieve Model's Knowledge Artifact Status",
            description = "Retrieve the status of knowledge artifacts for a specific AI model.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Success",
                content = @Content(schema = @Schema(example = "embedding"))),
        @ApiResponse(responseCode = "404", description = "Not Found - AI Model not found",
                content = @Content(mediaType = "application/json", schema = @Schema(example = """
                            {
                                "statusCode": 404,
                                "message": "NOT_FOUND",
                                "details": [
                                    "AI Model not found with unique ID: b3d95904-4883-4117-9adc-68577341f5c2"
                                ]
                            }
                        """))),
        @ApiResponse(responseCode = "401", description = "Unauthorized request",
                content = @Content(mediaType = "application/json", schema = @Schema(example = """
                            {
                                "statusCode": 401,
                                "message": "UNAUTHORIZED",
                                "details": [
                                    "Full authentication is required to access this resource"
                                ]
                            }
                        """))) })
    public String retrieveModelKnowledgeArtifactStatus(@PathVariable("model-id") UUID modelUniqueId) {
        log.info("Entering retrieveModelKnowledgeArtifactStatus");
        String modelKnowledgeArtifactStatus = generativeAiService.retrieveModelKnowledgeArtifactStatus(modelUniqueId);
        log.info("Leaving retrieveModelKnowledgeArtifactStatus");
        return modelKnowledgeArtifactStatus;
    }

}