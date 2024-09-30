package net.breezeware.dynamo.dynamodocswebbff.controller;

import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import net.breezeware.dynamo.dynamodocswebbff.dto.CollectionDto;
import net.breezeware.dynamo.dynamodocswebbff.dto.CollectionViewDto;
import net.breezeware.dynamo.dynamodocswebbff.service.MarkdownCollectionService;

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

@Tag(name = "Markdown Collection")
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping(value = "/api/collections")
public class CollectionController {

    private final MarkdownCollectionService markdownCollectionService;

    @PostMapping
    @Operation(summary = "Create a Collection", description = "Creates a new Collection.")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            content = @Content(mediaType = "application/json", schema = @Schema(example = """
                    {
                        "name":"Test",
                        "description":"",
                        "documentPermission":"read_write"
                    }
                    """)))
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Created - Collection created successfully",
                content = @Content(mediaType = "application/json", schema = @Schema(example = """
                            {
                                    "id": 15,
                                    "uniqueId": "fb68eaab-d81f-4d26-8fb0-7547c4f0a90d",
                                    "name": "Test",
                                    "description": "",
                                    "documentPermission": "read_write",
                                    "createdOn": "2024-04-17T04:52:04.937170770Z",
                                    "modifiedOn": "2024-04-17T04:52:04.937173563Z"
                                }
                        """))),
        @ApiResponse(responseCode = "400", description = "Bad Request - Invalid Collection details",
                content = @Content(mediaType = "application/json", schema = @Schema(example = """
                        {
                            "statusCode": 400,
                            "message": "BAD_REQUEST",
                            "details": [
                                "Name is missing or blank",
                                "Permission is missing or blank"
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
    public CollectionDto createCollection(@RequestBody CollectionDto collectionDto) {
        log.info("Entering createCollection()");
        CollectionDto savedCollection = markdownCollectionService.createCollection(collectionDto);
        log.info("Leaving createCollection()");
        return savedCollection;
    }

    @PutMapping
    @Operation(summary = "Update a Collection", description = "Updates a new Collection.")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            content = @Content(mediaType = "application/json", schema = @Schema(example = """
                    {
                        "id":15,
                        "uniqueId":"fb68eaab-d81f-4d26-8fb0-7547c4f0a90d",
                        "name":"Test1",
                        "description":"Test",
                        "documentPermission":"read_write"
                    }
                    """)))
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Created - Collection created successfully",
                content = @Content(mediaType = "application/json", schema = @Schema(example = """
                            {
                                    "id": 15,
                                    "uniqueId": "fb68eaab-d81f-4d26-8fb0-7547c4f0a90d",
                                    "name": "Test1",
                                    "description": "Test",
                                    "documentPermission": "read_write",
                                    "createdOn": "2024-04-17T04:52:04.937171Z",
                                    "modifiedOn": "2024-04-17T04:55:13.638495184Z"
                                }
                        """))),
        @ApiResponse(responseCode = "400", description = "Bad Request - Invalid Collection details",
                content = @Content(mediaType = "application/json", schema = @Schema(example = """
                        {
                            "statusCode": 400,
                            "message": "BAD_REQUEST",
                            "details": [
                                "Name is missing or blank",
                                "Permission is missing or blank"
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
        @ApiResponse(responseCode = "404", description = "Not Found - Collection Id",
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(example = """
                        {
                            "statusCode": 404,
                            "message": "NOT_FOUND",
                            "details": [
                                "Collection with unique-id 'c38b2827-d3d4-4fc1-b508-90b7f96c58c9' not found"
                            ]
                        }
                        """))) })
    public CollectionDto updateCollection(@RequestBody CollectionDto collectionDto) {
        log.info("Entering updateCollection()");
        CollectionDto updatedCollection = markdownCollectionService.updateCollection(collectionDto);
        log.info("Leaving updateCollection()");
        return updatedCollection;
    }

    @DeleteMapping("/{collection-id}")
    @Operation(summary = "Deletes a Collection", description = "Deletes a Collection record.")
    @Parameter(description = "Collection unique identifier", example = "c38b2827-d3d4-4fc1-b508-90b7f96c58c9",
            required = true, in = ParameterIn.PATH)
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "204", description = "Deleted - Collection deleted successfully",
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
                                        "Collection with unique-id 'c38b2827-d3d4-4fc1-b508-90b7f96c58c9' not found"
                                    ]
                                }
                                """))) })
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCollection(@PathVariable(name = "collection-id") UUID collectionId) {
        log.info("Entering deleteCollection()");
        markdownCollectionService.deleteCollection(collectionId);
        log.info("Leaving deleteCollection()");

    }

    @GetMapping
    @Operation(summary = "Retrieve List of Collection", description = "Retrieve Collections record.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Retrieve List of Collection with default sorting",
                content = @Content(mediaType = "application/json", schema = @Schema(example = """
                        {
                                           "paged": false,
                                           "data": [
                                               {
                                                   "id": 9,
                                                   "uniqueId": "9dc865cc-e861-475b-a306-648448382f29",
                                                   "name": "test edit",
                                                   "documentPermission": "no_access",
                                                   "createdOn": "2024-04-16T05:47:39.224524Z",
                                                   "modifiedOn": "2024-04-16T06:07:45.325309Z",
                                                   "documentList": [
                                                       {
                                                           "id": 23,
                                                           "uniqueId":
                                                           "f37a4754-01b3-42e0-b357-ff8393e04ecd",
                                                           "collectionId":
                                                           "9dc865cc-e861-475b-a306-648448382f29",
                                                           "title": "Cadence
                                                           modulith backend application and
                                                           libraries",
                                                           "content":
                                                           "\\n\\n- API application (api-app)\\n-
                                                           API library (api-lib)\\n
                                                            - API Server BFFs.\\n
                                                            - API Server Services.\\n",
                                                           "htmlContent": "<ul>\\n<li>API
                                                           application (api-app)
                                                           </li>\\n<li>API library (api-lib)\\
                                                           n<ul>\\n<li>API
                                                           Server BFFs.</li>\\n<li>API Server
                                                           Services.</li>
                                                           \\n</ul>\\n</li>\\n</ul>\\n",
                                                           "version": 1,
                                                           "status": "published",
                                                           "publishedOn": "2024-04-16T12:58:53.288185Z",
                                                           "children": [
                                                               {
                                                                   "id": 28,
                                                                   "uniqueId":
                                                                   "0e13a3d9-055c-4d67-a70e-
                                                                   11840f8fc81f",
                                                                   "collectionId":
                                                                   "9dc865cc-e861-475b-a306-
                                                                   648448382f29",
                                                                   "parentId":
                                                                   "f37a4754-01b3-42e0-b357-
                                                                   ff8393e04ecd",
                                                                   "title": "Cadence modulith backend
                                                                   application and libraries",
                                                                   "content": "\\n\\n- API application
                                                                   (api-app)\\n- API library
                                                                   (api-lib)\\n
                                                                   - API Server BFFs.\\n    - API Server
                                                                   Services.\\n",
                                                                   "htmlContent": "<ul>\\n<li>
                                                                   API application
                                                                   (api-app)</li>\\n<li>API library
                                                                   (api-lib)\\n<ul>\\n<li>API Server
                                                                   BFFs.
                                                                   </li>\\n<li>API Server Services.
                                                                   </li>\\
                                                                   n</ul>\\n</li>\\n</ul>\\n",
                                                                   "version": 1,
                                                                   "status": "published",
                                                                   "publishedOn":
                                                                   "2024-04-16T13:15:56.266282Z",
                                                                   "children": [],
                                                                   "createdOn":
                                                                   "2024-04-16T13:15:56.269603Z",
                                                                   "modifiedOn":
                                                                   "2024-04-16T13:15:56.269604Z"
                                                               }
                                                           ],
                                                           "createdOn": "2024-04-16T12:58:53.288428Z",
                                                           "modifiedOn": "2024-04-16T13:03:25.605873Z"
                                                       }]}]}
                                  """))),
        @ApiResponse(responseCode = "200", description = "Retrieve List of Collection with search-by Document Title",
                content = @Content(mediaType = "application/json", schema = @Schema(example = """
                        {
                                "paged": false,
                                "data": [
                                    {
                                        "id": 9,
                                        "uniqueId": "9dc865cc-e861-475b-a306-648448382f29",
                                        "name": "test edit",
                                        "documentPermission": "no_access",
                                        "createdOn": "2024-04-16T05:47:39.224524Z",
                                        "modifiedOn": "2024-04-16T06:07:45.325309Z",
                                        "documentList": [
                                            {
                                                "id": 26,
                                                "uniqueId": "d266c5f8-d287-4089-afed-e14bd68c25c5",
                                                "collectionId": "9dc865cc-e861-475b-a306-648448382f29",
                                                "title": "RPC modulith backend application and libraries",
                                                "content": "\\n\\n- API application (api-app)\\n-
                                                API library (api-lib)\\n    - API Server BFFs.\\n
                                                - API Server Services.\\n",
                                                "htmlContent": "<ul>\\n<li>API application
                                                (api-app)</li>\\n<li>API library (api-lib)\\n<ul>
                                                \\n<li>API Server BFFs.</li>\\n<li>API Server
                                                Services.</li>\\n</ul>\\n</li>\\n</ul>\\n",
                                                "version": 1,
                                                "status": "published",
                                                "publishedOn": "2024-04-16T13:06:28.435893Z",
                                                "children": [],
                                                "createdOn": "2024-04-16T13:06:28.438479Z",
                                                "modifiedOn": "2024-04-16T13:06:28.438483Z"
                                            }
                                        ]
                                    }]}
                        """))),
        @ApiResponse(responseCode = "200", description = "Retrieve List of Collection with search-by unknown name",
                content = @Content(mediaType = "application/json", schema = @Schema(example = """
                        {
                            "content": [
                            ]
                        }
                        """))),
        @ApiResponse(responseCode = "200", description = "Retrieve List of Collection with custom sorting",
                content = @Content(mediaType = "application/json", schema = @Schema(example = """
                        {
                               "paged": false,
                               "data": [
                                   {
                                       "id": 15,
                                       "uniqueId": "fb68eaab-d81f-4d26-8fb0-7547c4f0a90d",
                                       "name": "Test1",
                                       "description": "Test",
                                       "documentPermission": "read_write",
                                       "createdOn": "2024-04-17T04:52:04.937171Z",
                                       "modifiedOn": "2024-04-17T04:55:13.638495Z",
                                       "documentList": []
                                   }]}
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
    public CollectionViewDto retrieveCollections(
            @PageableDefault(size = 12, page = 0, sort = "modifiedOn",
                    direction = Sort.Direction.DESC) Pageable pageable,
            @RequestParam(required = false) MultiValueMap<String, String> searchParameters) {
        log.info("Entering retrieveCollections()");
        CollectionViewDto collectionViewDto = markdownCollectionService.retrieveCollections(pageable, searchParameters);
        log.info("Leaving retrieveCollections()");
        return collectionViewDto;
    }

    @PostMapping("/{collection-id}/documents/upload")
    @Operation(summary = "Upload a Document", description = "Upload a new Document.")
    @Parameter(description = "Collection unique identifier", example = "c38b2827-d3d4-4fc1-b508-90b7f96c58c9",
            required = true, in = ParameterIn.PATH)
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "204", description = "Document - Uploaded successfully",
                        content = @Content(mediaType = "application/json",
                                schema = @Schema(implementation = Void.class))),
                @ApiResponse(responseCode = "400", description = "Bad Request - Invalid Document details",
                        content = @Content(mediaType = "application/json", schema = @Schema(example = """
                                {
                                    "statusCode": 400,
                                    "message": "BAD_REQUEST",
                                    "details": [
                                        "Uploader id is missing or blank",
                                        "Collection id is missing or blank"
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
                @ApiResponse(responseCode = "404", description = "Not Found - Collection id Missing",
                        content = @Content(mediaType = "application/json", schema = @Schema(example = """
                                {
                                    "statusCode": 404,
                                    "message": "NOT_FOUND",
                                    "details": [
                                        "Collection with unique-id 'c38b2827-d3d4-4fc1-b508-90b7f96c58c1' not found"
                                    ]
                                }
                                    """))) })
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void uploadDocument(@PathVariable(name = "collection-id", required = true) UUID collectionId,
            @RequestParam(name = "parent-document-id", required = false, defaultValue = "") UUID parentDocumentId,
            @RequestPart("file") MultipartFile file) {
        log.info("Entering uploadDocument()");
        markdownCollectionService.uploadDocument(collectionId, parentDocumentId, file);
        log.info("Leaving uploadDocument()");
    }

}
