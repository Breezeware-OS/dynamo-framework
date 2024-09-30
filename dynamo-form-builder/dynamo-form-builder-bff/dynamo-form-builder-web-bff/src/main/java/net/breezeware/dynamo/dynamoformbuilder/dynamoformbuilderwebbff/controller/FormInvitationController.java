package net.breezeware.dynamo.dynamoformbuilder.dynamoformbuilderwebbff.controller;

import java.util.List;

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

import net.breezeware.dynamo.dynamoformbuilder.dynamoformbuilderwebbff.dto.FormInvitationDto;
import net.breezeware.dynamo.dynamoformbuilder.dynamoformbuilderwebbff.service.FormInvitationService;
import net.breezeware.dynamo.dynamoformbuilder.dynamoformsvc.entity.FormInvitation;
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

@Tag(name = "Form Invitation")
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping(value = "/api/form-invitations")
public class FormInvitationController {

    private final FormInvitationService formInvitationService;

    @Operation(summary = "Save FormInvitation",
            description = "Save a new form Invitation based on the provided request.")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = FormInvitationDto.class)))
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Success",
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)),
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
    public void saveFormInvitation(@RequestBody FormInvitationDto formInvitationDto) throws DynamoException {
        log.info("Entering saveFormInvitation(), formInvitationDto: {}", formInvitationDto);
        formInvitationService.saveFormInvitation(formInvitationDto);
        log.info("Leaving saveFormInvitation()");
    }

    @Operation(summary = "Retrieve Form Invitations",
            description = "Retrieves a paginated list of form Invitations based on specified criteria.")
    @Parameters(value = {
        @Parameter(allowEmptyValue = true, name = "form-id", example = "12", description = "Form id",
                in = ParameterIn.PATH),
        @Parameter(allowEmptyValue = true, name = "page-no", example = "0", description = "Page number",
                in = ParameterIn.QUERY),
        @Parameter(allowEmptyValue = true, name = "page-size", example = "8", description = "Page size",
                in = ParameterIn.QUERY),
        @Parameter(allowEmptyValue = true, name = "sort", example = "name,ASC",
                description = "Sort by field with sort order", in = ParameterIn.QUERY),
        @Parameter(allowEmptyValue = true, name = "email", example = "joe@gmail.com",
                description = "form invitation email search", in = ParameterIn.QUERY),
        @Parameter(allowEmptyValue = true, name = "form-invitation-date", example = "19/09/2023",
                description = "form invitation date search", in = ParameterIn.QUERY) })
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Success",
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(example = """
                        {
                            "content": [
                                {
                                    "id": 2,
                                    "createdOn": "2024-04-02T10:00:19.900156Z",
                                    "modifiedOn": "2024-04-02T10:02:05.876557Z",
                                    "email": "kishore@breezeware.net",
                                    "status": "submitted",
                                    "form": {
                                        "id": 1001,
                                        "createdOn": "2024-04-02T09:59:51.645837Z",
                                        "modifiedOn": "2024-04-02T09:59:51.645842Z",
                                        "name": "Patient Information",
                                        "description": "Patient info",
                                        "version": "1",
                                        "status": "Published",
                                        "uniqueId": "026e6a09",
                                        "accessType": "private",
                                        "owner": "siddharth@breezeware.net"
                                    }
                                }
                            ],
                            "pageable": {
                                "sort": {
                                    "empty": false,
                                    "unsorted": false,
                                    "sorted": true
                                },
                                "offset": 0,
                                "pageNumber": 0,
                                "pageSize": 8,
                                "paged": true,
                                "unpaged": false
                            },
                            "totalPages": 1,
                            "last": true,
                            "totalElements": 1,
                            "size": 8,
                            "number": 0,
                            "sort": {
                                "empty": false,
                                "unsorted": false,
                                "sorted": true
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
    public Page<FormInvitation> retrieveFormInvitations(
            @Parameter(hidden = true, in = ParameterIn.QUERY, style = ParameterStyle.FORM)
            @SortDefault(sort = "createdOn", direction = Sort.Direction.ASC) Pageable pageable,
            @RequestParam(required = false) MultiValueMap<String, String> searchParameters,
            @PathVariable("form-id") long formId) throws DynamoException {
        log.info("Entering retrieveFormInvitations(), pageable: {}, searchParameters: {}", pageable, searchParameters);
        Page<FormInvitation> formInvitations =
                formInvitationService.retrieveFormInvitations(formId, searchParameters, pageable);
        log.info("Leaving retrieveFormInvitations(), # of elements: {} in page-no: {}",
                formInvitations.getNumberOfElements(), formInvitations.getPageable().getPageNumber());
        return formInvitations;
    }

    @Operation(summary = "Deletes a form invitation",
            description = "Deletes a form invitation for given valid identifier")
    @Parameter(name = "form-invitation-id", example = "1", description = "Unique ID of the existing form invitation",
            in = ParameterIn.PATH)
    @ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Success"), @ApiResponse(
            responseCode = "404", description = "Not found",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(example = """
                    {
                        "statusCode": 404,
                        "message": "NOT_FOUND",
                        "details": [
                            "Invalid Form Invitation ID: -1. Form Invitation IDs must be greater than or equal to 1."
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
    @DeleteMapping("/{form-invitation-id}")
    public void deleteFormInvitation(@PathVariable("form-invitation-id") long formInvitationId) {
        log.info("Entering deleteFormInvitation(), invitationId: {}", formInvitationId);
        formInvitationService.deleteFormInvitation(formInvitationId);
        log.info("Leaving deleteFormInvitation()");
    }

    @Operation(summary = "Retrieves email list for form invitation",
            description = "Retrieves email list for form invitation based on user roles.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Success",
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(example = """
                        {
                           "jeo@gmal.com",
                           "deo@gmal.com",
                           "john@gmal.com"
                        }"""))),
        @ApiResponse(responseCode = "401", description = "Unauthorized request",
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(example = """
                        {
                            "statusCode": 401,
                            "message": "UNAUTHORIZED",
                            "details": [
                                "Full authentication is required to access this resource"
                            ]
                        }"""))) })
    @GetMapping("/email-list")
    public List<String> retrieveUsersEmailForInvitation() throws DynamoException {
        log.info("Entering retrieveFormInvitations()");
        List<String> emailList = formInvitationService.retrieveUsersEmailForInvitation();
        log.info("Leaving retrieveUsersEmailForInvitation()");
        return emailList;
    }

}
