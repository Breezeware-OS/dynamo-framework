package net.breezeware.dynamo.usermanagent.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import net.breezeware.dynamo.usermanagement.dto.SignedUpUserData;
import net.breezeware.dynamo.usermanagement.dto.UserAccountSetupRequest;
import net.breezeware.dynamo.usermanagement.dto.UserInviteRequest;
import net.breezeware.dynamo.usermanagement.dto.UserUpdateRequest;
import net.breezeware.dynamo.usermanagement.dto.UserViewResponse;
import net.breezeware.dynamo.usermanagement.entity.User;
import net.breezeware.dynamo.usermanagement.enumeration.UserStatus;
import net.breezeware.dynamo.usermanagent.service.UserManagementService;
import net.breezeware.dynamo.utils.exception.DynamoException;
import net.breezeware.dynamo.utils.exception.DynamoExceptionHandler;
import net.breezeware.dynamo.utils.exception.ErrorResponse;

import lombok.extern.slf4j.Slf4j;

@ExtendWith(MockitoExtension.class)
@DisplayName("Manage User Management REST API Controller unit test")
@Slf4j
public class UserManagementControllerTest {

    private ObjectMapper objectMapper;
    private MockMvc mockMvc;

    @Mock
    private UserManagementService userManagementService;

    @BeforeEach
    void setUp() {
        log.info("Entering setup()");
        mockMvc = MockMvcBuilders.standaloneSetup(new UserManagementController(userManagementService))
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .setControllerAdvice(new DynamoExceptionHandler()).build();
        log.info("""
                Configured a standalone MockMvc setup for 'UserManagementController'\
                 with 'PageableHandlerMethodArgumentResolver' and 'DynamoExceptionHandler' controller advice\
                """);
        objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        log.info("Configured Jackson ObjectMapper with JavaTimeModule for Java8 time API support");
        log.info("Leaving setup()");
    }

    @Test
    @DisplayName("Given valid userInviteRequest to POST '/api/service/user-management/users/invite', then invite and return the success response")
    void givenValidUserInviteRequest_whenInviteUser_thenReturnSuccessResponse() throws Exception {
        log.info("Entering givenValidUserInviteRequest_whenInviteUser_thenReturnSuccessResponse()");

        // Given
        UserInviteRequest userInviteRequest = UserInviteRequest.builder().email("john@examble.com")
                .invitedBy(UUID.fromString("b9ca36a5-92cc-4b37-8d6c-8e5567d8726e")).roles(List.of("admin"))
                .groups(List.of("developer")).build();

        // Mock
        doNothing().when(userManagementService).inviteUser(userInviteRequest);

        // Then
        MockHttpServletRequestBuilder requestBuilder = post("/api/service/user-management/users/invite")
                .contentType(MediaType.APPLICATION_JSON).characterEncoding(StandardCharsets.UTF_8)
                .content(objectMapper.writeValueAsString(userInviteRequest)).accept(MediaType.APPLICATION_JSON);
        mockMvc.perform(requestBuilder).andExpect(status().isCreated());

        // Verify
        verify(userManagementService).inviteUser(eq(userInviteRequest));

        log.info("Leaving givenValidUserInviteRequest_whenInviteUser_thenReturnSuccessResponse()");
    }

    @Test
    @DisplayName("Given invalid userInviteRequest with missing fields to POST '/api/service/user-management/users/invite', then return error response")
    void givenInvalidUserInviteRequestWithMissingFields_whenInviteUser_thenReturnErrorResponse() throws Exception {
        log.info("Entering givenInvalidUserInviteRequestWithMissingFields_whenInviteUser_thenReturnErrorResponse()");

        // Given
        UserInviteRequest userInviteRequest = UserInviteRequest.builder()
                // .email("john@examble.com")
                // .invitedBy(UUID.fromString("b9ca36a5-92cc-4b37-8d6c-8e5567d8726e"))
                .roles(List.of("admin")).groups(List.of("developer")).build();

        // Mock error response
        ErrorResponse mockErrorResponse = new ErrorResponse();
        mockErrorResponse.setStatusCode(400);
        mockErrorResponse.setMessage(HttpStatus.BAD_REQUEST.name());
        mockErrorResponse.setDetails(List.of("User's email is missing or blank", "Invited user id missing"));

        // Mock service behavior
        doThrow(new DynamoException(List.of("User's email is missing or blank", "Invited user id missing"),
                HttpStatus.BAD_REQUEST)).when(userManagementService).inviteUser(eq(userInviteRequest));

        // When
        MockHttpServletRequestBuilder requestBuilder = post("/api/service/user-management/users/invite")
                .contentType(MediaType.APPLICATION_JSON).characterEncoding(StandardCharsets.UTF_8)
                .content(objectMapper.writeValueAsString(userInviteRequest)).accept(MediaType.APPLICATION_JSON);
        mockMvc.perform(requestBuilder).andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(objectMapper.writeValueAsString(mockErrorResponse)))
                .andExpectAll(jsonPath("$.statusCode").value(400), // status code
                        jsonPath("$.message").value("BAD_REQUEST"), // status message
                        jsonPath("$.details").isArray(), // detailed errors is an array
                        jsonPath("$.details").isNotEmpty() // detailed errors is not empty
                );

        // Verify
        verify(userManagementService).inviteUser(eq(userInviteRequest));

        log.info("Leaving givenInvalidUserInviteRequestWithMissingFields_whenInviteUser_thenReturnErrorResponse()");
    }

    @Test
    @DisplayName("Given valid userAccountSetupRequest with missing fields to POST '/api/service/user-management/users/account-setup', then return success response")
    public void givenValidUserAccountSetupRequest_whenAccountSetupUser_thenReturnsOk() throws Exception {
        log.info("Entering givenValidUserAccountSetupRequest_whenAccountSetupUser_thenReturnsOk()");

        // Given
        UserAccountSetupRequest userAccountSetupRequest = UserAccountSetupRequest.builder().email("test@example.com")
                .password("securePassword").firstName("John").lastName("Doe").phoneNumber("1234567890").build();

        // Mock
        doNothing().when(userManagementService).setupUserAccount(any(UserAccountSetupRequest.class));

        // When/Then
        mockMvc.perform(post("/api/service/user-management/users/account-setup").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userAccountSetupRequest))).andExpect(status().isNoContent());

        log.info("Leaving givenValidUserAccountSetupRequest_whenAccountSetupUser_thenReturnsOk()");
    }

    @Test
    @DisplayName("Given invalid userAccountSetupRequest with missing fields to POST '/api/service/user-management/users/account-setup', then return error response")
    public void givenInvalidUserAccountSetupRequest_whenAccountSetupUser_thenReturnsBadRequest() throws Exception {
        log.info("Entering givenInvalidUserAccountSetupRequest_whenAccountSetupUser_thenReturnsBadRequest()");

        // Given
        UserAccountSetupRequest userAccountSetupRequest = UserAccountSetupRequest.builder()
                // .email("test@example.com")
                .password("securePassword").firstName("John").lastName("Doe").phoneNumber("1234567890").build();

        // Mock error response
        ErrorResponse mockErrorResponse = new ErrorResponse();
        mockErrorResponse.setStatusCode(400);
        mockErrorResponse.setMessage(HttpStatus.BAD_REQUEST.name());
        mockErrorResponse.setDetails(List.of("User's email is missing or blank"));

        // Mock service behavior
        doThrow(new DynamoException(List.of("User's email is missing or blank"), HttpStatus.BAD_REQUEST))
                .when(userManagementService).setupUserAccount(eq(userAccountSetupRequest));

        // When/Then
        MockHttpServletRequestBuilder requestBuilder = post("/api/service/user-management/users/account-setup")
                .contentType(MediaType.APPLICATION_JSON).characterEncoding(StandardCharsets.UTF_8)
                .content(objectMapper.writeValueAsString(userAccountSetupRequest)).accept(MediaType.APPLICATION_JSON);
        mockMvc.perform(requestBuilder).andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(objectMapper.writeValueAsString(mockErrorResponse)))
                .andExpectAll(jsonPath("$.statusCode").value(400), // status code
                        jsonPath("$.message").value("BAD_REQUEST"), // status message
                        jsonPath("$.details").isArray(), // detailed errors is an array
                        jsonPath("$.details").isNotEmpty() // detailed errors is not empty
                );

        log.info("Leaving givenInvalidUserAccountSetupRequest_whenAccountSetupUser_thenReturnsBadRequest()");
    }

    @Test
    @DisplayName("Given invalid user with not found to POST '/api/service/user-management/users/account-setup', then return user not found error response")
    public void givenUserNotFound_whenAccountSetupUser_thenReturnsNotFound() throws Exception {
        log.info("Entering givenUserNotFound_whenAccountSetupUser_thenReturnsNotFound()");

        // Given
        UserAccountSetupRequest userAccountSetupRequest = UserAccountSetupRequest.builder()
                // .email("test@example.com")
                .password("securePassword").firstName("John").lastName("Doe").phoneNumber("1234567890").build();

        // Mock service behavior
        doThrow(new DynamoException(List.of("User's email is missing or blank"), HttpStatus.NOT_FOUND))
                .when(userManagementService).setupUserAccount(eq(userAccountSetupRequest));

        // When/Then
        mockMvc.perform(post("/api/service/user-management/users/account-setup").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userAccountSetupRequest))).andExpect(status().isNotFound());

        log.info("Leaving givenUserNotFound_whenAccountSetupUser_thenReturnsNotFound()");
    }

    @Test
    @DisplayName("Given valid updateUserRequest to POST '/api/service/user-management/users', then return success response")
    public void givenValidUserUpdateRequest_whenUpdatingUser_thenExpectSuccess() throws Exception {
        log.info("Entering givenValidUserUpdateRequest_whenUpdatingUser_thenExpectSuccess()");

        // Given
        UserUpdateRequest updateUserRequest = UserUpdateRequest.builder().userId(UUID.randomUUID()).idmUserId("user-id")
                .email("johndoe@gmail.com").firstName("John").lastName("Doe").phoneNumber("testPhoneNumber")
                .roles(List.of("user")).groups(List.of("developer")).build();

        doNothing().when(userManagementService).updateUser(updateUserRequest);

        // When and Then
        MockHttpServletRequestBuilder requestBuilder = put("/api/service/user-management/users")
                .contentType(MediaType.APPLICATION_JSON).characterEncoding(StandardCharsets.UTF_8)
                .content(objectMapper.writeValueAsString(updateUserRequest)).accept(MediaType.APPLICATION_JSON);
        mockMvc.perform(requestBuilder).andExpect(status().isNoContent());

        // Verify that userManagementService.updateUser was called
        verify(userManagementService, times(1)).updateUser(updateUserRequest);

        log.info("Leaving givenValidUserUpdateRequest_whenUpdatingUser_thenExpectSuccess()");
    }

    @Test
    @DisplayName("Given inValid updateUserRequest to POST '/api/service/user-management/users', then return error response")
    public void givenBadRequestUserUpdateRequest_whenUpdatingUser_thenExpectBadRequest() throws Exception {
        log.info("Entering givenBadRequestUserUpdateRequest_whenUpdatingUser_thenExpectBadRequest()");

        // Given
        UserUpdateRequest updateUserRequest = UserUpdateRequest.builder().userId(UUID.randomUUID()).idmUserId("user-id")
                // .email("johndoe@gmail.com")
                .firstName("John").lastName("Doe").phoneNumber("testPhoneNumber").roles(List.of("user"))
                .groups(List.of("developer")).build();

        doThrow(new DynamoException("User's email is missing or blank", HttpStatus.BAD_REQUEST))
                .when(userManagementService).updateUser(updateUserRequest);

        // When and Then
        mockMvc.perform(put("/api/service/user-management/users").contentType("application/json")
                .content(new ObjectMapper().writeValueAsString(updateUserRequest))).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.statusCode").value(400));

        log.info("Leaving givenBadRequestUserUpdateRequest_whenUpdatingUser_thenExpectBadRequest()");
    }

    @Test
    @DisplayName("Given inValid updateUserRequest to PATCH '/api/service/user-management/users', then return error response")
    public void givenMethodNotAllowed_whenUpdatingUser_thenExpectMethodNotAllowed() throws Exception {
        log.info("Entering givenMethodNotAllowed_whenUpdatingUser_thenExpectMethodNotAllowed()");

        // Given
        UserUpdateRequest updateUserRequest = UserUpdateRequest.builder().userId(UUID.randomUUID()).idmUserId("user-id")
                .email("johndoe@gmail.com").firstName("John").lastName("Doe").phoneNumber("testPhoneNumber")
                .roles(List.of("user")).groups(List.of("developer")).build();

        // When and Then
        mockMvc.perform(patch("/api/service/user-management/users") // Using an unsupported HTTP method
                .contentType("application/json").content(new ObjectMapper().writeValueAsString(updateUserRequest)))
                .andExpect(status().isMethodNotAllowed()).andExpect(jsonPath("$.statusCode").value(405));

        log.info("Leaving givenMethodNotAllowed_whenUpdatingUser_thenExpectMethodNotAllowed()");
    }

    @Test
    @DisplayName("Given not found user updateUserRequest to POST '/api/service/user-management/users', then return error response")
    public void givenUserNotFound_whenUpdatingUser_thenExpectNotFound() throws Exception {
        log.info("Entering givenUserNotFound_whenUpdatingUser_thenExpectNotFound()");

        // Given
        UUID userId = UUID.fromString("29e28ddf-86bd-4954-bc64-a1ed2a67f461");
        UserUpdateRequest updateUserRequest = UserUpdateRequest.builder().userId(userId).idmUserId("user-id")
                .email("johndoe@gmail.com").firstName("John").lastName("Doe").phoneNumber("testPhoneNumber")
                .roles(List.of("user")).groups(List.of("developer")).build();

        doThrow(new DynamoException("User with unique-id '29e28ddf-86bd-4954-bc64-a1ed2a67f461' not found",
                HttpStatus.NOT_FOUND)).when(userManagementService).updateUser(updateUserRequest);

        // When and Then
        mockMvc.perform(put("/api/service/user-management/users").contentType("application/json")
                .content(new ObjectMapper().writeValueAsString(updateUserRequest))).andExpect(status().isNotFound())
                .andExpect(jsonPath("$.statusCode").value(404));

        log.info("Leaving givenUserNotFound_whenUpdatingUser_thenExpectNotFound()");
    }

    @Test
    @DisplayName("Given valid page number, size and search/filter predicate to GET '/api/service/user-management/users', then returns page of users")
    void givenValidPageNumberSizeAndSearchOrFilterPredicate_whenRetrieveUsersPage_thenReturnPageOfUsers()
            throws Exception {
        log.info(
                "Entering givenValidPageNumberSizeAndSearchOrFilterPredicate_whenRetrieveUsersPage_thenReturnPageOfUsers()");

        // Given
        String userName = "john";
        String statusSearchValue = "charging";
        MultiValueMap<String, String> searchParameters = new LinkedMultiValueMap<>();
        searchParameters.add("username", userName);
        searchParameters.add("status", statusSearchValue);

        // pagination and sorting
        Sort sort = Sort.by(Sort.Direction.ASC, "id");
        Pageable pageable = PageRequest.of(0, 1, sort);

        // Mock
        UserViewResponse mockUserViewResponse =
                UserViewResponse.builder().userId(UUID.fromString("a7d82416-3e79-4232-bb6b-0094319b07b5"))
                        .firstName("john").status(UserStatus.ACTIVE.getStatus()).email("john@examble.com")
                        .lastName("doe").createdOn(Instant.now()).build();
        List<UserViewResponse> mockUserViewResponseList = List.of(mockUserViewResponse);

        var offsetOrStart = ((int) pageable.getOffset());
        var end = Math.min((offsetOrStart + pageable.getPageSize()), mockUserViewResponseList.size());
        var slicedUserViewResponseList = mockUserViewResponseList.subList(offsetOrStart, end);
        PageImpl<UserViewResponse> mockPagedUserViewResponse =
                new PageImpl<>(slicedUserViewResponseList, pageable, mockUserViewResponseList.size());
        when(userManagementService.retrieveUsers(eq(searchParameters), any(Pageable.class)))
                .thenReturn(mockPagedUserViewResponse);

        // When
        MockHttpServletRequestBuilder requestBuilder =
                get("/api/service/user-management/users").params(searchParameters).accept(MediaType.APPLICATION_JSON);
        mockMvc.perform(requestBuilder).andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andExpectAll(jsonPath("$.numberOfElements").value(1), // total elements in
                        // current-page
                        jsonPath("$.totalElements").value(1), // total-elements in page
                        jsonPath("$.number").value(0), // current page-no
                        jsonPath("$.totalPages").value(1)) // total available pages
                .andExpect(content().json(objectMapper.writeValueAsString(mockPagedUserViewResponse)));

        // Then
        verify(userManagementService).retrieveUsers(eq(searchParameters), any(Pageable.class));

        log.info(
                "Leaving givenValidPageNumberSizeAndSearchOrFilterPredicate_whenRetrieveUsersPage_thenReturnPageOfUsers()");
    }

    @Test
    @DisplayName("Given valid user ID, when retrieveUser to GET '/api/service/user-management/users/{user-id}' endpoint is called, then return 200 OK")
    public void givenValidUserId_whenRetrieveUserEndpointCalled_thenReturnOk() throws Exception {
        log.info("Entering givenValidUserId_whenRetrieveUserEndpointCalled_thenReturnOk()");

        // Given
        UUID userId = UUID.randomUUID();
        UserViewResponse expectedResponse = UserViewResponse.builder().userId(userId).email("john@example.net")
                .firstName("John").lastName("Doe").phoneNumber("+11234567890").roles(Collections.singletonList("user"))
                .groups(Collections.singletonList("development")).build();

        // Mock
        when(userManagementService.retrieveUser(userId)).thenReturn(expectedResponse);

        // When
        mockMvc.perform(get("/api/service/user-management/users/{user-id}", userId)).andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.userId").value(userId.toString()))
                .andExpect(jsonPath("$.email").value(expectedResponse.getEmail()))
                .andExpect(jsonPath("$.firstName").value(expectedResponse.getFirstName()))
                .andExpect(jsonPath("$.lastName").value(expectedResponse.getLastName()))
                .andExpect(jsonPath("$.phoneNumber").value(expectedResponse.getPhoneNumber()))
                .andExpect(jsonPath("$.roles").isArray()).andExpect(jsonPath("$.roles[0]").value("user"))
                .andExpect(jsonPath("$.groups").isArray()).andExpect(jsonPath("$.groups[0]").value("development"));

        // Verify
        verify(userManagementService).retrieveUser(userId);

        log.info("Leaving givenValidUserId_whenRetrieveUserEndpointCalled_thenReturnOk()");
    }

    @Test
    @DisplayName("Given invalid user ID, when retrieveUser to GET '/api/service/user-management/users/{user-id}' endpoint is called, then return 404 Not Found")
    public void givenInvalidUserId_whenRetrieveUserEndpointCalled_thenReturnNotFound() throws Exception {
        log.info("Entering givenInvalidUserId_whenRetrieveUserEndpointCalled_thenReturnNotFound()");

        // Given
        UUID userId = UUID.randomUUID();

        // Mock
        when(userManagementService.retrieveUser(userId))
                .thenThrow(new DynamoException("User not found", HttpStatus.NOT_FOUND));

        // When
        mockMvc.perform(get("/api/service/user-management/users/{user-id}", userId)).andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.statusCode").value(HttpStatus.NOT_FOUND.value()))
                .andExpect(jsonPath("$.message").value("NOT_FOUND"))
                .andExpect(jsonPath("$.details[0]").value("User not found"));

        // Verify
        verify(userManagementService).retrieveUser(userId);

        log.info("Leaving givenInvalidUserId_whenRetrieveUserEndpointCalled_thenReturnNotFound()");
    }

    @Test
    @DisplayName("Given a user ID, when suspending a user to PUT '/api/service/user-management/users/{user-id}/suspend', then return HTTP OK status")
    public void givenUserId_whenSuspendUser_thenReturnOkStatus() throws Exception {
        log.info("Entering givenUserId_whenSuspendUser_thenReturnOkStatus()");

        // Given
        UUID userId = UUID.randomUUID();
        doNothing().when(userManagementService).suspendUser(userId);

        // When and Then
        mockMvc.perform(put("/api/service/user-management/users/{user-id}/suspend", userId))
                .andExpect(status().isNoContent());

        // Verify
        verify(userManagementService, times(1)).suspendUser(userId);

        log.info("Leaving givenUserId_whenSuspendUser_thenReturnOkStatus()");
    }

    @Test
    @DisplayName("Given an invalid user ID, when suspending a user to PUT '/api/service/user-management/users/{user-id}/suspend', then return HTTP Not Found status")
    public void givenInvalidUserId_whenSuspendUser_thenReturnNotFoundStatus() throws Exception {
        log.info("Entering givenInvalidUserId_whenSuspendUser_thenReturnNotFoundStatus()");

        // Given
        UUID invalidUserId = UUID.randomUUID();
        doThrow(new DynamoException("User not found", HttpStatus.NOT_FOUND)).when(userManagementService)
                .suspendUser(invalidUserId);

        // When and Then
        mockMvc.perform(put("/api/service/user-management/users/{user-id}/suspend", invalidUserId))
                .andExpect(status().isNotFound()).andExpect(jsonPath("$.statusCode").value(404));

        // Verify
        verify(userManagementService, times(1)).suspendUser(invalidUserId);

        log.info("Leaving givenInvalidUserId_whenSuspendUser_thenReturnNotFoundStatus()");
    }

    @Test
    @DisplayName("Given a user ID, when activating a user to PUT '/api/service/user-management/users/{user-id}/activate', then return HTTP OK status")
    public void givenUserId_whenActivateUser_thenReturnOkStatus() throws Exception {
        log.info("Entering givenUserId_whenActivateUser_thenReturnOkStatus()");

        // Given
        UUID userId = UUID.randomUUID();
        doNothing().when(userManagementService).activateUser(userId);

        // When and Then
        mockMvc.perform(put("/api/service/user-management/users/{user-id}/activate", userId))
                .andExpect(status().isNoContent());

        // Verify
        verify(userManagementService, times(1)).activateUser(userId);

        log.info("Leaving givenUserId_whenActivateUser_thenReturnOkStatus()");
    }

    @Test
    @DisplayName("Given an invalid user ID, when activating a user to PUT '/api/service/user-management/users/{user-id}/activate', then return HTTP Not Found status")
    public void givenInvalidUserId_whenActivateUser_thenReturnNotFoundStatus() throws Exception {
        log.info("Entering givenInvalidUserId_whenActivateUser_thenReturnNotFoundStatus()");

        // Given
        UUID invalidUserId = UUID.randomUUID();
        doThrow(new DynamoException("User not found", HttpStatus.NOT_FOUND)).when(userManagementService)
                .activateUser(invalidUserId);

        // When and Then
        mockMvc.perform(put("/api/service/user-management/users/{user-id}/activate", invalidUserId))
                .andExpect(status().isNotFound()).andExpect(jsonPath("$.statusCode").value(404));

        // Verify
        verify(userManagementService, times(1)).activateUser(invalidUserId);

        log.info("Leaving givenInvalidUserId_whenActivateUser_thenReturnNotFoundStatus()");
    }

    @Test
    @DisplayName("Given a valid groups when retrieveUsers to GET '/api/service/user-management/users/by-groups?groups?' ,then returnUsers")
    public void givenValidGroups_whenRetrieveUsers_thenReturnUsers() throws Exception {
        log.info("Entering givenValidGroups_whenRetrieveUsers_thenReturnUsers()");

        // Given
        List<String> groups = Arrays.asList("group1", "group2");
        List<User> expectedUsers = Arrays.asList(new User(), new User());
        when(userManagementService.retrieveUsersByGroup(groups)).thenReturn(expectedUsers);

        // When & Then
        mockMvc.perform(get("/api/service/user-management/users/by-groups?groups=group1,group2"))
                .andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)));

        log.info("Leaving givenValidGroups_whenRetrieveUsers_thenReturnUsers()");
    }

    @Test
    @DisplayName("Given a valid roles when retrieveUsers to GET '/api/service/user-management/users/by-roles?roles?' ,then returnUsers")
    public void givenValidRoles_whenRetrieveUsers_thenReturnUsers() throws Exception {
        log.info("Entering givenValidRoles_whenRetrieveUsers_thenReturnUsers()");

        // Given
        List<String> roles = Arrays.asList("role1", "role2");
        List<User> expectedUsers = Arrays.asList(new User(), new User());
        when(userManagementService.retrieveUsersByRole(roles)).thenReturn(expectedUsers);

        // When & Then
        mockMvc.perform(get("/api/service/user-management/users/by-roles?roles=role1,role2")).andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON)).andExpect(jsonPath("$", hasSize(2)));

        log.info("Leaving givenValidRoles_whenRetrieveUsers_thenReturnUsers()");
    }

    @Test
    @DisplayName("Given a valid SignedUpUserData to POST '/api/service/user-management/users' ,then return User")
    public void givenValidSignedUpUserData_whenCreateUser_thenReturnUser() throws Exception {
        log.info("Entering givenValidSignedUpUserData_whenCreateUser_thenReturnUser()");

        // Given
        SignedUpUserData signedUpUserData =
                SignedUpUserData.builder().firstName("John").lastName("Doe").email("john.doe@example.com")
                        .isEmailVerified(Boolean.TRUE).middleName("James").phoneNumber("+1234567890")
                        .organization("Acme").roles(List.of("user")).groups(List.of("users")).idmUserStatus("CONFIRMED")
                        .idmId("test-idm-id").idmName("Cognito").idmUniqueUserId(UUID.randomUUID().toString()).build();

        // Mock
        User mockerUser = User.builder().id(1L).firstName("John").lastName("Doe").email("john.doe@example.com")
                .phoneNumber("+1234567890").additionalInfo("").status("active").createdOn(Instant.now())
                .modifiedOn(Instant.now()).phoneNumber("+1234567890").build();

        when(userManagementService.createUser(eq(signedUpUserData))).thenReturn(mockerUser);

        // Then
        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.post("/api/service/user-management/users")
                .contentType(MediaType.APPLICATION_JSON).characterEncoding(StandardCharsets.UTF_8)
                .content(objectMapper.writeValueAsString(signedUpUserData)).accept(MediaType.APPLICATION_JSON);
        mockMvc.perform(requestBuilder).andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(objectMapper.writeValueAsString(mockerUser)))
                .andExpectAll(jsonPath("$.id").isNumber(), // Database sequence id
                        jsonPath("$.status").value("active"), // user status
                        jsonPath("$.createdOn").exists(), // createdOn exists
                        jsonPath("$.modifiedOn").exists() // modifiedOn exists
                );
        verify(userManagementService).createUser(eq(signedUpUserData));
        log.info("Leaving givenValidSignedUpUserData_whenCreateUser_thenReturnUser()");

    }

    @Test
    @DisplayName("Given an invalid SignedUpUserData to POST '/api/service/user-management/users' ,then return error")
    public void givenInvalidSignedUpUserData_whenCreateUser_thenReturnError() throws Exception {
        log.info("Entering givenInvalidSignedUpUserData_whenCreateUser_thenReturnError()");
        // Given
        SignedUpUserData signedUpUserData =
                SignedUpUserData.builder().idmId("unknown-idm-id").organization("unknown-organization")
                        .roles(List.of("unknown-role")).groups(List.of("unknown-group")).build();

        // Mock error response
        ErrorResponse mockErrorResponse = new ErrorResponse();
        mockErrorResponse.setStatusCode(400);
        mockErrorResponse.setMessage(HttpStatus.BAD_REQUEST.name());
        List<String> errorDetails = List.of("No IdmInfo found for IDM id: unknown-idm-id",
                "No organization with name: unknown-organization found", "No role with name: unknown-role found",
                "No group with name: unknown-group found");
        mockErrorResponse.setDetails(errorDetails);

        when(userManagementService.createUser(eq(signedUpUserData)))
                .thenThrow(new DynamoException(errorDetails, HttpStatus.BAD_REQUEST));
        // Then
        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.post("/api/service/user-management/users")
                .contentType(MediaType.APPLICATION_JSON).characterEncoding(StandardCharsets.UTF_8)
                .content(objectMapper.writeValueAsString(signedUpUserData)).accept(MediaType.APPLICATION_JSON);
        mockMvc.perform(requestBuilder).andExpect(content().json(objectMapper.writeValueAsString(mockErrorResponse)))
                .andExpect(status().isBadRequest());

        log.info("Leaving givenInvalidSignedUpUserData_whenCreateUser_thenReturnError()");
    }

}
