package net.breezeware.dynamo.dynamoformbuilder.dynamoformbuilderwebbff.controller;

import java.util.Map;

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

import net.breezeware.dynamo.dynamoformbuilder.dynamoformbuilderwebbff.dto.FormDto;
import net.breezeware.dynamo.dynamoformbuilder.dynamoformbuilderwebbff.dto.FormVersionDto;
import net.breezeware.dynamo.dynamoformbuilder.dynamoformbuilderwebbff.service.FormBuilderService;
import net.breezeware.dynamo.dynamoformbuilder.dynamoformsvc.entity.Form;
import net.breezeware.dynamo.utils.exception.DynamoException;
import net.breezeware.dynamo.utils.exception.ErrorResponse;

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

@Tag(name = "Form Builder")
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping(value = "/api/forms")
public class FormBuilderController {

    private final FormBuilderService formBuilderService;

    @Operation(summary = "Publish Form", description = "Publishes a form based on the provided request.")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = FormDto.class)))
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Success",
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(example = """
                        {
                            "id": 1000,
                            "createdOn": "2023-09-19T09:45:13.761348Z",
                            "modifiedOn": "2023-09-19T09:46:32.937385248Z",
                            "name": "Student Form",
                            "description": "The student management",
                            "version": "v1.0.0",
                            "status": "Published",
                            "formJson": {
                                "Name": "Bharat kumar"
                            }
                        }
                        """))),
        @ApiResponse(responseCode = "400", description = "Bad Request",
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(example = """
                        {
                            "statusCode": 400,
                            "message": "BAD_REQUEST",
                            "details": [
                                "Required request body is missing"
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
    @PostMapping("/publish")
    public Form publishForm(@RequestBody FormDto formDto) throws DynamoException {
        log.info("Entering publishForm(), formPublishRequest: {}", formDto);
        Form form = formBuilderService.publishForm(formDto);
        log.info("Leaving publishForm(), form: {}", form);
        return form;
    }

    @Operation(summary = "Draft Form", description = "Drafts a new form based on the provided request.")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = FormDto.class)))
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Success",
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(example = """
                        {
                            "id": 1000,
                            "createdOn": "2023-09-19T09:45:13.761348Z",
                            "modifiedOn": "2023-09-19T09:46:32.937385248Z",
                            "name": "Employee Form",
                            "description": "The employee management",
                            "version": "v1.0.0",
                            "status": "Draft",
                            "formJson": {
                                "Name": "Bharat kumar"
                            }
                        }
                        """))),
        @ApiResponse(responseCode = "400", description = "Bad Request",
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(example = """
                        {
                            "statusCode": 400,
                            "message": "BAD_REQUEST",
                            "details": [
                                "Required request body is missing"
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
    @PostMapping("/draft")
    public Form draftForm(@RequestBody FormDto formDto) throws DynamoException {
        log.info("Entering draftForm(), formPublishRequest: {}", formDto);
        Form form = formBuilderService.draftForm(formDto);
        log.info("Leaving draftForm(), form: {}", form);
        return form;
    }

    @Operation(summary = "Retrieve Forms",
            description = "Retrieves a paginated list of forms based on specified criteria.")
    @Parameters(value = {
        @Parameter(allowEmptyValue = true, name = "page-no", example = "0", description = "Page number",
                in = ParameterIn.QUERY),
        @Parameter(allowEmptyValue = true, name = "page-size", example = "8", description = "Page size",
                in = ParameterIn.QUERY),
        @Parameter(allowEmptyValue = true, name = "sort", example = "name,ASC",
                description = "Sort by field with sort order", in = ParameterIn.QUERY),
        @Parameter(allowEmptyValue = true, name = "form-name", example = "Employee Form",
                description = "Form name search", in = ParameterIn.QUERY),
        @Parameter(allowEmptyValue = true, name = "form-date", example = "19/09/2023", description = "Form date search",
                in = ParameterIn.QUERY) })
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Success",
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(example = """
                        {
                            "content": [
                                {
                                    "id": 1000,
                                    "createdOn": "2023-09-19T10:07:50.024849Z",
                                    "modifiedOn": "2023-09-19T10:07:50.024852Z",
                                    "name": "Employee Form",
                                    "description": "Employee Management",
                                    "version": null,
                                    "status": "Draft",
                                    "formJson": {
                                        "components": [
                                            {
                                                "label": "Text field",
                                                "type": "textfield",
                                                "_parent": "Form_1v99ha5",
                                                "layout": {
                                                    "row": "Row_1vui8uy",
                                                    "columns": null
                                                },
                                                "id": "Field_1wiy0ec",
                                                "key": "textfield_vfddp",
                                                "_path": [
                                                    "components",
                                                    0
                                                ]
                                            },
                                            {
                                                "label": "Text area",
                                                "type": "textarea",
                                                "_parent": "Form_1v99ha5",
                                                "layout": {
                                                    "row": "Row_1ar3c1v",
                                                    "columns": null
                                                },
                                                "id": "Field_1b63o74",
                                                "key": "textarea_g2ksyh",
                                                "_path": [
                                                    "components",
                                                    1
                                                ]
                                            }
                                        ],
                                        "schemaVersion": 2,
                                        "exporter": {
                                            "name": "Camunda Modeler",
                                            "version": "5.0.0-alpha.1"
                                        },
                                        "editorAdditionalModules": [],
                                        "type": "default",
                                        "id": "Form_1v99ha5",
                                        "executionPlatform": "Camunda Platform",
                                        "executionPlatformVersion": "7.16.0",
                                        "_path": []
                                    }
                                },
                                {
                                    "id": 1001,
                                    "createdOn": "2023-09-19T10:08:32.104230Z",
                                    "modifiedOn": "2023-09-19T10:08:32.104232Z",
                                    "name": "Student Form",
                                    "description": "Student Management",
                                    "version": null,
                                    "status": "Draft",
                                    "formJson": {
                                        "components": [
                                            {
                                                "label": "Text field",
                                                "type": "textfield",
                                                "_parent": "Form_1v99ha5",
                                                "layout": {
                                                    "row": "Row_0niyopz",
                                                    "columns": null
                                                },
                                                "id": "Field_1nwj6sk",
                                                "key": "textfield_v2iwxf",
                                                "_path": [
                                                    "components",
                                                    0
                                                ]
                                            },
                                            {
                                                "label": "Text area",
                                                "type": "textarea",
                                                "_parent": "Form_1v99ha5",
                                                "layout": {
                                                    "row": "Row_0dcq1kh",
                                                    "columns": null
                                                },
                                                "id": "Field_1ax6z80",
                                                "key": "textarea_46eoxh",
                                                "_path": [
                                                    "components",
                                                    1
                                                ]
                                            }
                                        ],
                                        "schemaVersion": 2,
                                        "exporter": {
                                            "name": "Camunda Modeler",
                                            "version": "5.0.0-alpha.1"
                                        },
                                        "editorAdditionalModules": [],
                                        "type": "default",
                                        "id": "Form_1v99ha5",
                                        "executionPlatform": "Camunda Platform",
                                        "executionPlatformVersion": "7.16.0",
                                        "_path": []
                                    }
                                }
                            ],
                            "pageable": {
                                "sort": {
                                    "sorted": true,
                                    "empty": false,
                                    "unsorted": false
                                },
                                "pageNumber": 0,
                                "pageSize": 20,
                                "offset": 0,
                                "paged": true,
                                "unpaged": false
                            },
                            "last": true,
                            "totalPages": 1,
                            "totalElements": 2,
                            "size": 20,
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
    @GetMapping
    public Page<Form> retrieveForms(
            @Parameter(hidden = true, in = ParameterIn.QUERY, style = ParameterStyle.FORM)
            @SortDefault(sort = "name", direction = Sort.Direction.ASC) Pageable pageable,
            @RequestParam(required = false) MultiValueMap<String, String> searchParameters) throws DynamoException {
        log.info("Entering retrieveForms(), pageable: {}, searchParameters: {}", pageable, searchParameters);
        Page<Form> formPage = formBuilderService.retrieveForms(searchParameters, pageable);
        log.info("Leaving retrieveForms(), # of elements: {} in page-no: {}", formPage.getNumberOfElements(),
                formPage.getPageable().getPageNumber());
        return formPage;
    }

    @Operation(summary = "Archives a form", description = "Archives an existing form")
    @Parameter(name = "form-id", example = "1000", description = "Unique ID of the existing form",
            in = ParameterIn.PATH)
    @ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Success"),
        @ApiResponse(responseCode = "404", description = "Not found",
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(example = """
                        {
                            "statusCode": 404,
                            "message": "NOT_FOUND",
                            "details": [
                                "Form with ID '1000' not found"
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
    @DeleteMapping("/{form-id}")
    public void archiveForm(@PathVariable("form-id") long formId) {
        log.info("Entering archiveForm(), formId: {}", formId);
        formBuilderService.archiveForm(formId);
        log.info("Leaving archiveForm()");
    }

    @Operation(summary = "Retrieve a form", description = "Retrieve an existing form by its ID")
    @Parameter(name = "form-id", example = "1000", description = "Unique ID of the form to retrieve",
            in = ParameterIn.PATH)
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Success",
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(example = """
                        {
                            "id": 1000,
                            "name": "Form 1",
                            "description": "Demo Test 2",
                            "version": "v1.0.0",
                            "formJson": {
                                "name": "demo Test 2"
                            }
                        }
                        """))),
        @ApiResponse(responseCode = "404", description = "Not found",
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(example = """
                        {
                            "statusCode": 404,
                            "message": "NOT_FOUND",
                            "details": [
                                "Form with ID '1000' not found"
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
    public FormDto retrieveForm(@PathVariable("form-id") long formId) {
        log.info("Entering retrieveForm(), formId: {}", formId);
        FormDto formDto = formBuilderService.retrieveForm(formId);
        log.info("Leaving retrieveForm()");
        return formDto;
    }

    @Operation(summary = "Retrieve Form Versions",
            description = "Retrieves a paginated list of form versions for a specified form.")
    @Parameters(value = {
        @Parameter(allowEmptyValue = true, name = "page-no", example = "0", description = "Page number",
                in = ParameterIn.QUERY),
        @Parameter(allowEmptyValue = true, name = "page-size", example = "8", description = "Page size",
                in = ParameterIn.QUERY),
        @Parameter(allowEmptyValue = true, name = "sort", example = "id,ASC",
                description = "Sort by field with sort order", in = ParameterIn.QUERY),
        @Parameter(allowEmptyValue = true, name = "form-version", example = "v1.0.0",
                description = "Form version search", in = ParameterIn.QUERY),
        @Parameter(allowEmptyValue = false, name = "form-id", example = "1000",
                description = "Unique ID of the form to retrieve", in = ParameterIn.PATH) })
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Success",
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(example = """
                        {
                            "content": [
                                {
                                    "version": "v1.0.0",
                                    "modifiedOn": "2023-09-21T07:35:36.628290Z"
                                }
                            ],
                            "pageable": {
                                "sort": {
                                    "sorted": true,
                                    "empty": false,
                                    "unsorted": false
                                },
                                "pageNumber": 0,
                                "pageSize": 12,
                                "offset": 0,
                                "paged": true,
                                "unpaged": false
                            },
                            "last": true,
                            "totalElements": 1,
                            "totalPages": 1,
                            "size": 12,
                            "number": 0,
                            "sort": {
                                "sorted": true,
                                "empty": false,
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
    @GetMapping("/{form-id}/form-versions")
    public Page<FormVersionDto> retrieveFormVersions(
            @Parameter(hidden = true, in = ParameterIn.QUERY, style = ParameterStyle.FORM)
            @SortDefault(sort = "id", direction = Sort.Direction.ASC) Pageable pageable,
            @RequestParam(required = false) MultiValueMap<String, String> searchParameters,
            @PathVariable("form-id") long formId) throws DynamoException {
        log.info("Entering retrieveFormVersions(), pageable: {}, searchParameters: {}", pageable, searchParameters);
        Page<FormVersionDto> formVersionDtoPage =
                formBuilderService.retrieveFormVersions(formId, searchParameters, pageable);
        log.info("Leaving retrieveFormVersions(), # of elements: {} in page-no: {}",
                formVersionDtoPage.getNumberOfElements(), formVersionDtoPage.getPageable().getPageNumber());
        return formVersionDtoPage;
    }

    @GetMapping("/status-count")
    @Operation(summary = "Retrieves form status count.", description = "Retrieves form status count.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Success Payload",
                content = @Content(mediaType = "application/json",
                        schema = @Schema(implementation = Map.class, example = """
                                {
                                    "all": 1,
                                    "archived": 0,
                                    "draft": 0,
                                    "published": 1
                                }
                                """))),
        @ApiResponse(responseCode = "401", description = "Unauthorized request",
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                        schema = @Schema(implementation = ErrorResponse.class, example = """
                                {
                                    "statusCode": 401,
                                    "message": "UNAUTHORIZED",
                                    "details": [
                                        "Full authentication is required to access this resource"
                                    ]
                                }"""))) })
    public Map<String, Long> retrieveFormStatusCount() {
        log.info("Entering retrieveFormStatusCount()");
        Map<String, Long> statusCountList = formBuilderService.retrieveFormStatusCount();
        log.info("Leaving retrieveFormStatusCount()");
        return statusCountList;
    }

    @Operation(summary = "Retrieve a form", description = "Retrieve an existing form by its unique id")
    @Parameter(name = "unique-id", example = "7789W879", description = "Unique ID of the form to retrieve",
            in = ParameterIn.QUERY)
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Success",
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(example = """
                        {
                            "id": 1000,
                            "name": "Form 1",
                            "description": "Demo Test 2",
                            "version": "v1.0.0",
                            "formJson": {
                                "name": "demo Test 2"
                            }
                        }
                        """))),
        @ApiResponse(responseCode = "404", description = "Not found",
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(example = """
                        {
                            "statusCode": 404,
                            "message": "NOT_FOUND",
                            "details": [
                                "Form with unique ID '2545sdf' not found"
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
    @GetMapping("/form")
    public FormDto retrieveFormByUniqueId(@RequestParam("unique-id") String uniqueId) {
        log.info("Entering retrieveFormByUniqueId(), uniqueId: {}", uniqueId);
        FormDto formDto = formBuilderService.retrieveForm(uniqueId);
        log.info("Leaving retrieveFormByUniqueId()");
        return formDto;
    }

}