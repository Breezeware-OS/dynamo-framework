package net.breezeware.dynamo.usermanagent.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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

import net.breezeware.dynamo.usermanagement.dto.SignedUpUserData;
import net.breezeware.dynamo.usermanagement.dto.UserAccountSetupRequest;
import net.breezeware.dynamo.usermanagement.dto.UserInviteRequest;
import net.breezeware.dynamo.usermanagement.dto.UserUpdateRequest;
import net.breezeware.dynamo.usermanagement.dto.UserViewResponse;
import net.breezeware.dynamo.usermanagement.entity.User;
import net.breezeware.dynamo.usermanagent.service.UserManagementService;
import net.breezeware.dynamo.utils.exception.DynamoException;
import net.breezeware.dynamo.utils.exception.ErrorResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.enums.ParameterStyle;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Controller class for managing user accounts and user-related functionality in
 * the system.
 */
@Slf4j
@RestController
@RequestMapping("/api/service/user-management/users")
@Tag(name = "User Management")
@RequiredArgsConstructor
public class UserManagementController {

    private final UserManagementService userManagementService;

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/invite")
    @Operation(summary = "Invite user from admin.",
            description = """
                    Sends an invitation to the specified email to join the platform with the
                      given groups and roles..\
                    """)
    @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = UserInviteRequest.class)))
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Created", content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "401", description = "Unauthorized request",
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                        schema = @Schema(implementation = ErrorResponse.class, example = """
                                {
                                    "statusCode": 401,
                                    "message": "UNAUTHORIZED",
                                    "details": [
                                        "Full authentication is required to access this resource"
                                    ]
                                }"""))),
        @ApiResponse(responseCode = "400", description = "Bad request",
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                        schema = @Schema(implementation = ErrorResponse.class, example = """
                                {
                                    "statusCode": 400,
                                    "message": "BAD_REQUEST",
                                    "details": [
                                        "User's email is missing or blank",
                                        "Invited user id missing"
                                    ]
                                }
                                """))),
        @ApiResponse(responseCode = "405", description = "Method Not Allowed",
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                        schema = @Schema(implementation = ErrorResponse.class, example = """
                                            {
                                                "statusCode": 405,
                                                "message": "METHOD_NOT_ALLOWED",
                                                "details": [
                                                    "Request method 'PATCH' not supported"
                                                ]
                                            }
                                """))),
        @ApiResponse(responseCode = "404", description = "Not Found",
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                        schema = @Schema(implementation = ErrorResponse.class, example = """
                                        {
                                                "statusCode": 404,
                                                "message": "NOT_FOUND",
                                                "details": [
                                                    "User with email 'john@examble.com' not found"
                                                ]
                                            }
                                """))),
        @ApiResponse(responseCode = "403", description = "Access is denied",
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                        schema = @Schema(implementation = ErrorResponse.class, example = """
                                         {
                                                 "statusCode": 403,
                                                 "message": "FORBIDDEN",
                                                 "details": [
                                                     "Access is denied"
                                                 ]
                                             }
                                """))) })
    public void inviteUser(@RequestBody UserInviteRequest inviteUserRequest) throws DynamoException {
        log.info("Entering inviteUser()");
        userManagementService.inviteUser(inviteUserRequest);
        log.info("Leaving inviteUser()");

    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping("/account-setup")
    @Operation(summary = "User account setup.",
            description = "Sets up a user account with the provided  account setup details.")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = UserAccountSetupRequest.class)))
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "No Content",
                content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "401", description = "Unauthorized request",
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                        schema = @Schema(implementation = ErrorResponse.class, example = """
                                {
                                    "statusCode": 401,
                                    "message": "UNAUTHORIZED",
                                    "details": [
                                        "Full authentication is required to access this resource"
                                    ]
                                }"""))),
        @ApiResponse(responseCode = "400", description = "Bad request",
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                        schema = @Schema(implementation = ErrorResponse.class, example = """
                                {
                                    "statusCode": 400,
                                    "message": "BAD_REQUEST",
                                    "details": [
                                        "User's email is missing or blank",
                                        User's first name is missing or blank,
                                        User's last name is missing or blank,
                                        User's password is missing or blank
                                    ]
                                }
                                """))),
        @ApiResponse(responseCode = "405", description = "Method Not Allowed",
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                        schema = @Schema(implementation = ErrorResponse.class, example = """
                                            {
                                                "statusCode": 405,
                                                "message": "METHOD_NOT_ALLOWED",
                                                "details": [
                                                    "Request method 'PATCH' not supported"
                                                ]
                                            }
                                """))),
        @ApiResponse(responseCode = "404", description = "Not Found",
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                        schema = @Schema(implementation = ErrorResponse.class, example = """
                                        {
                                                "statusCode": 404,
                                                "message": "NOT_FOUND",
                                                "details": [
                                                    "User with email 'john@examble.com' not found"
                                                ]
                                            }
                                """))) })
    public void setupUserAccount(@RequestBody UserAccountSetupRequest userAccountSetupRequest) throws DynamoException {
        log.info("Entering setupUserAccount()");
        userManagementService.setupUserAccount(userAccountSetupRequest);
        log.info("Leaving setupUserAccount()");
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PutMapping
    @Operation(summary = "Admin has updated the user's details.",
            description = "Updates the user details for the given user identifier.")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = UserUpdateRequest.class)))
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "No Content",
                content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "401", description = "Unauthorized request",
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                        schema = @Schema(implementation = ErrorResponse.class, example = """
                                {
                                    "statusCode": 401,
                                    "message": "UNAUTHORIZED",
                                    "details": [
                                        "Full authentication is required to access this resource"
                                    ]
                                }"""))),
        @ApiResponse(responseCode = "400", description = "Bad request",
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                        schema = @Schema(implementation = ErrorResponse.class, example = """
                                {
                                    "statusCode": 400,
                                    "message": "BAD_REQUEST",
                                    "details": [
                                        "User's email is missing or blank",
                                        User's first name is missing or blank,
                                        User's last name is missing or blank,
                                        User's phone number is missing or blank
                                    ]
                                }
                                """))),
        @ApiResponse(responseCode = "405", description = "Method Not Allowed",
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                        schema = @Schema(implementation = ErrorResponse.class, example = """
                                            {
                                                "statusCode": 405,
                                                "message": "METHOD_NOT_ALLOWED",
                                                "details": [
                                                    "Request method 'PATCH' not supported"
                                                ]
                                            }
                                """))),
        @ApiResponse(responseCode = "404", description = "Not Found",
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                        schema = @Schema(implementation = ErrorResponse.class, example = """
                                    {
                                "statusCode": 404,
                                "message": "NOT_FOUND",
                                "details": [
                                    "User with unique-id '29e28ddf-86bd-4954-bc64-a1ed2a67f461' not found"
                                            ]
                                        }
                                                        """))),
        @ApiResponse(responseCode = "403", description = "Access is denied",
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                        schema = @Schema(implementation = ErrorResponse.class, example = """
                                         {
                                                 "statusCode": 403,
                                                 "message": "FORBIDDEN",
                                                 "details": [
                                                     "Access is denied"
                                                 ]
                                             }
                                """))) })
    public void updateUser(@RequestBody UserUpdateRequest updateUserRequest) throws DynamoException {
        log.info("Entering updateUser()");
        userManagementService.updateUser(updateUserRequest);
        log.info("Leaving updateUser()");
    }

    @GetMapping
    @Operation(summary = "Retrieves users.",
            description = """
                    Retrieves a paginated list of users based on the given page number, page
                     size, and search parameters.\
                    """)
    @Parameters(value = {
        @Parameter(allowEmptyValue = true, name = "page-no", example = "0", description = "Page number",
                in = ParameterIn.QUERY),
        @Parameter(allowEmptyValue = true, name = "page-size", example = "8", description = "Page size",
                in = ParameterIn.QUERY),
        @Parameter(allowEmptyValue = true, name = "sort", example = "name,ASC",
                description = "Sort by field with sort order", in = ParameterIn.QUERY),
        @Parameter(allowEmptyValue = true, name = "role", example = "admin", description = "Filter by user role id",
                in = ParameterIn.QUERY),
        @Parameter(allowEmptyValue = true, name = "status", example = "active", description = "Filter by user status",
                in = ParameterIn.QUERY),
        @Parameter(allowEmptyValue = true, name = "created-date", example = "2023-09-25T11:35:06.625619Z",
                description = "Filter by user created date", in = ParameterIn.QUERY),
        @Parameter(allowEmptyValue = true, name = "username", example = "john doe", description = "user name search",
                in = ParameterIn.QUERY) })
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Success Payload",
                content = @Content(mediaType = "application/json", schema = @Schema(example = """
                        {
                            "content": [
                                {
                                    "userId": "0e4f5855-e738-4b24-aa33-2098a0ace529",
                                    "email": "surya@breezeware.net",
                                    "firstName": "Surya",
                                    "lastName": "Kumar",
                                    "phoneNumber": "+919876543210",
                                    "roles": [
                                        "admin"
                                    ],
                                    "groups": [
                                        "development"
                                    ],
                                    "status": "Active",
                                    "createdOn": "2023-09-15T11:28:33.745525Z"
                                },
                                {
                                    "userId": "d3a67b2e-01af-4dbb-81f7-78778c258993",
                                    "email": "sathish@breezeware.net",
                                    "firstName": "Sathish",
                                    "lastName": "Doe",
                                    "phoneNumber": "+11234567890",
                                    "roles": [
                                        "user"
                                    ],
                                    "groups": [
                                        "development"
                                    ],
                                    "status": "Active",
                                    "createdOn": "2023-09-20T05:39:17.413081Z"
                                },
                                {
                                    "userId": "4346d4e2-0ef8-4c00-8e9a-6f81f4304f49",
                                    "email": "manoj@breezeware.net",
                                    "firstName": "Manoj",
                                    "lastName": "Kumar",
                                    "phoneNumber": "+11234567890",
                                    "roles": [
                                        "user"
                                    ],
                                    "groups": [
                                        "administrative"
                                    ],
                                    "status": "Active",
                                    "createdOn": "2023-09-20T09:35:44.862637Z"
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
                                "pageSize": 16,
                                "paged": true,
                                "unpaged": false
                            },
                            "totalElements": 3,
                            "totalPages": 1,
                            "last": true,
                            "size": 16,
                            "number": 0,
                            "sort": {
                                "empty": false,
                                "sorted": true,
                                "unsorted": false
                            },
                            "numberOfElements": 3,
                            "first": true,
                            "empty": false
                        }"""))),
        @ApiResponse(responseCode = "401", description = "Unauthorized request",
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                        schema = @Schema(implementation = ErrorResponse.class, example = """
                                {
                                    "statusCode": 401,
                                    "message": "UNAUTHORIZED",
                                    "details": [
                                        "Full authentication is required to access this resource"
                                    ]
                                }"""))),
        @ApiResponse(responseCode = "400", description = "Bad request",
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                        schema = @Schema(implementation = ErrorResponse.class, example = """
                                {
                                    "statusCode": 400,
                                    "message": "BAD_REQUEST",
                                    "details": [
                                        "Unknown parameter(s) [first-name, last-name] found"
                                    ]
                                }
                                """))),
        @ApiResponse(responseCode = "400", description = "Bad request",
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                        schema = @Schema(implementation = ErrorResponse.class, example = """
                                {
                                     "statusCode": 400,
                                     "message": "BAD_REQUEST",
                                     "details": [
                                         "Invalid sort criteria 'email'. Should be something like
                                         'name,ASC' or 'name,asc'"
                                     ]
                                 }
                                """))),
        @ApiResponse(responseCode = "405", description = "Method Not Allowed",
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                        schema = @Schema(implementation = ErrorResponse.class, example = """
                                            {
                                                "statusCode": 405,
                                                "message": "METHOD_NOT_ALLOWED",
                                                "details": [
                                                    "Request method 'PATCH' not supported"
                                                ]
                                            }
                                """))),
        @ApiResponse(responseCode = "404", description = "Not Found", content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class, example = """
                                {
                                        "statusCode": 404,
                                        "message": "NOT_FOUND",
                                        "details": [
                                            "User with unique-id '29e28ddf-86bd-4954-bc64-a1ed2a67f461' not found"
                                        ]
                                    }
                        """))) })
    public Page<UserViewResponse> retrieveUsers(
            @Parameter(hidden = true, in = ParameterIn.QUERY, style = ParameterStyle.FORM)
            @SortDefault(sort = "email", direction = Sort.Direction.ASC) Pageable pageable,
            @RequestParam(required = false) MultiValueMap<String, String> searchParameters) throws DynamoException {
        log.info("Entering retrieveUsers(), pageable: {}, searchParameters: {}", pageable, searchParameters);
        Page<UserViewResponse> userPage = userManagementService.retrieveUsers(searchParameters, pageable);
        log.info("Leaving retrieveUsers(), # of elements: {} in page-no: {}", userPage.getNumberOfElements(),
                userPage.getPageable().getPageNumber());
        return userPage;
    }

    @GetMapping("/{user-id}")
    @Operation(summary = "Retrieve user details.", description = "Retrieves user for the given user identifier.")
    @Parameters(value = { @Parameter(allowEmptyValue = true, required = false, name = "user-id",
            example = "fdbfba40-daf2-4309-906a-7c0cc70dd0df", description = "Represents the user id.",
            in = ParameterIn.PATH) })
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Success Payload",
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                        schema = @Schema(implementation = ErrorResponse.class, example = """
                                {
                                    "userId": "d3a67b2e-01af-4dbb-81f7-78778c258993",
                                    "email": "john@examble.net",
                                    "firstName": "John",
                                    "lastName": "Doe",
                                    "phoneNumber": "+11234567890",
                                    "roles": [
                                        "user"
                                    ],
                                    "groups": [
                                        "development"
                                    ],
                                    "status": "active",
                                    "createdOn": "2023-09-20T05:39:17.413081Z"
                                }"""))),
        @ApiResponse(responseCode = "401", description = "Unauthorized request",
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                        schema = @Schema(implementation = ErrorResponse.class, example = """
                                {
                                    "statusCode": 401,
                                    "message": "UNAUTHORIZED",
                                    "details": [
                                        "Full authentication is required to access this resource"
                                    ]
                                }"""))),
        @ApiResponse(responseCode = "404", description = "Not Found",
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                        schema = @Schema(implementation = ErrorResponse.class, example = """
                                {
                                    "statusCode": 404,
                                    "message": "NOT_FOUND",
                                    "details": [
                                        "User with unique-id '29e28ddf-86bd-4954-bc64-a1ed2a67f461' not found"
                                    ]
                                }
                                    """))),
        @ApiResponse(responseCode = "405", description = "Method Not Allowed",
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                        schema = @Schema(implementation = ErrorResponse.class, example = """
                                            {
                                                "statusCode": 405,
                                                "message": "METHOD_NOT_ALLOWED",
                                                "details": [
                                                    "Request method 'PATCH' not supported"
                                                ]
                                            }
                                """))), })
    public UserViewResponse retrieveUser(@PathVariable(name = "user-id") UUID userId) throws DynamoException {
        log.info("Entering retrieveUser()");
        UserViewResponse userViewResponse = userManagementService.retrieveUser(userId);
        log.info("Leaving retrieveUser()");
        return userViewResponse;
    }

    @GetMapping(params = "idm-user-id")
    public UserViewResponse retrieveUser(@RequestParam("idm-user-id") String idmUserId) {
        log.info("Entering retrieveUser()");
        UserViewResponse userViewResponse = userManagementService.retrieveUser(idmUserId);
        log.info("Leaving retrieveUser()");
        return userViewResponse;
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PutMapping("/{user-id}/suspend")
    @Operation(summary = "Admin has suspend the user's account.",
            description = "Suspends the user account for the given user identifier.")
    @Parameters(value = { @Parameter(allowEmptyValue = true, required = false, name = "user-id",
            example = "fdbfba40-daf2-4309-906a-7c0cc70dd0df", description = "Represents the user id.",
            in = ParameterIn.PATH) })
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "No Content",
                content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "401", description = "Unauthorized request",
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                        schema = @Schema(implementation = ErrorResponse.class, example = """
                                {
                                    "statusCode": 401,
                                    "message": "UNAUTHORIZED",
                                    "details": [
                                        "Full authentication is required to access this resource"
                                    ]
                                }"""))),
        @ApiResponse(responseCode = "404", description = "Not Found",
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                        schema = @Schema(implementation = ErrorResponse.class, example = """
                                {
                                    "statusCode": 404,
                                    "message": "NOT_FOUND",
                                    "details": [
                                        "User with unique-id '29e28ddf-86bd-4954-bc64-a1ed2a67f461' not found"
                                    ]
                                }
                                    """))),
        @ApiResponse(responseCode = "405", description = "Method Not Allowed",
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                        schema = @Schema(implementation = ErrorResponse.class, example = """
                                            {
                                                "statusCode": 405,
                                                "message": "METHOD_NOT_ALLOWED",
                                                "details": [
                                                    "Request method 'PATCH' not supported"
                                                ]
                                            }
                                """))),
        @ApiResponse(responseCode = "403", description = "Access is denied",
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                        schema = @Schema(implementation = ErrorResponse.class, example = """
                                         {
                                                 "statusCode": 403,
                                                 "message": "FORBIDDEN",
                                                 "details": [
                                                     "Access is denied"
                                                 ]
                                             }
                                """))) })
    public void suspendUser(@PathVariable(name = "user-id") UUID userId) throws DynamoException {
        log.info("Entering suspendUser()");
        userManagementService.suspendUser(userId);
        log.info("Leaving suspendUser()");
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PutMapping("/{user-id}/activate")
    @Operation(summary = "Admin has activate the user's account.",
            description = "Activates the user account for the given user identifier.")
    @Parameters(value = { @Parameter(allowEmptyValue = true, required = false, name = "user-id",
            example = "fdbfba40-daf2-4309-906a-7c0cc70dd0df", description = "Represents the user id.",
            in = ParameterIn.PATH) })
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "No Content",
                content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "401", description = "Unauthorized request",
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                        schema = @Schema(implementation = ErrorResponse.class, example = """
                                {
                                    "statusCode": 401,
                                    "message": "UNAUTHORIZED",
                                    "details": [
                                        "Full authentication is required to access this resource"
                                    ]
                                }"""))),
        @ApiResponse(responseCode = "404", description = "Not Found",
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                        schema = @Schema(implementation = ErrorResponse.class, example = """
                                {
                                        "statusCode": 404,
                                        "message": "NOT_FOUND",
                                        "details": [
                                            "User with unique-id '29e28ddf-86bd-4954-bc64-a1ed2a67f461' not found"
                                        ]
                                    }
                                        """))),
        @ApiResponse(responseCode = "405", description = "Method Not Allowed",
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                        schema = @Schema(implementation = ErrorResponse.class, example = """
                                            {
                                                "statusCode": 405,
                                                "message": "METHOD_NOT_ALLOWED",
                                                "details": [
                                                    "Request method 'PATCH' not supported"
                                                ]
                                            }
                                """))),
        @ApiResponse(responseCode = "403", description = "Access is denied",
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                        schema = @Schema(implementation = ErrorResponse.class, example = """
                                         {
                                                 "statusCode": 403,
                                                 "message": "FORBIDDEN",
                                                 "details": [
                                                     "Access is denied"
                                                 ]
                                             }
                                """))) })
    public void activateUser(@PathVariable(name = "user-id") UUID userId) throws DynamoException {
        log.info("Entering activateUser()");
        userManagementService.activateUser(userId);
        log.info("Leaving activateUser()");
    }

    @GetMapping("/by-groups")
    @Operation(summary = "Retrieve User's.", description = "Retrieve User's by groups.")
    @Parameters(value = { @Parameter(allowEmptyValue = true, required = false, name = "groups",
            example = "group1,group2", description = "Represents the user groups.", in = ParameterIn.QUERY) })
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Success Payload",
                content = @Content(mediaType = "application/json",
                        array = @ArraySchema(schema = @Schema(implementation = User.class)))),
        @ApiResponse(responseCode = "401", description = "Unauthorized request",
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                        schema = @Schema(implementation = ErrorResponse.class, example = """
                                {
                                    "statusCode": 401,
                                    "message": "UNAUTHORIZED",
                                    "details": [
                                        "Full authentication is required to access this resource"
                                    ]
                                }"""))),
        @ApiResponse(responseCode = "404", description = "Not Found",
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                        schema = @Schema(implementation = ErrorResponse.class, example = """
                                {
                                    "statusCode": 404,
                                    "message": "NOT_FOUND",
                                    "details": [
                                        "Group with name 'group' not found"
                                    ]
                                }
                                    """))),
        @ApiResponse(responseCode = "405", description = "Method Not Allowed",
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                        schema = @Schema(implementation = ErrorResponse.class, example = """
                                            {
                                                "statusCode": 405,
                                                "message": "METHOD_NOT_ALLOWED",
                                                "details": [
                                                    "Request method 'PATCH' not supported"
                                                ]
                                            }
                                """))),
        @ApiResponse(responseCode = "403", description = "Access is denied",
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                        schema = @Schema(implementation = ErrorResponse.class, example = """
                                         {
                                                 "statusCode": 403,
                                                 "message": "FORBIDDEN",
                                                 "details": [
                                                     "Access is denied"
                                                 ]
                                             }
                                """))) })
    public List<User> retrieveUsersByGroup(@RequestParam(name = "groups") List<String> groups) {
        log.info("Entering retrieveUsersByGroup()");
        List<User> users = userManagementService.retrieveUsersByGroup(groups);
        log.info("Leaving retrieveUsersByGroup()");
        return users;
    }

    @GetMapping("/by-roles")
    @Operation(summary = "Retrieve User's.", description = "Retrieve User's by roles.")
    @Parameters(value = { @Parameter(allowEmptyValue = true, required = false, name = "roles", example = "role1,role2",
            description = "Represents the user roles.", in = ParameterIn.QUERY) })
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Success Payload",
                content = @Content(mediaType = "application/json",
                        array = @ArraySchema(schema = @Schema(implementation = User.class)))),
        @ApiResponse(responseCode = "401", description = "Unauthorized request",
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                        schema = @Schema(implementation = ErrorResponse.class, example = """
                                {
                                    "statusCode": 401,
                                    "message": "UNAUTHORIZED",
                                    "details": [
                                        "Full authentication is required to access this resource"
                                    ]
                                }"""))),
        @ApiResponse(responseCode = "404", description = "Not Found",
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                        schema = @Schema(implementation = ErrorResponse.class, example = """
                                {
                                    "statusCode": 404,
                                    "message": "NOT_FOUND",
                                    "details": [
                                        "Role with name 'role' not found"
                                    ]
                                }
                                    """))),
        @ApiResponse(responseCode = "405", description = "Method Not Allowed",
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                        schema = @Schema(implementation = ErrorResponse.class, example = """
                                            {
                                                "statusCode": 405,
                                                "message": "METHOD_NOT_ALLOWED",
                                                "details": [
                                                    "Request method 'PATCH' not supported"
                                                ]
                                            }
                                """))),
        @ApiResponse(responseCode = "403", description = "Access is denied",
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                        schema = @Schema(implementation = ErrorResponse.class, example = """
                                         {
                                                 "statusCode": 403,
                                                 "message": "FORBIDDEN",
                                                 "details": [
                                                     "Access is denied"
                                                 ]
                                             }
                                """))) })
    public List<User> retrieveUsersByRole(@RequestParam(name = "roles") List<String> roles) {
        log.info("Entering retrieveUsersByRole()");
        List<User> users = userManagementService.retrieveUsersByRole(roles);
        log.info("Leaving retrieveUsersByRole()");
        return users;
    }

    @PostMapping
    @Operation(summary = "Create a User after successful sign up from the IDM",
            description = "Create a User after successful sign up from the IDM")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Success",
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                        schema = @Schema(implementation = SignedUpUserData.class, example = """
                                {
                                    "firstName": "John",
                                    "middleName": "James",
                                    "lastName": "Doe",
                                    "email": "john.doe@example.com",
                                    "isEmailVerified": true,
                                    "phoneNumber": "+1234567890",
                                    "organization": "Acme",
                                    "roles": ["user"],
                                    "groups": ["user"],
                                    "idmUserStatus": "CONFIRMED",
                                    "idmUniqueUserId": "ac702593-7a9a-46c9-bdca-551554294604",
                                    "idmId": "us-east-1_abcde",
                                    "idmName": "Cognito"
                                }
                                """))),
        @ApiResponse(responseCode = "401", description = "Bad request",
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                        schema = @Schema(implementation = ErrorResponse.class, example = """
                                 {
                                    "statusCode": 401,
                                    "message": "UNAUTHORIZED",
                                    "details": [
                                        "Full authentication is required to access this resource"
                                    ]
                                }"""))),
        @ApiResponse(responseCode = "401", description = "Unauthorized request",
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                        schema = @Schema(implementation = ErrorResponse.class, example = """
                                {
                                    "statusCode": 404,
                                    "message": "BAD_REQUEST",
                                    "details": [
                                        "No IdmInfo found for IDM id: us-east-1_abcde",
                                        "No organization with name: Acme found",
                                        "No role with name: user found",
                                        "No group with name: user found"
                                    ]
                                }"""))) })
    public User createUser(@RequestBody SignedUpUserData signedUpUserData) {
        log.info("Entering createUser()");
        User user = userManagementService.createUser(signedUpUserData);
        log.info("Leaving createUser()");
        return user;
    }

}
