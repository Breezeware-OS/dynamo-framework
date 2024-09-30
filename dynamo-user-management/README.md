# User Management with AWS Cognito - Spring Boot Documentation


## Overview

This documentation provides a comprehensive guide for managing user accounts in a Spring Boot application using AWS Cognito. AWS Cognito facilitates authentication, authorization, and user management, and this guide details the setup, configuration, and methods for integrating with AWS Cognito, ensuring effective user management.

## Prerequisites

- **Java 17.x**
- **Maven 3.8.x**
- **PostgreSQL 12.x**

## Dependencies

- **dynamo-generics-crud**: Provides generic CRUD operations.
- **springdoc-openapi-starter-webmvc-ui**: For OpenAPI documentation and Swagger UI integration.
- **dynamo-utils**: Utility functions for Dynamo.
- **Flyway**: For database migration.
- **AWS cognitoidentityprovider**: For managing user accounts with AWS Cognito.

## Dependency Management

- **dynamo-sdk-lib-bom**: BOM for managing Spring AI dependencies.

## Plugins

- **apt-maven-plugin**: For annotation processing.

## Configuration Properties

- **`aws.cognito.user-pool.id`**: The ID of the AWS Cognito user pool. Default: `${COGNITO_USER_POOL_ID:us-east-1_u1phSSzqL}`
- **`aws.region`**: The AWS region for Cognito services. Default: `${AWS_REGION:us-east-1}`
- **`spring.security.oauth2.resourceserver.jwt.jwk-set-uri`**: The URI to fetch the JSON Web Key Set (JWKS) for JWT validation. Formatted as: `https://cognito-idp.${aws.region}.amazonaws.com/${aws.cognito.user-pool.id}/.well-known/jwks.json`
- **`spring.security.oauth2.resourceserver.jwt.issuer-uri`**: The URI of the issuer for JWT validation. Formatted as: `https://cognito-idp.${aws.region}.amazonaws.com/${aws.cognito.user-pool.id}`

## Vocabulary/Terminology

- **BFF (Backend for Frontend)**: A specialized backend service tailored to meet the needs of a specific frontend.
- **DTO (Data Transfer Object)**: Objects used to transfer data between different parts of the application.
- **IDM (Identity Management)**: Systems and processes used to manage user identities and permissions.

## Technologies/3rd Party Libraries

- **Spring Web**: For building web applications.
- **OpenAPI (springdoc-openapi)**: For API documentation.
- **AWS Cognito**: For user authentication and management.
- **Flyway**: For database migrations.

## Sample Use Cases

- **User Invitation**: Send invitations to users to join the system using `inviteUser`.
- **User Account Setup**: Create and configure new user accounts using `setupUserAccount`.
- **User Details Update**: Update user information and manage account settings using `updateUser`.
- **User Retrieval**: Fetch user details by ID or email using `retrieveUser`.
- **User Activation/Suspension**: Manage user account status by suspending or activating users using `suspendUser` and `activateUser`.

## Modules

### Module: dynamo-user-svc

This module is responsible for storing user data and provides CRUD operations for various entities.

#### Services

**UserService**

Provides CRUD operations for user entities:

- **`create(T entity)`**: Creates a new user entity.
- **`saveAll(List<T> entities)`**: Saves a list of user entities.
- **`deleteAll(List<T> entities)`**: Deletes a list of user entities.
- **`retrieveById(Long id)`**: Retrieves a user entity by its unique identifier, returning an `Optional` that may be empty if not found.
- **`update(T updatedItem)`**: Updates an existing user entity with the provided data.

Methods:
- **`retrieveUser(UUID userId)`**: Fetches a user by their unique identifier.
- **`retrieveUser(String email)`**: Retrieves a user by their email address.
- **`retrieveUsersByGroup(List<String> groups)`**: Retrieves users associated with specified groups.
- **`retrieveUsersByRole(List<String> roles)`**: Retrieves users assigned specific roles.
- **`retrieveUserByIdmUserId(String idmUserId)`**: Retrieves a user by their IDM (Identity Management) unique identifier.
- **`createUserProfile(Object userData)`**: Creates a new user profile with the provided data.

**RoleService**

Handles CRUD operations for user roles:

- **`create(T entity)`**: Creates a new role entity.
- **`saveAll(List<T> entities)`**: Saves a list of role entities.
- **`deleteAll(List<T> entities)`**: Deletes a list of role entities.
- **`retrieveById(Long id)`**: Retrieves a role entity by its unique identifier, returning an `Optional` that may be empty if not found.
- **`update(T updatedItem)`**: Updates an existing role entity with the provided data.

Methods:
- **`retrieveRole(String roleName)`**: Fetches details of a specific role by name.

**UserRoleMapService**

Manages the mapping of users to roles:

- **`create(T entity)`**: Creates a new user-role mapping.
- **`saveAll(List<T> entities)`**: Saves a list of user-role mappings.
- **`deleteAll(List<T> entities)`**: Deletes a list of user-role mappings.
- **`retrieveById(Long id)`**: Retrieves a user-role mapping by its unique identifier, returning an `Optional` that may be empty if not found.
- **`update(T updatedItem)`**: Updates an existing user-role mapping with the provided data.

Methods:
- **`retrieveUserRoleMap(User user)`**: Retrieves the role mapping for a specific user.

**OrganizationService**

Handles CRUD operations for organizations:

- **`create(T entity)`**: Creates a new organization entity.
- **`saveAll(List<T> entities)`**: Saves a list of organization entities.
- **`deleteAll(List<T> entities)`**: Deletes a list of organization entities.
- **`retrieveById(Long id)`**: Retrieves an organization entity by its unique identifier, returning an `Optional` that may be empty if not found.
- **`update(T updatedItem)`**: Updates an existing organization entity with the provided data.

Methods:
- **`retrieveOrganization(String name)`**: Fetches details of an organization by its name.

**UserOrganizationMapService**

Manages the mapping of users to organizations:

- **`create(T entity)`**: Creates a new user-organization mapping.
- **`saveAll(List<T> entities)`**: Saves a list of user-organization mappings.
- **`deleteAll(List<T> entities)`**: Deletes a list of user-organization mappings.
- **`retrieveById(Long id)`**: Retrieves a user-organization mapping by its unique identifier, returning an `Optional` that may be empty if not found.
- **`update(T updatedItem)`**: Updates an existing user-organization mapping with the provided data.

Methods:
- **`retrieveUserOrganizationMap(User user)`**: Retrieves the organization mapping for a specific user.

**GroupService**

Handles CRUD operations for user groups:

- **`create(T entity)`**: Creates a new group entity.
- **`saveAll(List<T> entities)`**: Saves a list of group entities.
- **`deleteAll(List<T> entities)`**: Deletes a list of group entities.
- **`retrieveById(Long id)`**: Retrieves a group entity by its unique identifier, returning an `Optional` that may be empty if not found.
- **`update(T updatedItem)`**: Updates an existing group entity with the provided data.

Methods:
- **`retrieveGroup(String groupName)`**: Fetches details of a specific user group by name.

**UserGroupMapService**

Manages the mapping of users to groups:

- **`create(T entity)`**: Creates a new user-group mapping.
- **`saveAll(List<T> entities)`**: Saves a list of user-group mappings.
- **`deleteAll(List<T> entities)`**: Deletes a list of user-group mappings.
- **`retrieveById(Long id)`**: Retrieves a user-group mapping by its unique identifier, returning an `Optional` that may be empty if not found.
- **`update(T updatedItem)`**: Updates an existing user-group mapping with the provided data.

Methods:
- **`retrieveUserGroupMap(User user)`**: Retrieves the group mapping for a specific user.

**IdmInfoService**

Handles CRUD operations for IDM information:

- **`create(T entity)`**: Creates a new IDM info entity.
- **`saveAll(List<T> entities)`**: Saves a list of IDM info entities.
- **`deleteAll(List<T> entities)`**: Deletes a list of IDM info entities.
- **`retrieveById(Long id)`**: Retrieves an IDM info entity by its unique identifier, returning an `Optional` that may be empty if not found.
- **`update(T updatedItem)`**: Updates an existing IDM info entity with the provided data.

Methods:
- **`retrieveIdmInfo(String idmUniqueId)`**: Retrieves IDM information for a user by their unique ID.

### Module: dynamo-cognito-svc

Handles AWS Cognito operations for user management.

#### Services

**CognitoService**

Provides methods for managing user accounts in AWS Cognito:

- **`inviteUser(UserInviteRequest userInviteRequest) throws DynamoException`**: Sends an invitation to a user to join the system.
- **`setupUserAccount(UserAccountSetupRequest userAccountSetupRequest) throws DynamoException`**: Sets up a new user account based on the request.
- **`updateUser(UserUpdateRequest userUpdateRequest) throws DynamoException`**: Updates user details.
- **`updateInvitedUser(UserUpdateRequest userUpdateRequest) throws DynamoException`**: Updates details of a user who has been invited but not yet fully set up.
- **`retrieveUser(String userId) throws DynamoException`**: Retrieves user details from Cognito using their user ID.
- **`disableUser(String userId) throws DynamoException`**: Disables a user account.
- **`enableUser(String userId) throws DynamoException`**: Enables a previously disabled user account.

### Module: dynamo-usermanagement-web-bff

Manages user management operations through a backend-for-frontend service.

#### Services

**UserManagementService**

Provides methods for user management, including validation, setup, and synchronization with AWS Cognito:

- **`inviteUser(UserInviteRequest userInviteRequest) throws DynamoException`**: Sends an invitation to a user to join the system.
- **`setupUserAccount(UserAccountSetupRequest userAccountSetupRequest) throws DynamoException`**: Sets up a new user account based on the request.
- **`updateUser(UserUpdateRequest userUpdateRequest) throws DynamoException`**: Updates user details.
- **`retrieveUsers(MultiValueMap<String, String> searchOrFilterParameters, Pageable pageable) throws DynamoException`**: Retrieves a list of users based on search or filter parameters and pagination.
- **`retrieveUser(UUID userId) throws DynamoException`**: Retrieves user details by their unique identifier.
- **`retrieveUser(String idmUserId) throws DynamoException`**: Retrieves user details by their IDM unique identifier.
- **`suspendUser(UUID userId) throws DynamoException`**: Suspends a user account.
- **`activateUser(UUID userId) throws DynamoException`**: Activates a suspended user account.
- **`retrieveUsersByGroup(List<String> groups) throws DynamoException`**: Retrieves users associated with specified groups.
- **`retrieveUsersByRole(List<String> roles) throws DynamoException`**: Retrieves users assigned specific roles.
- **`createUser(SignedUpUserData signedUpUserData) throws DynamoException`**: Creates a new user based on the provided signed-up data.



## Summary

This documentation provides a structured approach to managing users in a Spring Boot application using AWS Cognito. It covers prerequisites, dependencies, configuration properties, and detailed explanations of methods used for user management. It also includes key terminology, technologies, and sample use cases to help integrate AWS Cognito effectively into your application. By following this guide, you can ensure a robust and scalable user management solution.

