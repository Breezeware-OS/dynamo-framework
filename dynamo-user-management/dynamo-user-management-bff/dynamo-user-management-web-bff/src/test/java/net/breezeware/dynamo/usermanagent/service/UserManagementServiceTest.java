package net.breezeware.dynamo.usermanagent.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;

import net.breezeware.dynamo.usermanagement.cognito.service.CognitoService;
import net.breezeware.dynamo.usermanagement.dto.SignedUpUserData;
import net.breezeware.dynamo.usermanagement.dto.UserAccountSetupRequest;
import net.breezeware.dynamo.usermanagement.dto.UserInviteRequest;
import net.breezeware.dynamo.usermanagement.dto.UserUpdateRequest;
import net.breezeware.dynamo.usermanagement.dto.UserViewResponse;
import net.breezeware.dynamo.usermanagement.entity.Group;
import net.breezeware.dynamo.usermanagement.entity.IdmInfo;
import net.breezeware.dynamo.usermanagement.entity.Organization;
import net.breezeware.dynamo.usermanagement.entity.Role;
import net.breezeware.dynamo.usermanagement.entity.User;
import net.breezeware.dynamo.usermanagement.entity.UserGroupMap;
import net.breezeware.dynamo.usermanagement.entity.UserOrganizationMap;
import net.breezeware.dynamo.usermanagement.entity.UserRoleMap;
import net.breezeware.dynamo.usermanagement.enumeration.UserStatus;
import net.breezeware.dynamo.usermanagement.service.GroupService;
import net.breezeware.dynamo.usermanagement.service.IdmInfoService;
import net.breezeware.dynamo.usermanagement.service.OrganizationService;
import net.breezeware.dynamo.usermanagement.service.RoleService;
import net.breezeware.dynamo.usermanagement.service.UserGroupMapService;
import net.breezeware.dynamo.usermanagement.service.UserOrganizationMapService;
import net.breezeware.dynamo.usermanagement.service.UserRoleMapService;
import net.breezeware.dynamo.usermanagement.service.UserService;
import net.breezeware.dynamo.usermanagent.mapper.UserMapperService;
import net.breezeware.dynamo.utils.exception.DynamoException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@DisplayName("User Management Service implementation unit test")
@ExtendWith(MockitoExtension.class)
public class UserManagementServiceTest {

    @Mock
    IdmInfoService idmInfoService;

    @Mock
    UserService userService;

    @Mock
    RoleService roleService;

    @Mock
    GroupService groupService;

    @Mock
    UserRoleMapService userRoleMapService;

    @Mock
    UserGroupMapService userGroupMapService;

    @Mock
    UserOrganizationMapService userOrganizationMapService;

    @Mock
    OrganizationService organizationService;

    @Mock
    CognitoService cognitoService;

    @Mock
    UserMapperService userMapperService;

    @Mock
    Validator fieldValidator;

    @Mock
    ObjectMapper objectMapper;

    @InjectMocks
    UserManagementService userManagementService;

    @Test
    @DisplayName("Given valid UserInviteRequest, when inviteUser is called, then create and return a new user")
    void givenValidUserInviteRequest_whenInviteUser_thenCreateUser() throws DynamoException {
        log.info("Entering givenValidUserInviteRequest_whenInviteUser_thenCreateUser()");

        // Given
        UserInviteRequest userInviteRequest = UserInviteRequest.builder().email("john@example.com")
                .invitedBy(UUID.randomUUID()).groups(List.of("developer")).roles(List.of("admin")).build();

        // Mock
        Organization organization = Organization.builder().id(1L).name("google").build();

        IdmInfo idmInfo = IdmInfo.builder().id(1L).name("cognito").build();

        User invitedByUser = User.builder().uniqueId(userInviteRequest.getInvitedBy()).idmInfo(idmInfo).build();

        Group group = Group.builder().id(1L).name("developer").build();

        Role role = Role.builder().id(1L).name("admin").build();

        UserOrganizationMap userOrganizationMap =
                UserOrganizationMap.builder().organization(organization).user(invitedByUser).build();

        User savedUser = User.builder().uniqueId(UUID.randomUUID()).idmInfo(invitedByUser.getIdmInfo()).id(1L)
                .email(userInviteRequest.getEmail()).status(UserStatus.INVITED.getStatus()).createdOn(Instant.now())
                .modifiedOn(Instant.now()).build();
        savedUser.setUniqueId(UUID.randomUUID());

        when(userService.retrieveUser(userInviteRequest.getInvitedBy())).thenReturn(Optional.of(invitedByUser));
        when(userOrganizationMapService.retrieveUserOrganizationMap(invitedByUser))
                .thenReturn(Optional.of(userOrganizationMap));
        when(groupService.retrieveGroup(userInviteRequest.getGroups().get(0))).thenReturn(Optional.ofNullable(group));
        when(roleService.retrieveRole(userInviteRequest.getRoles().get(0))).thenReturn(Optional.ofNullable(role));
        when(idmInfoService.retrieveById(invitedByUser.getIdmInfo().getId())).thenReturn(Optional.of(idmInfo));
        when(cognitoService.inviteUser(userInviteRequest)).thenReturn("idmUserId");
        when(userService.create(any(User.class))).thenReturn(savedUser);

        // When
        userManagementService.inviteUser(userInviteRequest);

        // Then
        verify(fieldValidator).validate(userInviteRequest);
        verify(userService).retrieveUser(userInviteRequest.getInvitedBy());
        verify(userOrganizationMapService).retrieveUserOrganizationMap(invitedByUser);
        verify(idmInfoService).retrieveById(invitedByUser.getIdmInfo().getId());
        verify(cognitoService).inviteUser(userInviteRequest);
        verify(userService).create(any(User.class));
        verify(userOrganizationMapService).create(any(UserOrganizationMap.class));
        verify(userRoleMapService, times(userInviteRequest.getRoles().size())).create(any(UserRoleMap.class));
        verify(userGroupMapService, times(userInviteRequest.getGroups().size())).create(any(UserGroupMap.class));
        verify(groupService, times(userInviteRequest.getGroups().size())).retrieveGroup(anyString());
        log.info("Leaving givenValidUserInviteRequest_whenInviteUser_thenCreateUser()");

    }

    @Test
    @DisplayName("Given invalid UserInviteRequest, when inviteUser is called, then throw DynamoException")
    void givenInvalidUserInviteRequest_whenInviteUser_thenThrowDynamoException() throws DynamoException {
        log.info("Entering givenInvalidUserInviteRequest_whenInviteUser_thenThrowDynamoException()");

        // Given
        UserInviteRequest userInviteRequest = UserInviteRequest.builder().build();

        // Mock behavior for field validation
        Set<ConstraintViolation<UserInviteRequest>> fieldViolations = new HashSet<>();
        fieldViolations.add(mock(ConstraintViolation.class));
        when(fieldValidator.validate(userInviteRequest)).thenReturn(fieldViolations);

        // Then
        assertThrows(DynamoException.class, () -> userManagementService.inviteUser(userInviteRequest));
        verifyNoInteractions(userService, userOrganizationMapService, idmInfoService, cognitoService, groupService,
                userRoleMapService);
        log.info("Leaving givenInvalidUserInviteRequest_whenInviteUser_thenThrowDynamoException()");

    }

    @Test
    @DisplayName("Given valid UserAccountSetupRequest, when userAccountSetup is called, then return success response")
    public void givenValidUserAccountSetupRequest_whenUserAccountSetup_thenNoExceptionThrown() {
        log.info("Entering givenValidUserAccountSetupRequest_whenUserAccountSetup_thenNoExceptionThrown()");

        // Given
        UserAccountSetupRequest userAccountSetupRequest = UserAccountSetupRequest.builder().email("john@example.com")
                .password("securePassword").firstName("John").lastName("Doe").phoneNumber("1234567890").build();

        // Mock
        User mockUser =
                User.builder().id(1L).email("john@examble.com").idmUserId("0e4f5855-e738-4b24-aa33-2098a0ace529")
                        .idmInfo(IdmInfo.builder().idmUniqueId("unique_id").name("cognito").build())
                        .uniqueId(UUID.fromString("a7d82416-3e79-4232-bb6b-0094319b07b5")).firstName("John")
                        .lastName("Doe").phoneNumber("1234567890").status(UserStatus.ACTIVE.getStatus())
                        .createdOn(Instant.now()).modifiedOn(Instant.now()).build();

        // Mock behavior for field validation and user retrieval
        when(fieldValidator.validate(userAccountSetupRequest)).thenReturn(Collections.emptySet());
        when(userService.retrieveUser("john@example.com")).thenReturn(Optional.ofNullable(mockUser));

        // When and Then
        assertDoesNotThrow(() -> {
            userManagementService.setupUserAccount(userAccountSetupRequest);
        });

        // Verify
        verify(userService).update(mockUser);
        verify(cognitoService).setupUserAccount(eq(userAccountSetupRequest));

        log.info("Leaving givenValidUserAccountSetupRequest_whenUserAccountSetup_thenNoExceptionThrown()");
    }

    @Test
    @DisplayName("Given invalid UserAccountSetupRequest, when userAccountSetup is called, then throw DynamoException")
    public void givenInvalidUserAccountSetupRequest_whenUserAccountSetup_thenDynamoExceptionThrown() {
        log.info("Entering givenInvalidUserAccountSetupRequest_whenUserAccountSetup_thenDynamoExceptionThrown()");

        // Given
        UserAccountSetupRequest userAccountSetupRequest = new UserAccountSetupRequest();
        userAccountSetupRequest.setEmail("john@example.com");

        // Mock behavior for field validation
        Set<ConstraintViolation<UserAccountSetupRequest>> fieldViolations = new HashSet<>();
        fieldViolations.add(mock(ConstraintViolation.class));
        when(fieldValidator.validate(userAccountSetupRequest)).thenReturn(fieldViolations);

        // When and Then
        DynamoException exception = assertThrows(DynamoException.class, () -> {
            userManagementService.setupUserAccount(userAccountSetupRequest);
        });

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());

        // Verify
        verify(userService, never()).update(any(User.class));
        verify(cognitoService, never()).setupUserAccount(any(UserAccountSetupRequest.class));

        log.info("Leaving givenInvalidUserAccountSetupRequest_whenUserAccountSetup_thenDynamoExceptionThrown()");
    }

    @Test
    @DisplayName("Given not found user, when userAccountSetup is called, then throw DynamoException")
    public void givenUserNotFound_whenUserAccountSetup_thenDynamoExceptionThrown() {
        log.info("Entering givenUserNotFound_whenUserAccountSetup_thenDynamoExceptionThrown()");

        // Given
        UserAccountSetupRequest userAccountSetupRequest = new UserAccountSetupRequest();
        userAccountSetupRequest.setEmail("john@example.com");

        // Mock behavior for field validation
        when(fieldValidator.validate(userAccountSetupRequest)).thenReturn(Collections.emptySet());
        when(userService.retrieveUser("john@example.com")).thenReturn(Optional.empty());

        // When and Then
        DynamoException exception = assertThrows(DynamoException.class, () -> {
            userManagementService.setupUserAccount(userAccountSetupRequest);
        });

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());

        // Verify
        verify(userService, never()).update(any(User.class));
        verify(cognitoService, never()).setupUserAccount(any(UserAccountSetupRequest.class));

        log.info("Leaving givenUserNotFound_whenUserAccountSetup_thenDynamoExceptionThrown()");
    }

    @Test
    @DisplayName("Given valid userUpdateRequest, when updateUser is called, then return success response")
    public void givenValidUserUpdateRequest_whenUpdatingUser_thenExpectedBehavior() throws DynamoException {
        log.info("Entering givenValidUserUpdateRequest_whenUpdatingUser_thenExpectedBehavior()");

        // Given
        UUID userId = UUID.randomUUID();
        UserUpdateRequest userUpdateRequest = UserUpdateRequest.builder().userId(userId).idmUserId("user-id")
                .email("johndoe@gmail.com").firstName("John").lastName("Doe").phoneNumber("testPhoneNumber")
                .status("active").roles(List.of("user")).groups(List.of("developer")).build();

        // Mock
        User user = User.builder().id(1L).uniqueId(userId).firstName("John").lastName("Doe").email("jogn@examble.com")
                .build();
        Group group = Group.builder().name("developer").id(1L).build();
        Role role = Role.builder().id(1L).name("user").build();

        when(userService.retrieveUser(any(UUID.class))).thenReturn(Optional.of(new User())); // Adjust as needed

        when(userGroupMapService.retrieveUserGroupMap(any(User.class)))
                .thenReturn((List.of(UserGroupMap.builder().user(user).group(group).build())));

        when(userRoleMapService.retrieveUserRoleMap(any(User.class)))
                .thenReturn(List.of(UserRoleMap.builder().user(user).role(role).build()));

        // When
        userManagementService.updateUser(userUpdateRequest);

        // Then
        verify(userService, times(1)).update(any(User.class));
        verify(userService, times(1)).retrieveUser(userId);
        verify(cognitoService, times(1)).updateUser(userUpdateRequest);

        log.info("Leaving givenValidUserUpdateRequest_whenUpdatingUser_thenExpectedBehavior()");
    }

    @Test
    @DisplayName("Given inValid userUpdateRequest, when updateUser is called, then return error response and throw dynamoException")
    public void givenInvalidUserUpdateRequest_whenUpdatingUser_thenExceptionThrown() throws DynamoException {
        log.info("Entering givenInvalidUserUpdateRequest_whenUpdatingUser_thenExceptionThrown()");

        // Given
        UserUpdateRequest userUpdateRequest = UserUpdateRequest.builder()
                // .userId(UUID.randomUUID())
                .idmUserId("user-id").email("johndoe@gmail.com").firstName("John").lastName("Doe")
                .phoneNumber("testPhoneNumber").roles(List.of("user")).groups(List.of("developer")).build();

        // Mock behavior for field validation
        Set<ConstraintViolation<UserUpdateRequest>> fieldViolations = new HashSet<>();
        fieldViolations.add(mock(ConstraintViolation.class));
        when(fieldValidator.validate(userUpdateRequest)).thenReturn(fieldViolations);

        // When/Then
        assertThrows(DynamoException.class, () -> userManagementService.updateUser(userUpdateRequest));

        log.info("Leaving givenInvalidUserUpdateRequest_whenUpdatingUser_thenExceptionThrown()");
    }

    @Test
    @DisplayName("Given user not found userUpdateRequest, when updateUser is called, then return error response and throw dynamoException")
    public void givenUserNotFound_whenUpdatingUser_thenExceptionThrown() throws DynamoException {
        log.info("Entering givenUserNotFound_whenUpdatingUser_thenExceptionThrown()");

        // Given
        UserUpdateRequest userUpdateRequest = UserUpdateRequest.builder().userId(UUID.randomUUID()).idmUserId("user-id")
                .email("johndoe@gmail.com").firstName("John").lastName("Doe").phoneNumber("testPhoneNumber")
                .status("active").roles(List.of("user")).groups(List.of("developer")).build();

        // Mock
        when(userService.retrieveUser(any(UUID.class))).thenReturn(Optional.empty());

        // When/Then
        assertThrows(DynamoException.class, () -> userManagementService.updateUser(userUpdateRequest));

        log.info("Leaving givenUserNotFound_whenUpdatingUser_thenExceptionThrown()");
    }

    @Test
    @DisplayName("Given valid search and sort parameters, when retrieveUser is called, then return Paged userViewResponse")
    public void whenRetrieveUsersWithValidParameters_thenPageOfUserViewResponsesIsReturned() throws DynamoException {
        log.info("Entering whenRetrieveUsersWithValidParameters_thenPageOfUserViewResponsesIsReturned()");

        // Given
        MultiValueMap<String, String> searchOrFilterParameters = new LinkedMultiValueMap<>();
        Pageable pageable = PageRequest.of(0, 10); // Adjust as needed

        // Mock
        User user = User.builder().id(1L).email("john@examble.com").idmUserId("0e4f5855-e738-4b24-aa33-2098a0ace529")
                .firstName("john").idmInfo(IdmInfo.builder().idmUniqueId("unique_id").name("cognito").build())
                .uniqueId(UUID.fromString("a7d82416-3e79-4232-bb6b-0094319b07b5")).status(UserStatus.ACTIVE.getStatus())
                .createdOn(Instant.now()).modifiedOn(Instant.now()).build();

        List<User> pagedUsers = Collections.singletonList(user);

        Page<User> page = new PageImpl<>(pagedUsers, pageable, pagedUsers.size());
        when(userService.retrievePageEntitiesWithPredicate(any(BooleanBuilder.class), any(Pageable.class)))
                .thenReturn(page);

        UserViewResponse mockUserViewResponse =
                UserViewResponse.builder().userId(UUID.fromString("a7d82416-3e79-4232-bb6b-0094319b07b5"))
                        .firstName("john").status(UserStatus.ACTIVE.getStatus()).email("john@examble.com")
                        .lastName("doe").createdOn(Instant.now()).build();
        when(userMapperService.userToUserViewResponse(user)).thenReturn(mockUserViewResponse);

        // When
        Page<UserViewResponse> userViewResponsePage =
                userManagementService.retrieveUsers(searchOrFilterParameters, pageable);

        // Then
        assertNotNull(userViewResponsePage);
        verify(userService).retrievePageEntitiesWithPredicate(any(Predicate.class), eq(pageable));
        Assertions.assertThat(userViewResponsePage).isNotEmpty();

        log.info("Leaving whenRetrieveUsersWithValidParameters_thenPageOfUserViewResponsesIsReturned()");
    }

    @Test
    @DisplayName("Given inValid search and sort parameters, when retrieveUser is called, then return error response")
    public void whenRetrieveUsersWithInvalidParameter_thenDynamoExceptionIsThrown() {
        log.info("Entering whenRetrieveUsersWithInvalidParameter_thenDynamoExceptionIsThrown()");

        // Given
        MultiValueMap<String, String> searchOrFilterParameters = new LinkedMultiValueMap<>();
        searchOrFilterParameters.add("invalid-param", "some-value");
        Pageable pageable = PageRequest.of(0, 10);

        // When //Then
        assertThrows(DynamoException.class,
                () -> userManagementService.retrieveUsers(searchOrFilterParameters, pageable));

        log.info("Leaving whenRetrieveUsersWithInvalidParameter_thenDynamoExceptionIsThrown()");
    }

    @Test
    @DisplayName("Given valid user ID, when retrieveUser is called, then return user view response")
    public void givenValidUserId_whenRetrieveUserCalled_thenReturnUserViewResponse() throws DynamoException {
        log.info("Entering givenValidUserId_whenRetrieveUserCalled_thenReturnUserViewResponse()");

        // Given
        UUID userId = UUID.randomUUID();

        // Mock
        User mockUser = User.builder().id(1L).email("john@examble.com")
                .idmUserId("0e4f5855-e738-4b24-aa33-2098a0ace529").firstName("john")
                .idmInfo(IdmInfo.builder().idmUniqueId("unique_id").name("cognito").build()).uniqueId(userId)
                .status(UserStatus.ACTIVE.getStatus()).createdOn(Instant.now()).modifiedOn(Instant.now()).build();

        UserViewResponse expectedResponse = UserViewResponse.builder().userId(userId).firstName("john")
                .status(UserStatus.ACTIVE.getStatus()).email("john@examble.com").roles(List.of("user"))
                .groups(List.of("developer")).lastName("doe").createdOn(Instant.now()).build();

        when(userService.retrieveUser(userId)).thenReturn(Optional.of(mockUser));
        when(userRoleMapService.retrieveUserRoleMap(mockUser)).thenReturn(
                (List.of(UserRoleMap.builder().user(mockUser).role(Role.builder().name("user").build()).build())));
        when(userGroupMapService.retrieveUserGroupMap(mockUser)).thenReturn(List
                .of(UserGroupMap.builder().user(mockUser).group(Group.builder().name("developer").build()).build()));

        when(userMapperService.userToUserViewResponse(mockUser)).thenReturn(expectedResponse);

        // When
        UserViewResponse actualResponse = userManagementService.retrieveUser(userId);

        // Then
        assertEquals(expectedResponse.getUserId(), actualResponse.getUserId());

        // Verify
        verify(userService).retrieveUser(userId);

        log.info("Leaving givenValidUserId_whenRetrieveUserCalled_thenReturnUserViewResponse()");
    }

    @Test
    @DisplayName("Given invalid user ID, when retrieveUser is called, then throw DynamoException")
    public void givenInvalidUserId_whenRetrieveUserCalled_thenThrowDynamoException() throws DynamoException {
        log.info("Entering givenInvalidUserId_whenRetrieveUserCalled_thenThrowDynamoException()");

        // Given
        UUID userId = UUID.randomUUID();

        // Mock
        when(userService.retrieveUser(userId)).thenThrow(new DynamoException("User not found", HttpStatus.NOT_FOUND));

        // When/Then
        assertThrows(DynamoException.class, () -> userManagementService.retrieveUser(userId));

        // Verify
        verify(userService).retrieveUser(userId);

        log.info("Leaving givenInvalidUserId_whenRetrieveUserCalled_thenThrowDynamoException()");
    }

    @Test
    @DisplayName("Given valid userId, when suspendUser is called, then return success response")
    public void givenValidUserId_whenSuspendUser_thenUserIsSuspended() throws DynamoException {
        log.info("Entering givenValidUserId_whenSuspendUser_thenUserIsSuspended()");

        // Given
        UUID userId = UUID.randomUUID();

        // Mock
        User user = User.builder().uniqueId(userId).idmUserId("user-id").status(UserStatus.ACTIVE.getStatus()).build();

        when(userService.retrieveUser(userId)).thenReturn(Optional.of(user));
        when(userService.update(any(User.class))).thenReturn(user);
        doNothing().when(cognitoService).disableUser(anyString());

        // When
        userManagementService.suspendUser(userId);

        // Then
        assertEquals(UserStatus.SUSPENDED.getStatus(), user.getStatus());
        verify(userService, times(1)).update(user);
        verify(cognitoService, times(1)).disableUser(user.getIdmUserId());

        log.info("Leaving givenValidUserId_whenSuspendUser_thenUserIsSuspended()");
    }

    @Test
    @DisplayName("Given inValid userId, when suspendUser is called, then return error response")
    public void givenInvalidUserId_whenSuspendUser_thenDynamoExceptionIsThrown() {
        log.info("Entering givenInvalidUserId_whenSuspendUser_thenDynamoExceptionIsThrown()");

        // Given
        UUID userId = UUID.fromString("6adaed5d-bd99-4851-85ec-a3b515c2091a");

        // Mock
        when(userService.retrieveUser(userId)).thenReturn(Optional.empty());

        // When //Then
        assertThrows(DynamoException.class, () -> userManagementService.suspendUser(userId));

        log.info("Leaving givenInvalidUserId_whenSuspendUser_thenDynamoExceptionIsThrown()");
    }

    @Test
    @DisplayName("Given valid userId, when activateUser is called, then return success response")
    public void givenValidUserId_whenActivateUserWithValidUserId_thenUserIsActivated() throws DynamoException {
        log.info("Entering givenValidUserId_whenActivateUserWithValidUserId_thenUserIsActivated()");

        // Given
        UUID userId = UUID.fromString("6adaed5d-bd99-4851-85ec-a3b515c2091a");

        User user =
                User.builder().uniqueId(userId).idmUserId("user-id").status(UserStatus.SUSPENDED.getStatus()).build();

        when(userService.retrieveUser(userId)).thenReturn(Optional.of(user));
        when(userService.update(any(User.class))).thenReturn(user);
        doNothing().when(cognitoService).enableUser(anyString());

        // When
        userManagementService.activateUser(userId);

        // Then
        assertEquals(UserStatus.ACTIVE.getStatus(), user.getStatus());
        verify(userService, times(1)).update(user);
        verify(cognitoService, times(1)).enableUser(user.getIdmUserId());

        log.info("Leaving givenValidUserId_whenActivateUserWithValidUserId_thenUserIsActivated()");
    }

    @Test
    @DisplayName("Given inValid userId, when activateUser is called, then return error response")
    public void givenInvalidUserId_whenActivateUserWithInvalidUserId_thenDynamoExceptionIsThrown() {
        log.info("Entering givenInvalidUserId_whenActivateUserWithInvalidUserId_thenDynamoExceptionIsThrown()");

        // Given
        UUID userId = UUID.fromString("6adaed5d-bd99-4851-85ec-a3b515c2091a");

        when(userService.retrieveUser(userId)).thenReturn(Optional.empty());

        // When //Then
        assertThrows(DynamoException.class, () -> userManagementService.activateUser(userId));

        log.info("Leaving givenInvalidUserId_whenActivateUserWithInvalidUserId_thenDynamoExceptionIsThrown()");
    }

    @Test
    @DisplayName("GivenValidGroups_WhenRetrieveUsers_ThenReturnUsers")
    public void givenValidGroups_whenRetrieveUsers_thenReturnUsers() throws DynamoException {
        log.info("Entering givenValidGroups_whenRetrieveUsers_thenReturnUsers()");

        // Given
        List<String> validGroups = Arrays.asList("group1", "group2");
        List<User> expectedUsers = Arrays.asList(new User(), new User());

        // Mock
        when(groupService.retrieveGroup(validGroups.get(0)))
                .thenReturn(Optional.ofNullable(Group.builder().name("group1").build()));
        when(groupService.retrieveGroup(validGroups.get(1)))
                .thenReturn(Optional.ofNullable(Group.builder().name("group2").build()));
        when(userService.retrieveUsersByGroup(validGroups)).thenReturn(expectedUsers);

        // When
        List<User> resultUsers = userManagementService.retrieveUsersByGroup(validGroups);

        // Then
        assertEquals(expectedUsers, resultUsers);
        verify(userService, times(1)).retrieveUsersByGroup(validGroups);

        log.info("Leaving givenValidGroups_whenRetrieveUsers_thenReturnUsers()");
    }

    @Test
    @DisplayName("GivenEmptyGroups_WhenRetrieveUsers_ThenThrowException")
    public void givenEmptyGroups_whenRetrieveUsers_thenThrowException() {
        log.info("Entering givenEmptyGroups_whenRetrieveUsers_thenThrowException()");

        // Given
        List<String> emptyGroups = Arrays.asList("group1", "group2");

        // Mock
        when(groupService.retrieveGroup(emptyGroups.get(0))).thenReturn(Optional.empty());

        // When & Then
        assertThrows(DynamoException.class, () -> {
            userManagementService.retrieveUsersByGroup(emptyGroups);
        });
        verify(userService, never()).retrieveUsersByGroup(emptyGroups);

        log.info("Leaving givenEmptyGroups_whenRetrieveUsers_thenThrowException()");
    }

    @Test
    @DisplayName("GivenValidRoles_WhenRetrieveUsers_ThenReturnUsers")
    public void givenValidRoles_whenRetrieveUsers_thenReturnUsers() throws DynamoException {
        log.info("Entering givenValidRoles_whenRetrieveUsers_thenReturnUsers()");

        // Given
        List<String> validRoles = Arrays.asList("role1", "role2");
        List<User> expectedUsers = Arrays.asList(new User(), new User());

        // Mock
        when(roleService.retrieveRole(validRoles.get(0)))
                .thenReturn(Optional.ofNullable(Role.builder().name("role1").build()));
        when(roleService.retrieveRole(validRoles.get(1)))
                .thenReturn(Optional.ofNullable(Role.builder().name("role2").build()));
        when(userService.retrieveUsersByRole(validRoles)).thenReturn(expectedUsers);

        // When
        List<User> resultUsers = userManagementService.retrieveUsersByRole(validRoles);

        // Then
        assertEquals(expectedUsers, resultUsers);
        verify(userService, times(1)).retrieveUsersByRole(validRoles);

        log.info("Leaving givenValidRoles_whenRetrieveUsers_thenReturnUsers()");
    }

    @Test
    @DisplayName("GivenEmptyRoles_WhenRetrieveUsers_ThenThrowException")
    public void givenEmptyRoles_whenRetrieveUsers_thenThrowException() {
        log.info("Entering givenEmptyRoles_whenRetrieveUsers_thenThrowException()");

        // Given
        List<String> emptyRoles = Arrays.asList("role1", "role2");

        // Mock
        when(roleService.retrieveRole(emptyRoles.get(0))).thenReturn(Optional.empty());

        // When & Then
        assertThrows(DynamoException.class, () -> {
            userManagementService.retrieveUsersByRole(emptyRoles);
        });
        verify(userService, never()).retrieveUsersByRole(emptyRoles);

        log.info("Leaving givenEmptyRoles_whenRetrieveUsers_thenThrowException()");
    }

    @Test
    @Tag("CreateUserAfterSignUp")
    @Disabled
    @DisplayName("Given valid SignedUpUserData when createUser then return User")
    public void givenValidSignedUpUserData_whenCreateUser_thenReturnUser()
            throws DynamoException, JsonProcessingException {
        log.info("Entering givenValidSignedUpUserData_whenCreateUser_thenReturnUser()");

        // Given
        SignedUpUserData signedUpUserData =
                SignedUpUserData.builder().firstName("John").lastName("Doe").email("john.doe@example.com")
                        .isEmailVerified(Boolean.TRUE).middleName("James").phoneNumber("+1234567890")
                        .organization("Acme").roles(List.of("user")).groups(List.of("users")).idmUserStatus("CONFIRMED")
                        .idmId("test-idm-id").idmName("Cognito").idmUniqueUserId(UUID.randomUUID().toString()).build();

        // Mock
        when(idmInfoService.retrieveIdmInfo(eq(signedUpUserData.getIdmId()))).thenReturn(Optional.of(new IdmInfo()));
        String mockAdditionalInfo =
                "{\n                \"isEmailVerified\":true,\n                \"middleName\": \"James\",\n                \"age\": 25\n            }";
        when(objectMapper.writeValueAsString(any(Map.class))).thenReturn(mockAdditionalInfo);
        User mockerUser = User.builder().id(1L).firstName("John").lastName("Doe").email("john.doe@example.com")
                .phoneNumber("+1234567890").additionalInfo(mockAdditionalInfo).status("active").createdOn(Instant.now())
                .modifiedOn(Instant.now()).build();
        when(userMapperService.signedUpUserDataToUser(eq(signedUpUserData), any(IdmInfo.class))).thenReturn(mockerUser);
        when(organizationService.retrieveOrganization(eq(signedUpUserData.getOrganization())))
                .thenReturn(Optional.of(new Organization()));
        when(roleService.retrieveRole(anyString())).thenReturn(Optional.of(new Role()));
        when(groupService.retrieveGroup(anyString())).thenReturn(Optional.of(new Group()));
        when(userService.create(eq(mockerUser))).thenReturn(mockerUser);

        User user = userManagementService.createUser(signedUpUserData);
        assertEquals(mockerUser, user);

        log.info("Leaving givenValidSignedUpUserData_whenCreateUser_thenReturnUser()");
    }

    @Test
    @Tag("CreateUserAfterSignUp")
    @DisplayName("Given invalid IDM id in SignedUpUserData when createUser then throw Exception")
    public void givenInvalidIdmIdSignedUpUserData_whenCreateUser_thenThrowException() {
        log.info("Entering givenInvalidIdmIdSignedUpUserData_whenCreateUser_thenThrowException()");

        // Given
        SignedUpUserData signedUpUserData = SignedUpUserData.builder().organization("unknown-organization")
                .roles(List.of("unknown-role")).groups(List.of("unknown-group")).build();

        // Mock
        when(idmInfoService.retrieveIdmInfo(eq(signedUpUserData.getIdmId()))).thenReturn(Optional.empty());

        // When & Then
        assertThrows(DynamoException.class, () -> userManagementService.createUser(signedUpUserData));

        log.info("Leaving givenInvalidIdmIdSignedUpUserData_whenCreateUser_thenThrowException()");
    }

    @Test
    @Tag("CreateUserAfterSignUp")
    @DisplayName("Given invalid Organization name in SignedUpUserData when createUser then throw Exception")
    public void givenInvalidOrganizationNameSignedUpUserData_whenCreateUser_thenThrowException()
            throws JsonProcessingException {
        log.info("Entering givenInvalidOrganizationNameSignedUpUserData_whenCreateUser_thenThrowException()");

        // Given
        SignedUpUserData signedUpUserData = SignedUpUserData.builder().organization("unknown-organization")
                .roles(List.of("unknown-role")).groups(List.of("unknown-group")).build();

        // Mock
        when(idmInfoService.retrieveIdmInfo(eq(signedUpUserData.getIdmId()))).thenReturn(Optional.of(new IdmInfo()));
        String mockAdditionalInfo =
                "{\n                \"isEmailVerified\":true,\n                \"middleName\": \"James\",\n                \"age\": 25\n            }";
        when(objectMapper.writeValueAsString(any(Map.class))).thenReturn(mockAdditionalInfo);
        User mockerUser = User.builder().id(1L).firstName("John").lastName("Doe").email("john.doe@example.com")
                .phoneNumber("+1234567890").additionalInfo(mockAdditionalInfo).status("active").createdOn(Instant.now())
                .modifiedOn(Instant.now()).build();
        when(userMapperService.signedUpUserDataToUser(eq(signedUpUserData), any(IdmInfo.class))).thenReturn(mockerUser);
        when(organizationService.retrieveOrganization(eq(signedUpUserData.getOrganization())))
                .thenReturn(Optional.empty());

        // When & Then
        assertThrows(DynamoException.class, () -> userManagementService.createUser(signedUpUserData));

        log.info("Leaving givenInvalidOrganizationNameSignedUpUserData_whenCreateUser_thenThrowException()");
    }

    @Test
    @Tag("CreateUserAfterSignUp")
    @DisplayName("Given valid SignedUpUserData when createUser then throw Exception")
    public void givenValidSignedUpUserData_whenCreateUser_thenThrowException() throws JsonProcessingException {
        log.info("Entering givenValidSignedUpUserData_whenCreateUser_thenThrowException()");

        // Given
        SignedUpUserData signedUpUserData = SignedUpUserData.builder().organization("unknown-organization")
                .roles(List.of("unknown-role")).groups(List.of("unknown-group")).build();

        // Mock
        when(idmInfoService.retrieveIdmInfo(eq(signedUpUserData.getIdmId()))).thenReturn(Optional.of(new IdmInfo()));
        User mockerUser = User.builder().id(1L).firstName("John").lastName("Doe").email("john.doe@example.com")
                .phoneNumber("+1234567890").status("active").createdOn(Instant.now()).modifiedOn(Instant.now()).build();
        when(userMapperService.signedUpUserDataToUser(eq(signedUpUserData), any(IdmInfo.class))).thenReturn(mockerUser);
        when(objectMapper.writeValueAsString(any(Map.class))).thenThrow(JsonProcessingException.class);

        // When & Then
        assertThrows(DynamoException.class, () -> userManagementService.createUser(signedUpUserData));

        log.info("Leaving givenValidSignedUpUserData_whenCreateUser_thenThrowException()");
    }

}
