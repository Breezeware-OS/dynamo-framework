package net.breezeware.dynamo.dynamoformbuilder.dynamoformbuilderwebbff.controller;

import java.util.Map;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.SortDefault;
import org.springframework.http.MediaType;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import net.breezeware.dynamo.dynamoformbuilder.dynamoformbuilderwebbff.dto.FormResponseDto;
import net.breezeware.dynamo.dynamoformbuilder.dynamoformbuilderwebbff.service.FormResponseService;
import net.breezeware.dynamo.dynamoformbuilder.dynamoformsvc.entity.FormResponse;
import net.breezeware.dynamo.utils.exception.DynamoException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.enums.ParameterStyle;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Form Response")
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping(value = "/api/form-responses")
public class FormResponseController {

    private final FormResponseService formResponseService;

    @Operation(summary = "Save FormResponse", description = "Save a new form response based on the provided request.")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = FormResponseDto.class)))
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Success",
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(example = """
                        {
                            "id": 1000,
                            "createdOn": "2023-09-19T09:45:13.761348Z",
                            "modifiedOn": "2023-09-19T09:46:32.937385248Z",
                            "responseJson": {
                                "Name": "Akash kumar"
                            }
                        }
                        """))),
        @ApiResponse(responseCode = "400", description = "Bad Request",
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(example = """
                        {
                            "statusCode": 400,
                            "message": "BAD_REQUEST",
                            "details": [
                                "Form with unique id 10036884 is not found"
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
                        }"""))), })
    @PostMapping()
    public FormResponse saveFormResponse(@RequestBody FormResponseDto formResponseDto) throws DynamoException {
        log.info("Entering saveFormResponse(), formResponseDto: {}", formResponseDto);
        FormResponse formResponse = formResponseService.saveFormResponse(formResponseDto);
        log.info("Leaving saveFormResponse(), formResponse #ID: {}", formResponse.getId());
        return formResponse;
    }

    @Operation(summary = "Retrieve Form Responses",
            description = "Retrieves a paginated list of form responses based on specified criteria.")
    @Parameters(value = {
        @Parameter(allowEmptyValue = true, name = "page-no", example = "0", description = "Page number",
                in = ParameterIn.QUERY),
        @Parameter(allowEmptyValue = true, name = "page-size", example = "8", description = "Page size",
                in = ParameterIn.QUERY),
        @Parameter(allowEmptyValue = true, name = "sort", example = "name,ASC",
                description = "Sort by field with sort order", in = ParameterIn.QUERY),
        @Parameter(allowEmptyValue = true, name = "response-id", example = "1000", description = "Response id search",
                in = ParameterIn.QUERY),
        @Parameter(allowEmptyValue = true, name = "response-date", example = "19/09/2023",
                description = "response date search", in = ParameterIn.QUERY) })
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Success",
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(example = """
                        {
                            "content": [
                                {
                                    "id": 1,
                                    "createdOn": "2024-03-20T10:30:49.272117Z",
                                    "modifiedOn": null,
                                    "responseJson": {
                                    "name":"John",
                                    "age":30,
                                    "car":null
                                    },
                                    "form": {
                                        "id": 1000,
                                        "createdOn": "2024-03-20T10:30:28.084399Z",
                                        "modifiedOn": null,
                                        "name": "fgg",
                                        "description": "demo",
                                        "version": "1.1.f0",
                                        "status": "published",
                                        "uniqueId": "2wererW"
                                    }
                                }
                            ],
                            "pageable": {
                                "sort": {
                                    "empty": false,
                                    "sorted": true,
                                    "unsorted": false
                                },
                                "offset": 0,
                                "pageNumber": 0,
                                "pageSize": 12,
                                "paged": true,
                                "unpaged": false
                            },
                            "last": true,
                            "totalElements": 1,
                            "totalPages": 1,
                            "size": 12,
                            "number": 0,
                            "sort": {
                                "empty": false,
                                "sorted": true,
                                "unsorted": false
                            },
                            "first": true,
                            "numberOfElements": 1,
                            "empty": false
                        }
                        """))),
        @ApiResponse(responseCode = "400", description = "Bad request",
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(example = """
                        {
                            "statusCode": 400,
                            "message": "BAD_REQUEST",
                            "details": [
                                "Unknown parameter(s) [name] found"
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
                        }"""))) })
    @GetMapping("/{form-id}")
    public Page<FormResponse> retrieveFormResponses(
            @Parameter(hidden = true, in = ParameterIn.QUERY, style = ParameterStyle.FORM)
            @SortDefault(sort = "createdOn", direction = Sort.Direction.ASC) Pageable pageable,
            @RequestParam(required = false) MultiValueMap<String, String> searchParameters,
            @PathVariable("form-id") long formId) throws DynamoException {
        log.info("Entering retrieveFormResponses(), pageable: {}, searchParameters: {}", pageable, searchParameters);
        Page<FormResponse> formResponses =
                formResponseService.retrieveFormResponses(formId, searchParameters, pageable);
        log.info("Leaving retrieveFormResponses(), # of elements: {} in page-no: {}",
                formResponses.getNumberOfElements(), formResponses.getPageable().getPageNumber());
        return formResponses;
    }

    @Operation(summary = "Retrieve a form response", description = "Retrieve an existing form response by its ID")
    @Parameter(name = "form-response-id", example = "1000", description = "Unique ID of the form response to retrieve",
            in = ParameterIn.QUERY)
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Success",
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(example = """
                          { "id": 1000,
                            "createdOn": "2023-09-19T09:45:13.761348Z",
                            "modifiedOn": "2023-09-19T09:46:32.937385248Z",
                            "responseJson": {
                                "Name": "Akash kumar"
                            }
                        }
                        """))),
        @ApiResponse(responseCode = "404", description = "Not found",
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(example = """
                        {
                            "statusCode": 404,
                            "message": "NOT_FOUND",
                            "details": [
                                "Form response with ID '10' not found"
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
                        }"""))) })
    @GetMapping()
    public FormResponse retrieveFormResponse(@RequestParam("form-response-id") long formResponseId) {
        log.info("Entering retrieveFormResponse(), formResponseId: {}", formResponseId);
        FormResponse formResponse = formResponseService.retrieveFormResponse(formResponseId);
        log.info("Leaving retrieveFormResponse()");
        return formResponse;
    }

    @Operation(summary = "Deletes a form response", description = "Deletes a form response for given valid identifier")
    @Parameter(name = "form-response-id", example = "1", description = "Unique ID of the existing form response",
            in = ParameterIn.PATH)
    @ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Success"),
        @ApiResponse(responseCode = "404", description = "Not found",
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(example = """
                        {
                            "statusCode": 404,
                            "message": "NOT_FOUND",
                            "details": [
                                "Invalid Form Response ID: -1. Form Response IDs must be greater than or equal to 1."
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
                        }"""))) })
    @DeleteMapping("/{form-response-id}")
    public void deletesFormResponse(@PathVariable("form-response-id") long formResponseId) {
        log.info("Entering deletesFormResponse(), formResponseId: {}", formResponseId);
        formResponseService.deleteFormResponse(formResponseId);
        log.info("Leaving deletesFormResponse()");
    }

    @Operation(summary = "Retrieves  a form submissions",
            description = "Retrieves  a form submissions for given valid identifier")
    @Parameter(name = "form-id", example = "1", description = "Unique ID of the existing form", in = ParameterIn.PATH)
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Success",
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(example = """
                        [
                          {
                            "id": 10,
                            "Fullname": "Kishore Chinnaswamy",
                            "Text": "Sample Text",
                            "Text_1": "Sample Text",
                            "Date": "2024-04-11T18:30:00.000+00:00",
                            "Text  Area": "text area",
                            "Number": 98,
                            "Phone Number": "8521479630",
                            "Email": "kishore@breezeware.net",
                            "Address": "
                            {\\"addressline1\\":\\"18/9\\",\\"addressline2\\":\\"pudur\\",:\\"12345\\"}",
                            "Select": "Value 2",
                            "Radio": "Value 2",
                            "Checkbox": "true",
                            "Checkbox Group": "",
                            "Multi Select": "",
                            "form_id": 1
                          },
                          {
                            "id": 9,
                            "Fullname": "Kishore Chinnaswamy",
                            "Text": "Sample Text",
                            "Text_1": "Sample Text",
                            "Date": "2024-04-11T18:30:00.000+00:00",
                            "Text  Area": "text area",
                            "Number": 98,
                            "Phone Number": "8521479630",
                            "Email": "kishore@breezeware.net",
                            "Address": "
                            {\\"addressline1\\":\\"18/9\\",\\"addressline2\\":\\"pudur\\",:\\"12345\\"}"",
                            "Select": "Value 2",
                            "Radio": "Value 2",
                            "Checkbox": "true",
                            "Checkbox Group": "",
                            "Multi Select": "",
                            "form_id": 1
                          }]
                        """))),
        @ApiResponse(responseCode = "404", description = "Not found",
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(example = """
                        {
                            "statusCode": 404,
                            "message": "NOT_FOUND",
                            "details": [
                                "Invalid Form Response ID: -1. Form Response IDs must be greater than or equal to 1."
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
                        }"""))) })
    @GetMapping("/forms/{formId}/submissions")
    public Page<Map<String, Object>> retrieveFormSubmissions(@PathVariable long formId,
            @Parameter(hidden = true, in = ParameterIn.QUERY, style = ParameterStyle.FORM)
            @SortDefault(sort = "id", direction = Sort.Direction.ASC)
            @RequestParam MultiValueMap<String, String> searchOrFilterParameters, Pageable pageable) {
        log.info("Entering retrieveFormSubmissions()");
        Page<Map<String, Object>> retrievedFormSubmission =
                formResponseService.retrieveFormSubmission(formId, searchOrFilterParameters, pageable);
        log.info("Leaving retrieveFormSubmissions()");
        return retrievedFormSubmission;
    }

    @Operation(summary = "Retrieves  a form labels",
            description = "Retrieves  a form labels for given valid identifier")
    @Parameter(name = "form-id", example = "1", description = "Unique ID of the existing form", in = ParameterIn.PATH)
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Success",
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(example = """
                        [
                            "form_version",
                            "submission_date",
                            "Text Field",
                            "form_id",
                            "id"
                        ]
                        """))),
        @ApiResponse(responseCode = "404", description = "Not found",
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(example = """
                        {
                            "statusCode": 404,
                            "message": "NOT_FOUND",
                            "details": [
                                "Invalid Form Response ID: -1. Form Response IDs must be greater than or equal to 1."
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
                        }"""))) })
    @GetMapping("/forms/{form-id}/submission-labels")
    public Set<String> retrieveFormSubmissionLabels(@PathVariable("form-id") long formId) {
        log.info("Entering retrieveFormSubmissionLabels()");
        Set<String> columnHeaders = formResponseService.retrieveFormSubmissionLabels(formId);
        log.info("Entering retrieveFormSubmissionLabels()");

        return columnHeaders;
    }
}
