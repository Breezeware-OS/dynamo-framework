package net.breezeware.dynamo.usermanagent.service;

import java.time.DateTimeException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.MultiValueMap;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.querydsl.core.BooleanBuilder;

import net.breezeware.dynamo.usermanagement.cognito.service.CognitoService;
import net.breezeware.dynamo.usermanagement.dto.SignedUpUserData;
import net.breezeware.dynamo.usermanagement.dto.UserAccountSetupRequest;
import net.breezeware.dynamo.usermanagement.dto.UserInviteRequest;
import net.breezeware.dynamo.usermanagement.dto.UserUpdateRequest;
import net.breezeware.dynamo.usermanagement.dto.UserViewResponse;
import net.breezeware.dynamo.usermanagement.entity.Group;
import net.breezeware.dynamo.usermanagement.entity.IdmInfo;
import net.breezeware.dynamo.usermanagement.entity.Organization;
import net.breezeware.dynamo.usermanagement.entity.QUser;
import net.breezeware.dynamo.usermanagement.entity.Role;
import net.breezeware.dynamo.usermanagement.entity.User;
import net.breezeware.dynamo.usermanagement.entity.UserGroupMap;
import net.breezeware.dynamo.usermanagement.entity.UserOrganizationMap;
import net.breezeware.dynamo.usermanagement.entity.UserRoleMap;
import net.breezeware.dynamo.usermanagement.enumeration.UserStatus;
import net.breezeware.dynamo.usermanagement.events.UserCreated;
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
import net.breezeware.dynamo.utils.exception.ValidationExceptionUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserManagementService {

    private static final Set<String> defaultValidParameters = Set.of("page-no", "page-size", "sort", "search");

    private final Validator fieldValidator;
    private final CognitoService cognitoService;
    private final UserService userService;
    private final GroupService groupService;
    private final RoleService roleService;
    private final IdmInfoService idmInfoService;
    private final UserOrganizationMapService userOrganizationMapService;
    private final UserGroupMapService userGroupMapService;
    private final UserRoleMapService userRoleMapService;
    private final UserMapperService userMapperService;
    private final OrganizationService organizationService;
    private final ObjectMapper objectMapper;
    private final ApplicationEventPublisher applicationEventPublisher;

    /**
     * Invites a user to the system and performs various related tasks including
     * validation, user creation, association with an organization, groups, and
     * roles.
     * @param  userInviteRequest The request object containing user invitation
     *                           details.
     * @throws DynamoException   If there's an error during the invitation process,
     *                           field validation failure, or if the user or
     *                           identity management information is not found.
     */
    @Transactional
    public void inviteUser(UserInviteRequest userInviteRequest) throws DynamoException {
        log.info("Entering inviteUser() {}", userInviteRequest);

        // field constraint violation validation
        Set<ConstraintViolation<UserInviteRequest>> fieldViolations = fieldValidator.validate(userInviteRequest);
        // field constraint violation handling
        ValidationExceptionUtils.handleException(fieldViolations);

        // validate and retrieve user for who invited the user
        User invitedByUser = retrieveUserByUserId(userInviteRequest.getInvitedBy());

        // validate and retrieve organization for who invited the user
        Organization organization = userOrganizationMapService.retrieveUserOrganizationMap(invitedByUser)
                .orElseThrow(() -> new DynamoException(
                        "User '%s' does not have any organization".formatted(invitedByUser.getFirstName()),
                        HttpStatus.NOT_FOUND))
                .getOrganization();

        // validate and retrieve identity management information for who invited the
        // user
        IdmInfo idmInfo = idmInfoService.retrieveById(invitedByUser.getIdmInfo().getId())
                .orElseThrow(() -> new DynamoException(
                        "User '%s' does not have any Identity Management Information".formatted(invitedByUser),
                        HttpStatus.NOT_FOUND));

        // generate user unique id
        userInviteRequest.setUserId(UUID.randomUUID());

        // create user in cognito
        String idmUserId = cognitoService.inviteUser(userInviteRequest);

        // create new user
        User savedUser = buildAndPersistInvitedUser(userInviteRequest, idmUserId, idmInfo);

        // add organization for the user
        buildAndPersistUserOrganizationMap(savedUser, organization);

        // add groups for the user
        userInviteRequest.getGroups().forEach(groupName -> {
            Group group = retrieveGroup(groupName);
            buildAndPersistUserGroupMap(group, savedUser);
        });

        // add roles for the user
        userInviteRequest.getRoles().forEach(roleName -> {
            Role role = retrieveRole(roleName);
            buildAndPersistUserRoleMap(role, savedUser);
        });

        log.info("Leaving inviteUser()");
    }

    /**
     * Retrieves a user by their unique identifier.
     * @param  userId          The unique identifier of the user to retrieve.
     * @return                 The user with the specified unique identifier.
     * @throws DynamoException if the user is not found.
     */
    private User retrieveUserByUserId(UUID userId) {
        log.info("Entering retrieveUserByUserId() {}", userId);
        if (Objects.isNull(userId)) {
            throw new IllegalArgumentException("The User unique ID is null or empty.");
        }

        User user = userService.retrieveUser(userId)
                .orElseThrow(() -> new DynamoException("User with unique-id '%s' not found".formatted(userId),
                        HttpStatus.NOT_FOUND));
        log.info("Leaving retrieveUserByUserId()");
        return user;
    }

    /**
     * Retrieves a group by its name.
     * @param  groupName       The name of the group to retrieve.
     * @return                 The group with the specified name.
     * @throws DynamoException if the group is not found.
     */
    private Group retrieveGroup(String groupName) {
        log.info("Entering retrieveGroup() {}", groupName);
        if (Objects.isNull(groupName)) {
            throw new IllegalArgumentException("The Group name is null or empty.");
        }

        Group group = groupService.retrieveGroup(groupName).orElseThrow(
                () -> new DynamoException("Group with name '%s' not found".formatted(groupName), HttpStatus.NOT_FOUND));
        log.info("Leaving retrieveGroup()");
        return group;
    }

    /**
     * Retrieves a role by its name.
     * @param  roleName        The name of the role to retrieve.
     * @return                 The role with the specified name.
     * @throws DynamoException if the role is not found.
     */
    private Role retrieveRole(String roleName) {
        log.info("Entering retrieveRole() {}", roleName);
        if (Objects.isNull(roleName)) {
            throw new IllegalArgumentException("The Role name is null or empty.");
        }

        Role role = roleService.retrieveRole(roleName).orElseThrow(
                () -> new DynamoException("Role with name '%s' not found".formatted(roleName), HttpStatus.NOT_FOUND));
        log.info("Leaving retrieveRole()");
        return role;
    }

    /**
     * Builds and persists a new user based on an invitation request.
     * @param  userInviteRequest The invitation request containing user details.
     * @param  idmUserId         The IDM user ID associated with the new user.
     * @param  idmInfo           The IDM information associated with the new user.
     * @return                   The user has been created and persisted.
     */
    private User buildAndPersistInvitedUser(UserInviteRequest userInviteRequest, String idmUserId, IdmInfo idmInfo) {
        log.info("Entering buildAndPersistInvitedUser()");
        User user = new User();
        user.setUniqueId(userInviteRequest.getUserId());
        user.setIdmUserId(idmUserId);
        user.setIdmInfo(idmInfo);
        user.setEmail(userInviteRequest.getEmail());
        user.setStatus(UserStatus.INVITED.getStatus());
        User savedUser = userService.create(user);
        log.info("Leaving buildAndPersistInvitedUser() {}", user);
        return savedUser;
    }

    /**
     * Builds and persists a user-organization mapping, associating a user with an
     * organization.
     * @param user         The user to be associated with the organization.
     * @param organization The organization to which the user is being associated.
     */
    private void buildAndPersistUserOrganizationMap(User user, Organization organization) {
        log.info("Entering buildAndPersistUserOrganizationMap() {} {}", user, organization);
        UserOrganizationMap userOrganizationMap = new UserOrganizationMap();
        userOrganizationMap.setUser(user);
        userOrganizationMap.setOrganization(organization);
        userOrganizationMapService.create(userOrganizationMap);
        log.info("Leaving buildAndPersistUserOrganizationMap()");
    }

    /**
     * Builds and persists a User-Group mapping, associating a user with a group.
     * @param group The Group to be associated with the UserGroupMap.
     * @param user  The User to be associated with the UserGroupMap.
     */
    private void buildAndPersistUserGroupMap(Group group, User user) {
        log.info("Entering buildAndPersistUserGroupMap() {} {}", user, group);
        UserGroupMap userGroupMap = new UserGroupMap();
        userGroupMap.setGroup(group);
        userGroupMap.setUser(user);
        userGroupMapService.create(userGroupMap);
        log.info("Leaving buildAndPersistUserGroupMap()");
    }

    /**
     * Builds and persists a User-Role mapping, associating a user with a role.
     * @param role The Role to be associated with the UserRoleMap.
     * @param user The User to be associated with the UserRoleMap.
     */
    private void buildAndPersistUserRoleMap(Role role, User user) {
        log.info("Entering buildAndPersistUserRoleMap() {} {}", user, role);
        UserRoleMap userRoleMap = new UserRoleMap();
        userRoleMap.setRole(role);
        userRoleMap.setUser(user);
        userRoleMapService.create(userRoleMap);
        log.info("Leaving buildAndPersistUserRoleMap()");
    }

    /**
     * Performs user account setup, including validation, updating user account
     * information, and synchronizing the changes with the Cognito identity
     * management service.
     * @param  userAccountSetupRequest The request object containing user account
     *                                 setup details.
     * @throws DynamoException         If there's an error during the account setup
     *                                 process, field validation failure, or if the
     *                                 user with the specified email is not found.
     */
    @Transactional
    public void setupUserAccount(UserAccountSetupRequest userAccountSetupRequest) throws DynamoException {
        log.info("Entering setupUserAccount() {}", userAccountSetupRequest);
        // field constraint violation validation
        Set<ConstraintViolation<UserAccountSetupRequest>> fieldViolations =
                fieldValidator.validate(userAccountSetupRequest);
        // field constraint violation handling
        ValidationExceptionUtils.handleException(fieldViolations);

        // validate and retrieve user
        User user = userService.retrieveUser(userAccountSetupRequest.getEmail())
                .orElseThrow(() -> new DynamoException(
                        "User with email '%s' not found".formatted(userAccountSetupRequest.getEmail()),
                        HttpStatus.NOT_FOUND));

        // update user in database
        updateAndPersistUserAccount(userAccountSetupRequest, user);

        // update user in cognito
        cognitoService.setupUserAccount(userAccountSetupRequest);

        log.info("Leaving setupUserAccount()");
    }

    /**
     * Updates the user's email, first name, last name, phone number, and status
     * based on the provided user account setup request, then persists the updated
     * user.
     * @param userAccountSetupRequest The user account setup request containing
     *                                updated user information.
     * @param user                    The user to be updated and persisted.
     */
    private void updateAndPersistUserAccount(UserAccountSetupRequest userAccountSetupRequest, User user) {
        log.info("Entering updateAndPersistUserAccount() {} {}", userAccountSetupRequest, user);
        user.setEmail(userAccountSetupRequest.getEmail());
        user.setFirstName(userAccountSetupRequest.getFirstName());
        user.setLastName(userAccountSetupRequest.getLastName());
        user.setPhoneNumber(userAccountSetupRequest.getPhoneNumber());
        user.setStatus(UserStatus.ACTIVE.getStatus());
        userService.update(user);
        log.info("Leaving updateAndPersistUserAccount()");
    }

    /**
     * Updates user information including validation, updating user details in the
     * database, and synchronizing changes with the Cognito identity management
     * service. This method also handles group and role associations for the user.
     * @param  userUpdateRequest The request object containing user update details.
     * @throws DynamoException   If there's an error during the user update process,
     *                           field validation failure, or if there are issues
     *                           with group or role associations.
     */
    @Transactional
    public void updateUser(UserUpdateRequest userUpdateRequest) throws DynamoException {
        log.info("Entering updateUser() {}", userUpdateRequest);

        // field constraint violation validation
        Set<ConstraintViolation<UserUpdateRequest>> fieldViolations = fieldValidator.validate(userUpdateRequest);
        // field constraint violation handling
        ValidationExceptionUtils.handleException(fieldViolations);
        // validate non-invited user update request field
        validateUpdateUserRequest(userUpdateRequest);

        // validate and retrieve user
        User user = retrieveUserByUserId(userUpdateRequest.getUserId());

        // update user in database
        buildAndPersistUser(userUpdateRequest, user);

        // Retrieve user group maps
        List<UserGroupMap> retrievedUserGroupMaps = userGroupMapService.retrieveUserGroupMap(user);

        // create a map of group names to UserGroupMap objects for faster lookup
        Map<String, UserGroupMap> groupNameToUserGroupMap =
                retrievedUserGroupMaps.stream().collect(Collectors.toMap(map -> map.getGroup().getName(), map -> map));

        // check group name and size are equal
        boolean areGroupsEqual = userUpdateRequest.getGroups().stream().allMatch(groupNameToUserGroupMap::containsKey)
                && userUpdateRequest.getGroups().size() == groupNameToUserGroupMap.size();

        if (!areGroupsEqual) {
            // Delete user group maps that are not in the update request
            retrievedUserGroupMaps.forEach(userGroupMap -> {
                if (!userUpdateRequest.getGroups().contains(userGroupMap.getGroup().getName())) {
                    userGroupMapService.delete(userGroupMap.getId());
                }

            });

            // Create user group maps for new groups in the update request
            userUpdateRequest.getGroups().stream().filter(groupName -> !groupNameToUserGroupMap.containsKey(groupName))
                    .forEach(groupName -> {
                        Group group = retrieveGroup(groupName);
                        buildAndPersistUserGroupMap(group, user);
                    });
        }

        // retrieve user role maps
        List<UserRoleMap> retrievedUserRoleMaps = userRoleMapService.retrieveUserRoleMap(user);

        // create a map of role names to UserRoleMap objects for faster lookup
        Map<String, UserRoleMap> roleNameToUserRoleMap =
                retrievedUserRoleMaps.stream().collect(Collectors.toMap(map -> map.getRole().getName(), map -> map));

        // check role name and size are equal
        boolean areRolesEqual = userUpdateRequest.getRoles().stream().allMatch(roleNameToUserRoleMap::containsKey)
                && userUpdateRequest.getRoles().size() == roleNameToUserRoleMap.size();

        if (!areRolesEqual) {
            // delete user role maps that are not in the update request
            retrievedUserRoleMaps.forEach(userRoleMap -> {
                if (!userUpdateRequest.getRoles().contains(userRoleMap.getRole().getName())) {
                    userRoleMapService.delete(userRoleMap.getId());
                }

            });

            // create user role maps for new roles in the update request
            userUpdateRequest.getRoles().stream().filter(roleName -> !roleNameToUserRoleMap.containsKey(roleName))
                    .forEach(roleName -> {
                        Role role = retrieveRole(roleName);
                        buildAndPersistUserRoleMap(role, user);
                    });
        }

        // set cognito idm user id
        userUpdateRequest.setIdmUserId(user.getIdmUserId());

        // update user in cognito
        if (!userUpdateRequest.getStatus().equals(UserStatus.INVITED.getStatus())) {
            cognitoService.updateUser(userUpdateRequest);
        } else {
            String idmUserId = cognitoService.updateInvitedUser(userUpdateRequest);
            if (Objects.nonNull(idmUserId) && !idmUserId.isBlank()) {
                user.setIdmUserId(idmUserId);
                userService.update(user);
            }

        }

        log.info("Leaving updateUser()");

    }

    /**
     * Validates the given {@link UserUpdateRequest} object to ensure it meets the
     * required criteria for updating a user.
     * @param  userUpdateRequest The {@link UserUpdateRequest} to be validated.
     * @throws DynamoException   If the validation fails due to missing or invalid
     *                           data.
     */
    private void validateUpdateUserRequest(UserUpdateRequest userUpdateRequest) {
        log.info("Entering validateUpdateUserRequest() {}", userUpdateRequest);

        // validate user status
        UserStatus userStatus = UserStatus.retrieveUserStatus(userUpdateRequest.getStatus())
                .orElseThrow(() -> new DynamoException(
                        ("Invalid user status '%s' for user'. " + "Possible user status are: '%s'")
                                .formatted(userUpdateRequest.getUserId(), UserStatus.retrieveAllUserStatus()),
                        HttpStatus.BAD_REQUEST));

        if (!userStatus.equals(UserStatus.INVITED)) {
            if (Objects.isNull(userUpdateRequest.getFirstName()) || userUpdateRequest.getFirstName().isBlank()) {
                throw new DynamoException("The User first name is null or empty.", HttpStatus.BAD_REQUEST);
            }

            if (Objects.isNull(userUpdateRequest.getLastName()) || userUpdateRequest.getLastName().isBlank()) {
                throw new DynamoException("The User last name is null or empty.", HttpStatus.BAD_REQUEST);
            }

            if (Objects.isNull(userUpdateRequest.getPhoneNumber()) || userUpdateRequest.getPhoneNumber().isBlank()) {
                throw new DynamoException("The User phone number is null or empty.", HttpStatus.BAD_REQUEST);
            }

        }

        log.info("Leaving validateUpdateUserRequest()");
    }

    /**
     * Builds and persists user information based on the provided
     * {@link UserUpdateRequest}.
     * @param userUpdateRequest The {@link UserUpdateRequest} containing updated
     *                          user information.
     * @param user              The {@link User} entity to be updated.
     */
    private void buildAndPersistUser(UserUpdateRequest userUpdateRequest, User user) {
        log.info("Entering buildAndPersistUser() {} {}", userUpdateRequest, user);
        user.setEmail(userUpdateRequest.getEmail());
        user.setFirstName(userUpdateRequest.getFirstName());
        user.setLastName(userUpdateRequest.getLastName());
        user.setPhoneNumber(userUpdateRequest.getPhoneNumber());
        userService.update(user);
        log.info("Leaving buildAndPersistUser()");
    }

    /**
     * Retrieves a paginated list of user information based on search or filter
     * parameters and pageable criteria.
     * @param  searchOrFilterParameters A map containing search or filter parameters
     *                                  for user retrieval.
     * @param  pageable                 The pageable criteria for pagination and
     *                                  sorting.
     * @return                          A paginated list of UserViewResponse objects
     *                                  representing user information.
     * @throws DynamoException          If there's an error during the user
     *                                  retrieval process or if the input parameters
     *                                  are invalid.
     */
    public Page<UserViewResponse> retrieveUsers(MultiValueMap<String, String> searchOrFilterParameters,
            Pageable pageable) throws DynamoException {
        log.info("Entering retrieveUsers()");

        // validate query parameters
        Set<String> validParameters = new HashSet<>(defaultValidParameters);
        validParameters.add("role");
        validParameters.add("created-date");
        validParameters.add("status");
        validParameters.add("username");
        validateParameters(validParameters, searchOrFilterParameters.keySet());

        // validate sort criteria
        validateSortCriteria(searchOrFilterParameters.getFirst("sort"));

        // search filter predicate
        BooleanBuilder booleanBuilder = buildSearchOrFilterPredicate(searchOrFilterParameters);

        // retrieve paged user
        Page<User> pagedUsers = userService.retrievePageEntitiesWithPredicate(booleanBuilder, pageable);

        // convert Page of User to List of UserViewResponse
        List<UserViewResponse> userViewResponseList = pagedUsers.stream().map(user -> {
            List<String> roles = retrieveAndMapUserRoles(user);
            List<String> groups = retrieveAndMapUserGroups(user);
            UserViewResponse userViewResponse = userMapperService.userToUserViewResponse(user);
            userViewResponse.setRoles(roles);
            userViewResponse.setGroups(groups);
            return userViewResponse;
        }).toList();

        // convert List of UserViewResponse to Page of UserViewResponse
        PageImpl<UserViewResponse> pagedUserViewResponse =
                new PageImpl<>(userViewResponseList, pageable, pagedUsers.getTotalElements());

        log.info("Leaving retrieveUsers(), # of users: {}", pagedUserViewResponse.getTotalElements());

        return pagedUserViewResponse;
    }

    /**
     * Retrieves and maps the names of roles associated with a user.
     * @param  user The user for whom to retrieve and map role names.
     * @return      A list of role names associated with the user.
     */
    private List<String> retrieveAndMapUserRoles(User user) {
        log.info("Entering retrieveAndMapUserRoles() {}", user);

        List<String> userRoles = userRoleMapService.retrieveUserRoleMap(user).stream()
                .map(userRoleMap -> userRoleMap.getRole().getName()).toList();

        log.info("Leaving retrieveAndMapUserRoles() {}", userRoles);
        return userRoles;
    }

    /**
     * Retrieves and maps the names of groups associated with a user.
     * @param  user The user for whom to retrieve and map group names.
     * @return      A list of group names associated with the user.
     */
    private List<String> retrieveAndMapUserGroups(User user) {
        log.info("Entering retrieveAndMapUserGroups() {}", user);

        List<String> userGroups = userGroupMapService.retrieveUserGroupMap(user).stream()
                .map(userGroupMap -> userGroupMap.getGroup().getName()).toList();

        log.info("Leaving retrieveAndMapUserGroups() {}", userGroups);
        return userGroups;
    }

    /**
     * Validates that the actual parameters are among the allowed parameters.
     * @param  allowedParameters A Set of allowed parameter names.
     * @param  actualParameters  A Set of actual parameter names.
     * @throws DynamoException   if unknown parameters are found.
     */
    private void validateParameters(Set<String> allowedParameters, Set<String> actualParameters)
            throws DynamoException {
        log.info("Entering validateParameters() {} {}", allowedParameters, allowedParameters);
        List<String> invalidParameters =
                actualParameters.stream().filter(param -> !allowedParameters.contains(param)).toList();
        if (!invalidParameters.isEmpty()) {
            log.error("Parameter(s) {} not supported!", invalidParameters);
            throw new DynamoException("Unknown parameter(s) %s found".formatted(invalidParameters),
                    HttpStatus.BAD_REQUEST);
        }

        log.info("Leaving validateParameters()");
    }

    /**
     * Validates the sort criteria.
     * @param  sortCriteria    the sort criteria to be validated.
     * @throws DynamoException if there is an error in the sort criteria.
     */
    private void validateSortCriteria(String sortCriteria) {
        log.info("Entering validateSortCriteria() {}", sortCriteria);

        if (Objects.nonNull(sortCriteria) && !sortCriteria.isBlank()) {
            String[] sortSplit = sortCriteria.split(",", 2);
            if (sortSplit.length != 2) {
                throw new DynamoException(
                        "Invalid sort criteria '%s'. Should be something like 'name,ASC' or 'name,asc'"
                                .formatted(sortCriteria),
                        HttpStatus.BAD_REQUEST);
            }

            String sortBy = sortSplit[0].trim();
            String sortOrder = sortSplit[1].trim().toLowerCase();

            if (!sortOrder.equalsIgnoreCase("asc") && !sortOrder.equalsIgnoreCase("desc")) {
                throw new DynamoException("Invalid sort-order [%s] for sort-by [%s]".formatted(sortOrder, sortBy),
                        HttpStatus.BAD_REQUEST);
            }

        } else {
            log.info("No sort data provided!");
        }

        log.info("Leaving validateSortCriteria()");

    }

    /**
     * Builds a {@link BooleanBuilder} predicate based on the provided search
     * parameters.
     * @param  searchOrFilterParameters The search parameters.
     * @return                          The {@link BooleanBuilder} predicate.
     */
    private BooleanBuilder buildSearchOrFilterPredicate(MultiValueMap<String, String> searchOrFilterParameters)
            throws DynamoException {
        log.info("Entering buildSearchPredicate() {}", searchOrFilterParameters);

        // build predicate from the searchOrFilterParameters value
        BooleanBuilder booleanBuilder = new BooleanBuilder();
        QUser user = QUser.user;

        // filter by user role
        String filterByRole = searchOrFilterParameters.getFirst("role");
        if (Objects.nonNull(filterByRole) && !filterByRole.isBlank()) {
            log.info("Adding filter by user role predicate for value '{}'", filterByRole);
            booleanBuilder.and(user.userRoleMap.any().role.name.containsIgnoreCase(filterByRole));
        }

        // filter by status
        String filterByStatus = searchOrFilterParameters.getFirst("status");
        if (Objects.nonNull(filterByStatus) && !filterByStatus.isBlank()) {
            log.info("Adding filter by user status predicate for value '{}'", filterByStatus);
            UserStatus userStatus =
                    UserStatus.retrieveUserStatus(filterByStatus)
                            .orElseThrow(() -> new DynamoException(
                                    "Invalid status '%s' for user. Possible status values are: '%s'"
                                            .formatted(filterByStatus, UserStatus.retrieveAllUserStatus()),
                                    HttpStatus.BAD_REQUEST));

            if (userStatus.equals(UserStatus.ACTIVE)) {
                booleanBuilder.and(user.status.equalsIgnoreCase(UserStatus.ACTIVE.getStatus()));
            } else if (userStatus.equals(UserStatus.SUSPENDED)) {
                booleanBuilder.and(user.status.equalsIgnoreCase(UserStatus.SUSPENDED.getStatus()));
            } else if (userStatus.equals(UserStatus.INVITED)) {
                booleanBuilder.and(user.status.equalsIgnoreCase(UserStatus.INVITED.getStatus()));
            }

        }

        // Filter by user name
        String filterByUsername = searchOrFilterParameters.getFirst("username");
        if (Objects.nonNull(filterByUsername) && !filterByUsername.isBlank()) {
            log.info("Adding filter by username predicate for value '{}'", filterByUsername);

            // Create a custom query using SQL-like syntax
            String fullNameQuery = "%" + filterByUsername + "%";

            booleanBuilder.and(user.firstName.concat(" ").concat(user.lastName).likeIgnoreCase(fullNameQuery));
        }

        // filter by user created date
        String createdDateValue = searchOrFilterParameters.getFirst("created-date");
        if (Objects.nonNull(createdDateValue) && !createdDateValue.isBlank()) {

            try {
                Instant startDate = Instant.parse(createdDateValue).truncatedTo(ChronoUnit.DAYS);
                booleanBuilder.and(user.createdOn.goe(startDate))
                        .and(user.createdOn.lt(startDate.plus(1, ChronoUnit.DAYS)));
            } catch (DateTimeException e) {
                log.error("Error processing start date: {}", createdDateValue);
                throw new DynamoException(e.getMessage(), HttpStatus.BAD_REQUEST);
            }

        }

        log.info("Leaving buildSearchPredicate()");
        return booleanBuilder;
    }

    /**
     * Retrieves user information by their unique user ID and maps it to a
     * UserViewResponse object, including associated user roles and groups.
     * @param  userId          The unique identifier of the user to retrieve.
     * @return                 A UserViewResponse object representing the user's
     *                         information, including roles and groups.
     * @throws DynamoException If there's an error during user retrieval or if the
     *                         specified user ID is invalid.
     */
    public UserViewResponse retrieveUser(UUID userId) throws DynamoException {
        log.info("Entering retrieveUser() {}", userId);

        // validate and retrieve user
        User user = retrieveUserByUserId(userId);

        // map User to UserViewResponse
        UserViewResponse userViewResponse = userMapperService.userToUserViewResponse(user);

        // retrieve and set user roles and groups
        List<String> roles = retrieveAndMapUserRoles(user);
        List<String> groups = retrieveAndMapUserGroups(user);
        userViewResponse.setRoles(roles);
        userViewResponse.setGroups(groups);

        log.info("Leaving retrieveUser()");

        return userViewResponse;
    }

    public UserViewResponse retrieveUser(String idmUserId) throws DynamoException {
        log.info("Entering retrieveUser() {}", idmUserId);

        // validate and retrieve user
        User user = userService.retrieveUserByIdmUserId(idmUserId)
                .orElseThrow(() -> new DynamoException("No User found for IDM User ID: %s".formatted(idmUserId),
                        HttpStatus.NOT_FOUND));

        // map User to UserViewResponse
        UserViewResponse userViewResponse = userMapperService.userToUserViewResponse(user);

        // retrieve and set user roles and groups
        List<String> roles = retrieveAndMapUserRoles(user);
        List<String> groups = retrieveAndMapUserGroups(user);
        userViewResponse.setRoles(roles);
        userViewResponse.setGroups(groups);

        log.info("Leaving retrieveUser()");

        return userViewResponse;
    }

    /**
     * Suspends a user by updating their status to "SUSPENDED" in the database and
     * disabling their account in Cognito.
     * @param  userId          The unique identifier of the user to be suspended.
     * @throws DynamoException If there's an error during the suspension process or
     *                         if the specified user ID is invalid.
     */
    @Transactional
    public void suspendUser(UUID userId) throws DynamoException {
        log.info("Entering suspendUser() {}", userId);

        updateUserStatus(userId, UserStatus.SUSPENDED);

        log.info("Leaving suspendUser()");
    }

    /**
     * Activates a previously suspended user by updating their status to "ACTIVE" in
     * the database and enabling their account in Cognito.
     * @param  userId          The unique identifier of the user to be activated.
     * @throws DynamoException If there's an error during the activation process or
     *                         if the specified user ID is invalid.
     */
    @Transactional
    public void activateUser(UUID userId) throws DynamoException {
        log.info("Entering activateUser() {}", userId);

        updateUserStatus(userId, UserStatus.ACTIVE);

        log.info("Leaving activateUser()");
    }

    /**
     * Updates the status of a user, either activating or suspending them, in both
     * the database and the associated Cognito account.
     * @param  userId          The unique identifier of the user whose status is to
     *                         be updated.
     * @param  userStatus      The new status to set for the user (e.g., active or
     *                         suspended).
     * @throws DynamoException If there's an error during the status update process
     *                         or if the specified user ID is invalid.
     */
    private void updateUserStatus(UUID userId, UserStatus userStatus) throws DynamoException {
        log.info("Entering updateUserStatus() {} {}", userId, userStatus);

        // validate and retrieve user
        User user = retrieveUserByUserId(userId);

        // update user status in db
        user.setStatus(userStatus.getStatus());
        User updatedUser = userService.update(user);

        // enable/disable in cognito
        if (userStatus.equals(UserStatus.ACTIVE)) {
            cognitoService.enableUser(updatedUser.getIdmUserId());
        } else {
            cognitoService.disableUser(updatedUser.getIdmUserId());
        }

        log.info("Leaving updateUserStatus()");
    }

    /**
     * Retrieves a list of users based on the provided list of group names. This
     * method validates each group name and then queries the user service to
     * retrieve users belonging to the specified groups.
     * @param  groups          A list of strings representing group names to filter
     *                         users. Must not be null. Each group name should be a
     *                         non-empty string.
     * @return                 A List of User objects matching the provided group
     *                         names. An empty list is returned if no users are
     *                         found for the specified groups.
     * @throws DynamoException If there is an error during the retrieval process, a
     *                         DynamoException is thrown. Possible error scenarios
     *                         include invalid group names or issues with the user
     *                         service.
     */
    public List<User> retrieveUsersByGroup(List<String> groups) throws DynamoException {
        log.info("Entering retrieveUsersByGroup() {}", groups);

        // validate group
        groups.forEach(this::retrieveGroup);

        // retrieve users
        List<User> users = userService.retrieveUsersByGroup(groups);
        log.info("Leaving retrieveUsersByGroup() size = {}", users.size());
        return users;
    }

    /**
     * Retrieves a list of users based on the provided list of role names. This
     * method validates each role name and then queries the user service to retrieve
     * users belonging to the specified roles.
     * @param  roles           A list of strings representing role names to filter
     *                         users. Must not be null. Each role name should be a
     *                         non-empty string.
     * @return                 A List of User objects matching the provided group
     *                         names. An empty list is returned if no users are
     *                         found for the specified roles.
     * @throws DynamoException If there is an error during the retrieval process, a
     *                         DynamoException is thrown. Possible error scenarios
     *                         include invalid group names or issues with the user
     *                         service.
     */
    public List<User> retrieveUsersByRole(List<String> roles) throws DynamoException {
        log.info("Entering retrieveUsersByRole() {}", roles);

        // validate group
        roles.forEach(this::retrieveRole);

        // retrieve users
        List<User> users = userService.retrieveUsersByRole(roles);
        log.info("Leaving retrieveUsersByRole() size = {}", users.size());
        return users;
    }

    /**
     * Creates a user after successful signup from the IDM.
     * @param  signedUpUserData {@link SignedUpUserData}
     * @return                  {@link User}
     * @throws DynamoException  If there is an error during the user creation
     *                          process.
     */
    @Transactional
    public User createUser(SignedUpUserData signedUpUserData) throws DynamoException {
        log.info("Entering createUser()");

        // NOTE: this whole function is based on the assumption that Idm, Organization,
        // Groups & Roles already exists

        IdmInfo idmInfo = idmInfoService.retrieveIdmInfo(signedUpUserData.getIdmId())
                .orElseThrow(() -> new DynamoException(
                        "No IdmInfo found for unique-id: %s".formatted(signedUpUserData.getIdmId()),
                        HttpStatus.BAD_REQUEST));

        // build user from signed up user data
        final User user = userMapperService.signedUpUserDataToUser(signedUpUserData, idmInfo);
        user.setUniqueId(UUID.randomUUID());

        // set additional info
        Map<String, Object> additionalInfo = new HashMap<>();
        additionalInfo.put("middleName", signedUpUserData.getMiddleName());
        additionalInfo.put("isEmailVerified", signedUpUserData.getIsEmailVerified());

        try {
            user.setAdditionalInfo(objectMapper.writeValueAsString(additionalInfo));
        } catch (JsonProcessingException e) {
            log.error("Error while serializing additionalInfo, error: {}", e.getMessage());
            throw new DynamoException("Error while serializing additionalInfo", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        User savedUser = userService.create(user);

        // build a user organization map
        String organizationName = signedUpUserData.getOrganization();
        Organization organization = organizationService.retrieveOrganization(organizationName).orElseThrow(
                () -> new DynamoException("No organization with name: %s found".formatted(organizationName),
                        HttpStatus.BAD_REQUEST));
        // add organization for the user
        buildAndPersistUserOrganizationMap(savedUser, organization);

        // build a user role map
        List<String> roles = signedUpUserData.getRoles();
        roles.forEach(r -> {
            Role role = roleService.retrieveRole(r).orElseThrow(
                    () -> new DynamoException("No role with name: %s found".formatted(r), HttpStatus.BAD_REQUEST));
            buildAndPersistUserRoleMap(role, savedUser);
        });

        // build a user group map
        List<String> groups = signedUpUserData.getGroups();
        groups.forEach(g -> {
            Group group = groupService.retrieveGroup(g).orElseThrow(
                    () -> new DynamoException("No group with name: %s found".formatted(g), HttpStatus.BAD_REQUEST));
            buildAndPersistUserGroupMap(group, savedUser);
        });

        // publish user created event
        applicationEventPublisher.publishEvent(UserCreated.builder().userId(savedUser.getUniqueId()).build());

        log.info("Leaving createUser()");
        return savedUser;
    }
}
