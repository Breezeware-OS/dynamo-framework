package net.breezeware.dynamo.usermanagement.cognito.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import net.breezeware.dynamo.usermanagement.cognito.enumeration.UserAttributes;
import net.breezeware.dynamo.usermanagement.dto.UserAccountSetupRequest;
import net.breezeware.dynamo.usermanagement.dto.UserInviteRequest;
import net.breezeware.dynamo.usermanagement.dto.UserUpdateRequest;
import net.breezeware.dynamo.usermanagement.dto.UserViewResponse;
import net.breezeware.dynamo.utils.exception.DynamoException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminAddUserToGroupRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminCreateUserRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminCreateUserResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminDeleteUserRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminDisableUserRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminEnableUserRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminGetUserRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminGetUserResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminListGroupsForUserRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminListGroupsForUserResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminRemoveUserFromGroupRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminSetUserPasswordRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminUpdateUserAttributesRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AttributeType;
import software.amazon.awssdk.services.cognitoidentityprovider.model.CognitoIdentityProviderException;
import software.amazon.awssdk.services.cognitoidentityprovider.model.DeliveryMediumType;
import software.amazon.awssdk.services.cognitoidentityprovider.model.GetGroupRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.GroupType;

/**
 * This class defines the operations for managing user accounts in the system
 * using AWS cognito.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class CognitoService {

    private final CognitoIdentityProviderClient cognitoIdentityProviderClient;

    @Value("${aws.cognito.user-pool.id}")
    private String userPoolId;

    /**
     * Invites a new user to the system by creating their account in the Cognito
     * identity management system, setting their email as the username, and
     * associating them with the specified groups and roles.
     * @param  userInviteRequest The request object containing user invitation
     *                           details.
     * @return                   The username of the invited user.
     * @throws DynamoException   If there's an error during the invitation process
     *                           or if the specified request is invalid.
     */
    public String inviteUser(UserInviteRequest userInviteRequest) throws DynamoException {
        log.info("Entering inviteUser()");

        // convert to cognito groups
        List<String> cognitoGroups = prepareGroupsAndRoles(userInviteRequest.getGroups(), userInviteRequest.getRoles());

        try {
            // create user
            AdminCreateUserRequest adminCreateUserRequest =
                    AdminCreateUserRequest.builder().userPoolId(userPoolId).username(userInviteRequest.getEmail())
                            .userAttributes(
                                    AttributeType.builder().name(UserAttributes.EMAIL.name)
                                            .value(userInviteRequest.getEmail()).build(),
                                    AttributeType.builder().name(UserAttributes.USER_ID.name)
                                            .value(String.valueOf(userInviteRequest.getUserId())).build())
                            .desiredDeliveryMediums(DeliveryMediumType.EMAIL).build();
            AdminCreateUserResponse adminCreateUserResponse =
                    cognitoIdentityProviderClient.adminCreateUser(adminCreateUserRequest);

            // add groups to the invited user
            updateUserGroups(adminCreateUserResponse.user().username(), cognitoGroups);

            log.info("Leaving inviteUser()");
            return adminCreateUserResponse.user().username();
        } catch (CognitoIdentityProviderException e) {
            log.error("Exception: {}", e.getLocalizedMessage());
            throw new DynamoException(e.awsErrorDetails().errorMessage(), HttpStatus.valueOf(e.statusCode()));
        } catch (SdkClientException e) {
            log.error("Exception: {}", e.getLocalizedMessage());
            throw new DynamoException(e.getMessage(), HttpStatus.BAD_REQUEST);
        }

    }

    /**
     * Prepares a list of Cognito groups and roles by prefixing the group and role
     * names with "G_" and "R_" respectively.
     * @param  groups A list of group names.
     * @param  roles  A list of role names.
     * @return        A list containing modified Cognito group names.
     */
    private List<String> prepareGroupsAndRoles(List<String> groups, List<String> roles) {
        log.info("Entering prepareGroupsAndRoles()");
        List<String> cognitoGroups = new ArrayList<>();

        groups.stream().map(group -> "g_" + group).forEach(cognitoGroups::add);

        roles.stream().map(role -> "r_" + role).forEach(cognitoGroups::add);

        log.info("Entering prepareGroupsAndRoles()");
        return cognitoGroups;
    }

    /**
     * Sets up a user's account, including updating their password and user
     * attributes, such as first name, last name, phone number, and verification
     * status.
     * @param  userAccountSetupRequest The request object containing user account
     *                                 setup details.
     * @throws DynamoException         If there's an error during the account setup
     *                                 process or if the specified request is
     *                                 invalid.
     */
    public void setupUserAccount(UserAccountSetupRequest userAccountSetupRequest) throws DynamoException {
        log.info("Entering setupUserAccount()");
        try {
            // update user password
            cognitoIdentityProviderClient.adminSetUserPassword(AdminSetUserPasswordRequest.builder()
                    .userPoolId(userPoolId).password(userAccountSetupRequest.getPassword())
                    .username(userAccountSetupRequest.getEmail()).permanent(true).build());

            // update user attributes
            AdminUpdateUserAttributesRequest adminUpdateUserAttributesRequest = AdminUpdateUserAttributesRequest
                    .builder().userPoolId(userPoolId).username(userAccountSetupRequest.getEmail())
                    .userAttributes(
                            AttributeType.builder().name(UserAttributes.GIVEN_NAME.name)
                                    .value(userAccountSetupRequest.getFirstName()).build(),
                            AttributeType.builder().name(UserAttributes.FAMILY_NAME.name)
                                    .value(userAccountSetupRequest.getLastName()).build(),
                            AttributeType.builder().name(UserAttributes.PHONE_NUMBER.name)
                                    .value(userAccountSetupRequest.getPhoneNumber()).build(),
                            AttributeType.builder().name(UserAttributes.PHONE_NUMBER_VERIFIED.name)
                                    .value(Boolean.TRUE.toString()).build(),
                            AttributeType.builder().name(UserAttributes.EMAIL_VERIFIED.name)
                                    .value(Boolean.TRUE.toString()).build())
                    .build();
            cognitoIdentityProviderClient.adminUpdateUserAttributes(adminUpdateUserAttributesRequest);

            log.info("Leaving setupUserAccount()");
        } catch (CognitoIdentityProviderException e) {
            log.error("Exception: {}", e.getLocalizedMessage());
            throw new DynamoException(e.awsErrorDetails().errorMessage(), HttpStatus.valueOf(e.statusCode()));
        } catch (SdkClientException e) {
            log.error("Exception: {}", e.getLocalizedMessage());
            throw new DynamoException(e.getMessage(), HttpStatus.BAD_REQUEST);
        }

    }

    /**
     * Updates user attributes in the Cognito identity management system based on
     * the provided user update request. This method updates user attributes like
     * first name, last name, email, phone number, and verification status. It also
     * updates the user's group and role associations.
     * @param  userUpdateRequest The request object containing user update details.
     * @throws DynamoException   If there's an error during the update process or if
     *                           the specified user update request is invalid.
     */
    public void updateUser(UserUpdateRequest userUpdateRequest) throws DynamoException {
        log.info("Entering updateUser()");

        // convert to cognito groups
        List<String> updateUserGroups =
                prepareGroupsAndRoles(userUpdateRequest.getGroups(), userUpdateRequest.getRoles());

        try {
            // update user groups
            updateUserGroups(userUpdateRequest.getIdmUserId(), updateUserGroups);

            // update user attributes
            cognitoIdentityProviderClient.adminUpdateUserAttributes(AdminUpdateUserAttributesRequest.builder()
                    .userPoolId(userPoolId).username(userUpdateRequest.getIdmUserId())
                    .userAttributes(
                            AttributeType.builder().name(UserAttributes.GIVEN_NAME.name)
                                    .value(userUpdateRequest.getFirstName()).build(),
                            AttributeType.builder().name(UserAttributes.FAMILY_NAME.name)
                                    .value(userUpdateRequest.getLastName()).build(),
                            AttributeType.builder().name(UserAttributes.EMAIL.name).value(userUpdateRequest.getEmail())
                                    .build(),
                            AttributeType.builder().name(UserAttributes.PHONE_NUMBER.name)
                                    .value(userUpdateRequest.getPhoneNumber()).build(),
                            AttributeType.builder().name(UserAttributes.EMAIL_VERIFIED.name)
                                    .value(Boolean.TRUE.toString()).build(),
                            AttributeType.builder().name(UserAttributes.PHONE_NUMBER_VERIFIED.name)
                                    .value(Boolean.TRUE.toString()).build())
                    .build());
            log.info("Leaving updateUser()");
        } catch (CognitoIdentityProviderException e) {
            log.error("Exception: {}", e.getLocalizedMessage());
            throw new DynamoException(e.awsErrorDetails().errorMessage(), HttpStatus.valueOf(e.statusCode()));
        } catch (SdkClientException e) {
            log.error("Exception: {}", e.getLocalizedMessage());
            throw new DynamoException(e.getMessage(), HttpStatus.BAD_REQUEST);
        }

    }

    /**
     * Updates the groups associated with a user in a Cognito User Pool.
     * @param userId           The unique identifier of the user whose groups are to
     *                         be updated.
     * @param updateUserGroups A list of group names to which the user should belong
     *                         after the update.
     */
    private void updateUserGroups(String userId, List<String> updateUserGroups) {
        log.info("Entering updateUserGroups()");

        // validate groups
        updateUserGroups.forEach(group -> cognitoIdentityProviderClient
                .getGroup(GetGroupRequest.builder().groupName(group).userPoolId(userPoolId).build()));

        // retrieve user groups
        AdminListGroupsForUserResponse adminListGroupsForUserResponse =
                cognitoIdentityProviderClient.adminListGroupsForUser(
                        AdminListGroupsForUserRequest.builder().userPoolId(userPoolId).username(userId).build());

        // create a map of group names to GroupType objects for faster lookup
        Map<String, GroupType> groupNameToGroupType = adminListGroupsForUserResponse.groups().stream()
                .collect(Collectors.toMap(GroupType::groupName, map -> map));

        // check group name and size are equal
        boolean areGroupsEqual = updateUserGroups.stream().allMatch(groupNameToGroupType::containsKey)
                && updateUserGroups.size() == groupNameToGroupType.size();

        if (!areGroupsEqual) {
            // delete user groups that are not in the update request
            adminListGroupsForUserResponse.groups().forEach(group -> {
                if (!updateUserGroups.contains(group.groupName())) {
                    cognitoIdentityProviderClient.adminRemoveUserFromGroup(AdminRemoveUserFromGroupRequest.builder()
                            .username(userId).userPoolId(userPoolId).groupName(group.groupName()).build());
                }

            });

            // add user groups for new groups in the update request
            updateUserGroups.stream().filter(groupName -> !groupNameToGroupType.containsKey(groupName))
                    .forEach(groupName -> {
                        cognitoIdentityProviderClient.adminAddUserToGroup(AdminAddUserToGroupRequest.builder()
                                .username(userId).userPoolId(userPoolId).groupName(groupName).build());
                    });

        }

        log.info("Leaving updateUserGroups()");
    }

    /**
     * Updates the profile of an invited user in the Cognito identity management
     * system and handles changes to the user's email and group/role associations.
     * This method checks if the email has changed and, if so, re-invites the user.
     * It also updates the user's group and role associations.
     * @param  userUpdateRequest The request object containing user update details.
     * @return                   The user ID of the invited user if the email was
     *                           changed and re-invitation was necessary, otherwise
     *                           an empty string.
     * @throws DynamoException   If there's an error during the update process or if
     *                           the specified user ID or request is invalid.
     */
    public String updateInvitedUser(UserUpdateRequest userUpdateRequest) throws DynamoException {
        log.info("Entering updateInvitedUser()");
        try {
            // retrieve user
            AdminGetUserResponse adminGetUserResponse = cognitoIdentityProviderClient.adminGetUser(AdminGetUserRequest
                    .builder().userPoolId(userPoolId).username(userUpdateRequest.getIdmUserId()).build());

            String invitedUserId = "";
            for (AttributeType user : adminGetUserResponse.userAttributes()) {

                // if invited user email change
                if (user.name().equalsIgnoreCase(UserAttributes.EMAIL.name)) {
                    if (!user.value().equalsIgnoreCase(userUpdateRequest.getEmail())) {
                        cognitoIdentityProviderClient.adminDeleteUser(AdminDeleteUserRequest.builder()
                                .userPoolId(userPoolId).username(userUpdateRequest.getIdmUserId()).build());
                        UserInviteRequest userInviteRequest = buildUserInviteRequest(userUpdateRequest);
                        invitedUserId = inviteUser(userInviteRequest);
                    }

                } else {
                    // convert to cognito groups
                    List<String> updateUserGroups =
                            prepareGroupsAndRoles(userUpdateRequest.getGroups(), userUpdateRequest.getRoles());

                    // validate and update user groups
                    updateUserGroups(userUpdateRequest.getIdmUserId(), updateUserGroups);

                }

            }

            log.info("Leaving updateInvitedUser()");
            return invitedUserId;
        } catch (CognitoIdentityProviderException e) {
            log.error("Exception: {}", e.getLocalizedMessage());
            throw new DynamoException(e.awsErrorDetails().errorMessage(), HttpStatus.valueOf(e.statusCode()));
        } catch (SdkClientException e) {
            log.error("Exception: {}", e.getLocalizedMessage());
            throw new DynamoException(e.getMessage(), HttpStatus.BAD_REQUEST);
        }

    }

    /**
     * Builds a {@link UserInviteRequest} object based on the provided
     * {@link UserUpdateRequest}.
     * @param  userUpdateRequest The {@link UserUpdateRequest} containing updated
     *                           user information.
     * @return                   A {@link UserInviteRequest} object populated with
     *                           the relevant data from the input request.
     */
    private UserInviteRequest buildUserInviteRequest(UserUpdateRequest userUpdateRequest) {
        log.info("Entering buildUserInviteRequest() {}", userUpdateRequest);
        UserInviteRequest userInviteRequest = new UserInviteRequest();
        userInviteRequest.setUserId(userUpdateRequest.getUserId());
        userInviteRequest.setEmail(userUpdateRequest.getEmail());
        userInviteRequest.setFirstName(userUpdateRequest.getFirstName());
        userInviteRequest.setLastName(userUpdateRequest.getLastName());
        userInviteRequest.setPhoneNumber(userUpdateRequest.getPhoneNumber());
        userInviteRequest.setRoles(userUpdateRequest.getRoles());
        userInviteRequest.setGroups(userUpdateRequest.getGroups());
        log.info("Leaving buildUserInviteRequest() {}", userUpdateRequest);
        return userInviteRequest;
    }

    /**
     * Retrieves user profile information from the Cognito identity management
     * system based on the provided user ID or username and maps it to a
     * UserViewResponse object.
     * @param  userId          The unique identifier or username of the user to
     *                         retrieve the profile for.
     * @return                 A UserViewResponse object containing user profile
     *                         information, including email, user ID, phone number,
     *                         first name, and last name.
     * @throws DynamoException If there's an error during the retrieval process or
     *                         if the specified user ID is invalid.
     */
    public UserViewResponse retrieveUser(String userId) throws DynamoException {
        log.info("Entering retrieveUser() {}", userId);

        try {
            // retrieve user
            AdminGetUserResponse adminGetUserResponse = cognitoIdentityProviderClient
                    .adminGetUser(AdminGetUserRequest.builder().userPoolId(userPoolId).username(userId).build());
            List<AttributeType> userTypeList = adminGetUserResponse.userAttributes();

            UserViewResponse userViewResponse = new UserViewResponse();

            for (AttributeType attributeType : userTypeList) {
                if (attributeType.name().equalsIgnoreCase(UserAttributes.EMAIL.name)) {
                    userViewResponse.setEmail(attributeType.value());
                }

                if (attributeType.name().equalsIgnoreCase(UserAttributes.USER_ID.name)) {
                    userViewResponse.setUserId(UUID.fromString(attributeType.value()));
                }

                if (attributeType.name().equalsIgnoreCase(UserAttributes.PHONE_NUMBER.name)) {
                    userViewResponse.setPhoneNumber(attributeType.value());
                }

                if (attributeType.name().equalsIgnoreCase(UserAttributes.GIVEN_NAME.name)) {
                    userViewResponse.setFirstName(attributeType.value());
                }

                if (attributeType.name().equalsIgnoreCase(UserAttributes.FAMILY_NAME.name)) {
                    userViewResponse.setLastName(attributeType.value());
                }

                if (attributeType.name().equalsIgnoreCase(UserAttributes.PHONE_NUMBER.name)) {
                    userViewResponse.setPhoneNumber(attributeType.value());
                }

            }

            log.info("Leaving retrieveUser()");
            return userViewResponse;
        } catch (

        CognitoIdentityProviderException e) {
            log.error("Exception: {}", e.getLocalizedMessage());
            throw new DynamoException(e.awsErrorDetails().errorMessage(), HttpStatus.valueOf(e.statusCode()));
        } catch (SdkClientException e) {
            log.error("Exception: {}", e.getLocalizedMessage());
            throw new DynamoException(e.getMessage(), HttpStatus.BAD_REQUEST);
        }

    }

    /**
     * Disables a user account in the Cognito identity management system.
     * @param  userId          The unique identifier or username of the user whose
     *                         account will be disabled.
     * @throws DynamoException If there's an error during the account disabling
     *                         process or if the specified user ID is invalid.
     */
    public void disableUser(String userId) throws DynamoException {
        log.info("Entering disableUser()");

        try {
            // disable user
            cognitoIdentityProviderClient.adminDisableUser(
                    AdminDisableUserRequest.builder().userPoolId(userPoolId).username(userId).build());
        } catch (CognitoIdentityProviderException e) {
            log.error("Exception: {}", e.getLocalizedMessage());
            throw new DynamoException(e.awsErrorDetails().errorMessage(), HttpStatus.valueOf(e.statusCode()));
        } catch (SdkClientException e) {
            log.error("Exception: {}", e.getLocalizedMessage());
            throw new DynamoException(e.getMessage(), HttpStatus.BAD_REQUEST);
        }

        log.info("Leaving disableUser()");
    }

    /**
     * Enables a user account in the Cognito identity management system.
     * @param  userId          The unique identifier or username of the user whose
     *                         account will be enabled.
     * @throws DynamoException If there's an error during the account enabling
     *                         process or if the specified user ID is invalid.
     */
    public void enableUser(String userId) throws DynamoException {
        log.info("Entering enableUser()");

        try {
            // enable user
            cognitoIdentityProviderClient
                    .adminEnableUser(AdminEnableUserRequest.builder().userPoolId(userPoolId).username(userId).build());
        } catch (CognitoIdentityProviderException e) {
            log.error("Exception: {}", e.getLocalizedMessage());
            throw new DynamoException(e.awsErrorDetails().errorMessage(), HttpStatus.valueOf(e.statusCode()));
        } catch (SdkClientException e) {
            log.error("Exception: {}", e.getLocalizedMessage());
            throw new DynamoException(e.getMessage(), HttpStatus.BAD_REQUEST);
        }

        log.info("Leaving enableUser()");
    }

}
