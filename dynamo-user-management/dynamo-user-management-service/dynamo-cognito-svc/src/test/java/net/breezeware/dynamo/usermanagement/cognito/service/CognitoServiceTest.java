package net.breezeware.dynamo.usermanagement.cognito.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import net.breezeware.dynamo.usermanagement.cognito.enumeration.UserAttributes;
import net.breezeware.dynamo.usermanagement.dto.UserAccountSetupRequest;
import net.breezeware.dynamo.usermanagement.dto.UserInviteRequest;
import net.breezeware.dynamo.usermanagement.dto.UserUpdateRequest;
import net.breezeware.dynamo.usermanagement.dto.UserViewResponse;
import net.breezeware.dynamo.utils.exception.DynamoException;

import lombok.extern.slf4j.Slf4j;

import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminAddUserToGroupRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminCreateUserRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminCreateUserResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminDisableUserRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminDisableUserResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminEnableUserRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminEnableUserResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminGetUserRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminGetUserResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminListGroupsForUserRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminListGroupsForUserResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminSetUserPasswordRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminSetUserPasswordResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminUpdateUserAttributesRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminUpdateUserAttributesResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AttributeType;
import software.amazon.awssdk.services.cognitoidentityprovider.model.GetGroupRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.GetGroupResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.model.GroupType;
import software.amazon.awssdk.services.cognitoidentityprovider.model.UserType;

@ExtendWith(MockitoExtension.class)
@DisplayName("Cognito User Management Test")
@Slf4j
public class CognitoServiceTest {

    @Mock
    CognitoIdentityProviderClient cognitoIdentityProviderClient;
    @InjectMocks
    CognitoService cognitoService;

    @Test
    @DisplayName("Given valid validUser, when inviteUser is called, then create and return a new user id")
    void givenValidUser_whenInviteUser_thenCreateUserAndAddToGroups() {
        log.info("Entering givenValidUser_whenInviteUser_thenCreateUserAndAddToGroups()");
        // Given
        UserInviteRequest userInviteRequest = new UserInviteRequest();
        userInviteRequest.setEmail("test@example.com");
        userInviteRequest.setGroups(Arrays.asList("group1", "group2"));
        userInviteRequest.setRoles(Arrays.asList("role1", "role2"));

        // Mock
        when(cognitoIdentityProviderClient.getGroup(any(GetGroupRequest.class)))
                .thenReturn(GetGroupResponse.builder().build());
        when(cognitoIdentityProviderClient.adminCreateUser(any(AdminCreateUserRequest.class)))
                .thenReturn(AdminCreateUserResponse.builder()
                        .user(UserType.builder().username("test@example.com").build()).build());
        when(cognitoIdentityProviderClient.adminListGroupsForUser(any(AdminListGroupsForUserRequest.class)))
                .thenReturn(AdminListGroupsForUserResponse.builder().build());
        // When
        cognitoService.inviteUser(userInviteRequest);

        // Then
        verify(cognitoIdentityProviderClient, times(4)).getGroup(any(GetGroupRequest.class));
        verify(cognitoIdentityProviderClient, times(4)).adminAddUserToGroup(any(AdminAddUserToGroupRequest.class));
        verify(cognitoIdentityProviderClient).adminCreateUser(any(AdminCreateUserRequest.class));
        log.info("Leaving givenValidUser_whenInviteUser_thenCreateUserAndAddToGroups()");

    }

    @Test
    @DisplayName("Given valid userAccountSetupRequest, when userAccountSetup is called, then setup the user account")
    public void givenValidUserAccountSetupRequest_whenUserAccountSetup_thenNoExceptionThrown() throws DynamoException {
        log.info("Entering givenValidUserAccountSetupRequest_whenUserAccountSetup_thenNoExceptionThrown()");

        // Given
        UserAccountSetupRequest userAccountSetupRequest = UserAccountSetupRequest.builder().email("test@example.com")
                .password("securePassword").firstName("John").lastName("Doe").phoneNumber("1234567890").build();

        // Mock the behavior
        when(cognitoIdentityProviderClient.adminSetUserPassword(
                AdminSetUserPasswordRequest.builder().password(userAccountSetupRequest.getPassword())
                        .username(userAccountSetupRequest.getEmail()).permanent(true).build()))
                                .thenReturn(AdminSetUserPasswordResponse.builder().build());
        when(cognitoIdentityProviderClient.adminUpdateUserAttributes(any(AdminUpdateUserAttributesRequest.class)))
                .thenReturn(any(AdminUpdateUserAttributesResponse.class));

        // When and Then
        assertDoesNotThrow(() -> {
            // When
            cognitoService.setupUserAccount(userAccountSetupRequest);
        });

        // Then
        verify(cognitoIdentityProviderClient, times(1)).adminSetUserPassword(any(AdminSetUserPasswordRequest.class));
        verify(cognitoIdentityProviderClient, times(1))
                .adminUpdateUserAttributes(any(AdminUpdateUserAttributesRequest.class));
        log.info("Leaving givenValidUserAccountSetupRequest_whenUserAccountSetup_thenNoExceptionThrown()");

    }

    @Test
    @DisplayName("Given valid userUpdateRequest, when updateUser is called, then update the user account")
    public void givenValidUserUpdateRequest_whenUpdatingUser_thenExpectedMethodsCalled() throws DynamoException {
        log.info("Entering givenValidUserUpdateRequest_whenUpdatingUser_thenExpectedMethodsCalled()");

        // Given
        UserUpdateRequest userUpdateRequest = UserUpdateRequest.builder().userId(UUID.randomUUID()).idmUserId("user-id")
                .email("johndoe@gmail.com").firstName("John").lastName("Doe").phoneNumber("testPhoneNumber")
                .roles(List.of("user")).groups(List.of("developer")).build();

        // Mock
        AdminListGroupsForUserResponse adminListGroupsResponse = AdminListGroupsForUserResponse.builder()
                .groups(Collections.singletonList(GroupType.builder().groupName("developer").build())).build();

        AdminUpdateUserAttributesResponse adminUpdateUserAttributesResponse =
                AdminUpdateUserAttributesResponse.builder().build();

        when(cognitoIdentityProviderClient.adminListGroupsForUser(any(AdminListGroupsForUserRequest.class)))
                .thenReturn(adminListGroupsResponse);

        when(cognitoIdentityProviderClient.adminUpdateUserAttributes(any(AdminUpdateUserAttributesRequest.class)))
                .thenReturn(adminUpdateUserAttributesResponse);

        // When
        cognitoService.updateUser(userUpdateRequest);

        // Verify
        verify(cognitoIdentityProviderClient, times(1))
                .adminListGroupsForUser(any(AdminListGroupsForUserRequest.class));
        verify(cognitoIdentityProviderClient, times(1))
                .adminUpdateUserAttributes(any(AdminUpdateUserAttributesRequest.class));
        log.info("Leaving givenValidUserUpdateRequest_whenUpdatingUser_thenExpectedMethodsCalled()");
    }

    @Test
    @DisplayName("Given valid userUpdateRequest, when updateInviteUser is called, then update the user account")
    public void givenValidUserUpdateRequest_whenUpdatingInviteUser_thenExpectedMethodsCalled() throws DynamoException {
        log.info("Entering givenValidUserUpdateRequest_whenUpdatingInviteUser_thenExpectedMethodsCalled()");

        // Given
        UserUpdateRequest userUpdateRequest = UserUpdateRequest.builder().userId(UUID.randomUUID()).idmUserId("user-id")
                .email("johndoe@gmail.com").roles(List.of("user")).groups(List.of("developer")).build();

        // Mock
        when(cognitoIdentityProviderClient.adminGetUser(any(AdminGetUserRequest.class)))
                .thenReturn(AdminGetUserResponse.builder().build());

        // When
        cognitoService.updateInvitedUser(userUpdateRequest);

        // Verify
        verify(cognitoIdentityProviderClient, times(1)).adminGetUser(any(AdminGetUserRequest.class));
        log.info("Leaving givenValidUserUpdateRequest_whenUpdatingInviteUser_thenExpectedMethodsCalled()");
    }

    @Test
    @DisplayName("Given a user ID, when retrieveUser, then return the user data")
    void givenUserId_whenRetrieveUser_thenReturnUser() throws DynamoException {
        log.info("Entering givenUserId_whenRetrieveUser_thenReturnUser()");

        // Given
        String userId = String.valueOf(UUID.randomUUID());

        // Mock
        when(cognitoIdentityProviderClient.adminGetUser(any(AdminGetUserRequest.class))).thenReturn(AdminGetUserResponse
                .builder()
                .userAttributes(
                        AttributeType.builder().name(UserAttributes.EMAIL.name).value("johndoe@gmail.com").build(),
                        AttributeType.builder().name(UserAttributes.PHONE_NUMBER.name).value("+12065551212").build(),
                        AttributeType.builder().name(UserAttributes.GIVEN_NAME.name).value("John").build(),
                        AttributeType.builder().name(UserAttributes.FAMILY_NAME.name).value("Doe").build())
                .build());

        UserViewResponse expectedResponse = UserViewResponse.builder().email("johndoe@gmail.com")
                .phoneNumber("+12065551212").firstName("John").lastName("Doe").build();

        UserViewResponse actualResponse = cognitoService.retrieveUser(userId);
        assertEquals(expectedResponse, actualResponse);
        log.info("Leaving givenUserId_whenRetrieveUser_thenReturnUser()");
    }

    @Test
    @DisplayName("Given a user ID, when enabling a user, then the user should be enabled")
    void givenUserId_whenEnablingUser_thenUserShouldBeEnabled() throws DynamoException {
        log.info("Entering givenUserId_whenEnablingUser_thenUserShouldBeEnabled()");

        // Given
        String userId = String.valueOf(UUID.randomUUID());

        // Mock
        AdminEnableUserRequest adminEnableUserRequest = AdminEnableUserRequest.builder().username(userId).build();

        when(cognitoIdentityProviderClient.adminEnableUser(adminEnableUserRequest))
                .thenReturn(AdminEnableUserResponse.builder().build());

        // When
        cognitoService.enableUser(userId);

        // Then
        verify(cognitoIdentityProviderClient).adminEnableUser(adminEnableUserRequest);
        log.info("Leaving givenUserId_whenEnablingUser_thenUserShouldBeEnabled()");
    }

    @Test
    @DisplayName("Given a user ID, when disabling a user, then the user should be disabled")
    void givenUserId_whenDisablingUser_thenUserShouldBeDisabled() throws DynamoException {
        log.info("Entering givenUserId_whenDisablingUser_thenUserShouldBeDisabled()");

        // Given
        String userId = String.valueOf(UUID.randomUUID());

        // Mock
        AdminDisableUserRequest adminDisableUserRequest = AdminDisableUserRequest.builder().username(userId).build();

        when(cognitoIdentityProviderClient.adminDisableUser(adminDisableUserRequest))
                .thenReturn(AdminDisableUserResponse.builder().build());

        // When
        cognitoService.disableUser(userId);

        // Then
        verify(cognitoIdentityProviderClient).adminDisableUser(adminDisableUserRequest);
        log.info("Leaving givenUserId_whenDisablingUser_thenUserShouldBeDisabled()");
    }

}
