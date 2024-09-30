package net.breezeware.dynamo.dynamodocswebbff.controller;

import java.util.List;
import java.util.UUID;

import jakarta.servlet.http.HttpServletResponse;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.SortDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import net.breezeware.dynamo.dynamodocswebbff.dto.DocumentDto;
import net.breezeware.dynamo.dynamodocswebbff.dto.DocumentViewDto;
import net.breezeware.dynamo.dynamodocswebbff.dto.RevisionDto;
import net.breezeware.dynamo.dynamodocswebbff.dto.UpdateDocumentResponseDto;
import net.breezeware.dynamo.dynamodocswebbff.service.MarkdownDocumentService;
import net.breezeware.dynamo.utils.exception.DynamoException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Markdown Document")
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping(value = "/api/documents")
public class DocumentController {

    private final MarkdownDocumentService documentService;

    @PostMapping
    @Operation(summary = "Create a Document", description = "Creates a new Document.")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            content = @Content(mediaType = "application/json", schema = @Schema(example = """
                    {
                        "collectionId": "df1000e3-2b70-466d-9aa6-b62ccce55a3c",
                        "parentId": ""
                    }
                    """)))
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Created - Document created successfully",
                content = @Content(mediaType = "application/json", schema = @Schema(example = """
                           {
                               "id": 30,
                               "uniqueId": "40b7d960-32a6-49bc-8ecf-b2aab19a2af3",
                               "collectionId": "df1000e3-2b70-466d-9aa6-b62ccce55a3c",
                               "version": 1,
                               "status": "drafted",
                               "createdOn": "2024-04-17T07:16:14.604808611Z",
                               "modifiedOn": "2024-04-17T07:16:14.604811332Z"
                           }
                        """))),
        @ApiResponse(responseCode = "400", description = "Bad Request - Invalid Document details",
                content = @Content(mediaType = "application/json", schema = @Schema(example = """
                        {
                            "statusCode": 400,
                            "message": "BAD_REQUEST",
                            "details": [
                                "Invalid UUID for User."
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
    public DocumentDto createDocument(@RequestBody DocumentDto documentDto) {
        log.info("Entering createDocument()");
        DocumentDto savedDocumentDto = documentService.createDocument(documentDto);
        log.info("Leaving createDocument()");
        return savedDocumentDto;
    }

    @PutMapping("/publish")
    @Operation(summary = "Publish a Document", description = "Publishes a new Document.")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            content = @Content(mediaType = "application/json", schema = @Schema(example = """
                    {
                        "id": 20,
                        "uniqueId": "0e2e7f05-923b-4b23-b52d-4a87c1204ce1",
                        "title": "Test",
                        "content": "# sathish",
                        "collectionId": "b7a5a2b0-9312-4820-910b-b584cfd7f3eb"
                    }
                    """)))
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "204", description = "Document - Published successfully",
                        content = @Content(mediaType = "application/json",
                                schema = @Schema(implementation = Void.class))),
                @ApiResponse(responseCode = "400", description = "Bad Request - Invalid Document details",
                        content = @Content(mediaType = "application/json", schema = @Schema(example = """
                                {
                                    "statusCode": 400,
                                    "message": "BAD_REQUEST",
                                    "details": [
                                        "Name is missing or blank",
                                        "Please provide valid document unique id"
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
                                """))),
                @ApiResponse(responseCode = "404", description = "Not Found - Document Id",
                        content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(example = """
                                {
                                    "statusCode": 404,
                                    "message": "NOT_FOUND",
                                    "details": [
                                        "Document with unique-id 'c38b2827-d3d4-4fc1-b508-90b7f96c58c9' not found"
                                    ]
                                }
                                """))) })
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void publishDocument(@RequestBody DocumentDto documentDto) {
        log.info("Entering publishDocument()");
        documentService.publishDocument(documentDto);
        log.info("Leaving publishDocument()");
    }

    @PutMapping("/{document-id}/fork-check")
    @Operation(summary = "Fork Check a Document", description = "Forks Checked a Document.")
    @Parameter(description = "Document unique identifier", example = "c38b2827-d3d4-4fc1-b508-90b7f96c58c9",
            required = true, in = ParameterIn.PATH)
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Document Fork Checked - successfully",
                content = @Content(mediaType = "application/json", schema = @Schema(example = """
                        {
                            "documentDto": {
                                "id": 31,
                                "uniqueId": "e4df8b15-f134-4316-9b2e-eeb67450953e",
                                "collectionId": "54e45b0f-651b-4919-93ba-d2ac3ff33f0e",
                                "title": "Hello",
                                "content": "",
                                "version": 1,
                                "status": "published",
                                "publishedOn": "2024-04-17T07:23:41.033925Z",
                                "children": [],
                                "createdOn": "2024-04-17T07:23:41.037258Z",
                                "modifiedOn": "2024-04-17T07:23:41.037260Z"
                            },
                            "forked": true
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
                        """))),
        @ApiResponse(responseCode = "404", description = "Not Found - Collection Id",
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(example = """
                        {
                            "statusCode": 404,
                            "message": "NOT_FOUND",
                            "details": [
                                "Document with unique-id 'c38b2827-d3d4-4fc1-b508-90b7f96c58c9' not found"
                            ]
                        }
                        """))) })
    public UpdateDocumentResponseDto updateDocumentWithForkCheck(@PathVariable(name = "document-id") UUID documentId) {
        log.info("Entering updateDocumentWithForkCheck()");
        UpdateDocumentResponseDto updateDocumentResponseDto = documentService.updateDocumentWithForkCheck(documentId);
        log.info("Leaving updateDocumentWithForkCheck()");
        return updateDocumentResponseDto;
    }

    @PutMapping
    @Operation(summary = "Update a Document", description = "Updates a new Document.")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            content = @Content(mediaType = "application/json", schema = @Schema(example = """
                    {
                        "id": 30,
                        "uniqueId": "40b7d960-32a6-49bc-8ecf-b2aab19a2af3",
                        "title": "Test",
                        "content": "# sathish",
                        "collectionId": "b7a5a2b0-9312-4820-910b-b584cfd7f3eb"
                    }
                    """)))
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "204", description = "Document - Published successfully",
                        content = @Content(mediaType = "application/json",
                                schema = @Schema(implementation = Void.class))),
                @ApiResponse(responseCode = "400", description = "Bad Request - Invalid Document details",
                        content = @Content(mediaType = "application/json", schema = @Schema(example = """
                                {
                                    "statusCode": 400,
                                    "message": "BAD_REQUEST",
                                    "details": [
                                        "Name is missing or blank",
                                        "Please provide valid document unique id"
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
                                """))),
                @ApiResponse(responseCode = "404", description = "Not Found - Document Id",
                        content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(example = """
                                {
                                    "statusCode": 404,
                                    "message": "NOT_FOUND",
                                    "details": [
                                        "Document with unique-id 'c38b2827-d3d4-4fc1-b508-90b7f96c58c9' not found"
                                    ]
                                }
                                """))) })
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateDocument(@RequestBody DocumentDto documentDto) {
        log.info("Entering updateDocument()");
        documentService.updateDocument(documentDto);
        log.info("Leaving updateDocument()");
    }

    @GetMapping
    @Operation(summary = "Retrieve Page of Document", description = "Retrieve Documents record.")
    @Parameter(description = "Collection unique identifiers",
            example = "c38b2827-d3d4-4fc1-b508-90b7f96c58c9,c38b2827-d3d4-4fc1-b508-90b7f96c58c9", required = false,
            in = ParameterIn.PATH)
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200",
                description = "Retrieve Page of Document with default pagination and sorting",
                content = @Content(mediaType = "application/json", schema = @Schema(example = """
                        {
                             "content": [
                                   {
                                                               "id": 27,
                                                               "uniqueId":
                                                               "395bea32-7cc0-45c9-8112-a93dd51c1cd2",
                                                               "collectionId":
                                                               "9fe4bfe8-f431-42ab-ab30-eef348fb5bf2",
                                                               "parentId":
                                                               "a28730bf-15ba-46ff-9655-12e7362e1c90",
                                                               "title": "PostgREST API Summary",
                                                               "content": "## Test",
                                                               "htmlContent": "<h2>Test</h2>\\n",
                                                               "version": 1,
                                                               "status": "published",
                                                               "createdByUserId":
                                                               "4c2bb19c-854d-4320-b257-bae33e1fe279",
                                                               "createdUserFirstName": "Siddharth",
                                                               "createdUserLastName": "Elancheziyan",
                                                               "publishedOn":
                                                               "2024-04-24T05:59:18.411256Z",
                                                               "children": [],
                                                               "createdOn":
                                                               "2024-04-24T05:59:18.415089Z",
                                                               "modifiedOn":
                                                               "2024-04-24T05:59:18.415091Z"
                                                           },
                                                           {
                                                               "id": 26,
                                                               "uniqueId":
                                                               "a28730bf-15ba-46ff-9655-12e7362e1c90",
                                                               "collectionId":
                                                               "9fe4bfe8-f431-42ab-ab30-eef348fb5bf2",
                                                               "title": "gtfhgfh",
                                                               "content": "",
                                                               "htmlContent": "",
                                                               "version": 1,
                                                               "status": "published",
                                                               "createdByUserId":
                                                               "4c2bb19c-854d-4320-b257-bae33e1fe279",
                                                               "createdUserFirstName": "Siddharth",
                                                               "createdUserLastName": "Elancheziyan",
                                                               "publishedOn":
                                                               "2024-04-24T05:41:11.849561Z",
                                                               "children": [
                                                                   {
                                                                       "id": 27,
                                                                       "uniqueId":
                                                                       "395bea32-7cc0-45c9-8112-
                                                                       a93dd51c1cd2",
                                                                       "collectionId":
                                                                       "9fe4bfe8-f431-42ab-ab30-
                                                                       eef348fb5bf2",
                                                                       "parentId":
                                                                       "a28730bf-15ba-46ff-9655-
                                                                       12e7362e1c90",
                                                                       "title": "PostgREST API Summary",
                                                                       "content": "## Test",
                                                                       "version": 1,
                                                                       "status": "published",
                                                                       "createdByUserId":
                                                                       "4c2bb19c-854d-4320-b257-
                                                                       bae33e1fe279",
                                                                       "createdUserFirstName":
                                                                       "Siddharth",
                                                                       "createdUserLastName":
                                                                       "Elancheziyan",
                                                                       "publishedOn":
                                                                       "2024-04-24T05:59:18.411256Z",
                                                                       "children": [],
                                                                       "createdOn":
                                                                       "2024-04-24T05:59:18.415089Z",
                                                                       "modifiedOn":
                                                                       "2024-04-24T05:59:18.415091Z"
                                                                   }
                                                               ],
                                                               "createdOn":
                                                               "2024-04-24T05:41:08.773009Z",
                                                               "modifiedOn":
                                                               "2024-04-24T05:41:11.851616Z"
                                                           }
                             ],
                             "pageable": {
                                 "sort": {
                                     "empty": false,
                                     "sorted": true,
                                     "unsorted": false
                                 },
                                 "offset": 0,
                                 "pageSize": 8,
                                 "pageNumber": 0,
                                 "paged": true,
                                 "unpaged": false
                             },
                             "last": true,
                             "totalPages": 1,
                             "totalElements": 2,
                             "size": 12,
                             "number": 0,
                             "sort": {
                                 "empty": false,
                                 "sorted": true,
                                 "unsorted": false
                             },
                             "first": true,
                             "numberOfElements": 2,
                             "empty": false
                         }
                        """))),
        @ApiResponse(responseCode = "200", description = "Retrieve Page of Document with search-by document name",
                content = @Content(mediaType = "application/json", schema = @Schema(example = """
                        {
                               "content": [
                                           {
                                                               "id": 27,
                                                               "uniqueId":
                                                               "395bea32-7cc0-45c9-8112-a93dd51c1cd2",
                                                               "collectionId":
                                                               "9fe4bfe8-f431-42ab-ab30-eef348fb5bf2",
                                                               "parentId":
                                                               "a28730bf-15ba-46ff-9655-12e7362e1c90",
                                                               "title": "PostgREST API Summary",
                                                               "content": "## Test",
                                                               "htmlContent": "<h2>Test</h2>\\n",
                                                               "version": 1,
                                                               "status": "published",
                                                               "createdByUserId":
                                                               "4c2bb19c-854d-4320-b257-bae33e1fe279",
                                                               "createdUserFirstName": "Siddharth",
                                                               "createdUserLastName": "Elancheziyan",
                                                               "publishedOn":
                                                               "2024-04-24T05:59:18.411256Z",
                                                               "children": [],
                                                               "createdOn":
                                                               "2024-04-24T05:59:18.415089Z",
                                                               "modifiedOn":
                                                               "2024-04-24T05:59:18.415091Z"
                                                           }
                              ],
                              "pageable": {
                                  "sort": {
                                      "empty": false,
                                      "sorted": true,
                                      "unsorted": false
                                  },
                                  "offset": 0,
                                  "pageSize": 8,
                                  "pageNumber": 0,
                                  "paged": true,
                                  "unpaged": false
                              },
                              "last": true,
                              "totalPages": 1,
                              "totalElements": 8,
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
        @ApiResponse(responseCode = "200", description = "Retrieve Page of Document with search-by unknown name",
                content = @Content(mediaType = "application/json", schema = @Schema(example = """
                        {
                            "content": [
                            ],
                            "pageable": {
                                "sort": {
                                    "empty": false,
                                    "sorted": true,
                                    "unsorted": false
                                },
                                "offset": 0,
                                "pageSize": 8,
                                "pageNumber": 0,
                                "paged": true,
                                "unpaged": false
                            },
                            "last": true,
                            "totalPages": 1,
                            "totalElements": 8,
                            "size": 12,
                            "number": 0,
                            "sort": {
                                "empty": false,
                                "sorted": true,
                                "unsorted": false
                            },
                            "first": true,
                            "numberOfElements": 0,
                            "empty": false
                        }
                        """))),
        @ApiResponse(responseCode = "200", description = "Retrieve Page of Document with custom pagination and sorting",
                content = @Content(mediaType = "application/json", schema = @Schema(example = """
                        {
                             "content": [
                                                {
                                                                   "id": 27,
                                                                   "uniqueId":
                                                                   "395bea32-7cc0-45c9-8112-a93dd51c1cd2",
                                                                   "collectionId":
                                                                   "9fe4bfe8-f431-42ab-ab30-eef348fb5bf2",
                                                                   "parentId":
                                                                   "a28730bf-15ba-46ff-9655-12e7362e1c90",
                                                                   "title": "PostgREST API Summary",
                                                                   "content": "## Test",
                                                                   "htmlContent": "<h2>Test</h2>\\n",
                                                                   "version": 1,
                                                                   "status": "published",
                                                                   "createdByUserId":
                                                                   "4c2bb19c-854d-4320-b257-bae33e1fe279",
                                                                   "createdUserFirstName": "Siddharth",
                                                                   "createdUserLastName": "Elancheziyan",
                                                                   "publishedOn":
                                                                   "2024-04-24T05:59:18.411256Z",
                                                                   "children": [],
                                                                   "createdOn":
                                                                   "2024-04-24T05:59:18.415089Z",
                                                                   "modifiedOn":
                                                                   "2024-04-24T05:59:18.415091Z"
                                                               },
                                                               {
                                                                   "id": 26,
                                                                   "uniqueId":
                                                                   "a28730bf-15ba-46ff-9655-
                                                                   12e7362e1c90",
                                                                   "collectionId":
                                                                   "9fe4bfe8-f431-42ab-ab30-
                                                                   eef348fb5bf2",
                                                                   "title": "gtfhgfh",
                                                                   "content": "",
                                                                   "htmlContent": "",
                                                                   "version": 1,
                                                                   "status": "published",
                                                                   "createdByUserId":
                                                                   "4c2bb19c-854d-4320-b257-
                                                                   bae33e1fe279",
                                                                   "createdUserFirstName": "Siddharth",
                                                                   "createdUserLastName":
                                                                   "Elancheziyan",
                                                                   "publishedOn":
                                                                   "2024-04-24T05:41:11.849561Z",
                                                                   "children": [
                                                                       {
                                                                           "id": 27,
                                                                           "uniqueId":
                                                                           "395bea32-7cc0-45c9-8112-
                                                                           a93dd51c1cd2",
                                                                           "collectionId":
                                                                           "9fe4bfe8-f431-42ab-ab30-
                                                                           eef348fb5bf2",
                                                                           "parentId":
                                                                           "a28730bf-15ba-46ff-9655-
                                                                           12e7362e1c90",
                                                                           "title": "PostgREST API
                                                                           Summary",
                                                                           "content": "## Test",
                                                                           "version": 1,
                                                                           "status": "published",
                                                                           "createdByUserId":
                                                                           "4c2bb19c-854d-4320-b257-
                                                                           bae33e1fe279",
                                                                           "createdUserFirstName":
                                                                           "Siddharth",
                                                                           "createdUserLastName":
                                                                           "Elancheziyan",
                                                                           "publishedOn":
                                                                           "2024-04-24T05:59:18.
                                                                           411256Z",
                                                                           "children": [],
                                                                           "createdOn":
                                                                           "2024-04-24T05:59:18.
                                                                           415089Z",
                                                                           "modifiedOn":
                                                                           "2024-04-24T05:59:18.415091Z"
                                                                       }
                                                                   ],
                                                                   "createdOn":
                                                                   "2024-04-24T05:41:08.773009Z",
                                                                   "modifiedOn":
                                                                   "2024-04-24T05:41:11.851616Z"
                                                               }
                             ],
                             "pageable": {
                                 "sort": {
                                     "empty": false,
                                     "sorted": true,
                                     "unsorted": false
                                 },
                                 "offset": 0,
                                 "pageSize": 8,
                                 "pageNumber": 0,
                                 "paged": true,
                                 "unpaged": false
                             },
                             "last": true,
                             "totalPages": 2,
                             "totalElements": 10,
                             "size": 8,
                             "number": 1,
                             "sort": {
                                 "empty": false,
                                 "sorted": true,
                                 "unsorted": false
                             },
                             "first": true,
                             "numberOfElements": 2,
                             "empty": false
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
                        """))) })
    public DocumentViewDto retrieveDocuments(
            @PageableDefault(size = 10, page = 0, sort = "modifiedOn",
                    direction = Sort.Direction.DESC) Pageable pageable,
            @RequestParam(name = "collection-ids", required = false, defaultValue = "") List<String> collectionIds,
            @RequestParam(required = false) MultiValueMap<String, String> searchParameters) {
        log.info("Entering retrieveDocuments()");
        DocumentViewDto documentViewDto = documentService.retrieveDocuments(pageable, collectionIds, searchParameters);
        log.info("Leaving retrieveDocuments()");
        return documentViewDto;
    }

    @PutMapping("/{document-id}/archive")
    @Operation(summary = "Archive a Document", description = "Archives a Document record.")
    @Parameter(description = "Document unique identifier", example = "c38b2827-d3d4-4fc1-b508-90b7f96c58c9",
            required = true, in = ParameterIn.PATH)
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "204", description = "Archived - Document deleted successfully",
                        content = @Content(mediaType = "application/json",
                                schema = @Schema(implementation = Void.class))),
                @ApiResponse(responseCode = "401", description = "Unauthorized request",
                        content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(example = """
                                {
                                    "statusCode": 401,
                                    "message": "UNAUTHORIZED",
                                    "details": [
                                        "Full authentication is required to access this resource"
                                    ]
                                }
                                """))),
                @ApiResponse(responseCode = "404", description = "Not Found - Collection Id",
                        content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(example = """
                                {
                                    "statusCode": 404,
                                    "message": "NOT_FOUND",
                                    "details": [
                                        "Document with unique-id 'c38b2827-d3d4-4fc1-b508-90b7f96c58c9' not found"
                                    ]
                                }
                                """))) })
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void archiveDocument(@PathVariable(name = "document-id") UUID documentId) {
        log.info("Entering archiveDocument()");
        documentService.archiveDocument(documentId);
        log.info("Leaving archiveDocument()");
    }

    @PutMapping("/{document-id}/delete")
    @Operation(summary = "Delete a Document", description = "Deletes a Document record.")
    @Parameter(description = "Document unique identifier", example = "c38b2827-d3d4-4fc1-b508-90b7f96c58c9",
            required = true, in = ParameterIn.PATH)
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "204", description = "Deleted - Document deleted successfully",
                        content = @Content(mediaType = "application/json",
                                schema = @Schema(implementation = Void.class))),
                @ApiResponse(responseCode = "401", description = "Unauthorized request",
                        content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(example = """
                                {
                                    "statusCode": 401,
                                    "message": "UNAUTHORIZED",
                                    "details": [
                                        "Full authentication is required to access this resource"
                                    ]
                                }
                                """))),
                @ApiResponse(responseCode = "404", description = "Not Found - Collection Id",
                        content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(example = """
                                {
                                    "statusCode": 404,
                                    "message": "NOT_FOUND",
                                    "details": [
                                        "Document with unique-id 'c38b2827-d3d4-4fc1-b508-90b7f96c58c9' not found"
                                    ]
                                }
                                """))) })
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteDocument(@PathVariable(name = "document-id") UUID documentId,
            @RequestParam(name = "permanent", required = false) boolean isPermanent) {
        log.info("Entering deleteDocument()");
        documentService.deleteDocument(documentId, isPermanent);
        log.info("Leaving deleteDocument()");
    }

    @GetMapping("/{document-id}/download")
    @Operation(summary = "Download a Document", description = "Download a Document record.")
    @Parameter(description = "Document unique identifier", example = "c38b2827-d3d4-4fc1-b508-90b7f96c58c9",
            required = true, in = ParameterIn.PATH)
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "204", description = "Downloaded - Document deleted successfully",
                        content = @Content(mediaType = "application/json",
                                schema = @Schema(implementation = Void.class))),
                @ApiResponse(responseCode = "401", description = "Unauthorized request",
                        content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(example = """
                                {
                                    "statusCode": 401,
                                    "message": "UNAUTHORIZED",
                                    "details": [
                                        "Full authentication is required to access this resource"
                                    ]
                                }
                                """))),
                @ApiResponse(responseCode = "404", description = "Not Found - Collection Id",
                        content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(example = """
                                {
                                    "statusCode": 404,
                                    "message": "NOT_FOUND",
                                    "details": [
                                        "Document with unique-id 'c38b2827-d3d4-4fc1-b508-90b7f96c58c9' not found"
                                    ]
                                }
                                """))) })
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void downloadDocument(@PathVariable(name = "document-id") UUID documentId,
            HttpServletResponse httpServletResponse) throws DynamoException {
        log.info("Entering downloadDocument()");
        documentService.downloadDocument(documentId, httpServletResponse);
        log.info("Leaving downloadDocument()");
    }

    @GetMapping("/{document-id}/revisions")
    @Operation(summary = "Retrieve List of Document Revisions", description = "Retrieve Documents Revision record.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Retrieve List of Document  Revision with default sorting",
                content = @Content(mediaType = "application/json", schema = @Schema(example = """
                            [
                                {
                                    "id": 15,
                                    "uniqueId": "764b2346-c0de-4173-b440-d4ff87890513",
                                    "documentId": "cf421ab8-f3ac-4035-a8f2-e4f4609277f2",
                                    "collectionId": "9dc865cc-e861-475b-a306-648448382f29",
                                    "title": "Cadence modulith backend application and libraries",
                                    "content": "\\n\\n- API application (api-app)\\n- API library
                                    (api-lib)\\n    - API Server BFFs.\\n    - API Server Services.\\n",
                                    "version": 2,
                                    "status": "published",
                                    "editedOn": "2024-04-16T13:20:56.271Z",
                                    "createdOn": "2024-04-16T13:15:56.271386Z",
                                    "modifiedOn": "2024-04-16T13:15:56.271387Z"
                                }
                            ]
                        """))),
        @ApiResponse(responseCode = "200", description = "Retrieve List of Document Revisions with custom sorting",
                content = @Content(mediaType = "application/json", schema = @Schema(example = """
                          [
                                  {
                                      "id": 15,
                                      "uniqueId": "764b2346-c0de-4173-b440-d4ff87890513",
                                      "documentId": "cf421ab8-f3ac-4035-a8f2-e4f4609277f2",
                                      "collectionId": "9dc865cc-e861-475b-a306-648448382f29",
                                      "title": "Cadence modulith backend application and libraries",
                                      "content": "\\n\\n- API application (api-app)\\n- API library
                                      (api-lib)\\n    - API Server BFFs.\\n    - API Server Services.\\n",
                                      "version": 2,
                                      "status": "published",
                                      "editedOn": "2024-04-16T13:20:56.271Z",
                                      "createdOn": "2024-04-16T13:15:56.271386Z",
                                      "modifiedOn": "2024-04-16T13:15:56.271387Z"
                                  }
                              ]
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
                        """))) })
    public List<RevisionDto> retrieveDocumentRevisions(@PathVariable(name = "document-id") UUID documentId,
            @SortDefault(sort = "editedOn", direction = Sort.Direction.DESC) Sort sort,
            @RequestParam(required = false) MultiValueMap<String, String> searchParameters) {
        log.info("Entering retrieveDocumentRevisions()");
        List<RevisionDto> revisionDtos = documentService.retrieveDocumentRevisions(documentId, sort, searchParameters);
        log.info("Leaving retrieveDocumentRevisions()");
        return revisionDtos;
    }

}
