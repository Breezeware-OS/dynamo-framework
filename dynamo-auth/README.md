# Dynamo Auth Module


## Overview

The **Dynamo Auth Module** is responsible for handling authentication and authorization within the Dynamo platform. It provides security configurations, CORS settings, and JWT token processing to ensure secure access to the application. The module integrates with OAuth2 and JWT standards to manage access control and authentication. Additionally, it supports OAuth-based authentication services and password reset functionalities, integrating with FusionAuth and Active Directory.

## Prerequisites

- **Java 17.x**
- **Maven 3.8.x**
- **Spring Boot 3.x**
- **OAuth2 and JWT compatible identity provider**

## Relevant Dependencies, Plugins, and Properties

### Dependencies

- **`spring-boot-starter-security`**: Spring Security for authentication and authorization.
- **`spring-boot-starter-web`**: Spring Web for building web applications.
- **`spring-boot-starter-oauth2-resource-server`**: OAuth2 resource server support.
- **`nimbus-jose-jwt`**: JWT processing library.

### Plugins

- **`apt-maven-plugin`**: Annotation processing.

### Properties

- **`spring.security.oauth2.resourceserver.jwt.jwk-set-uri`**: URI to retrieve the JSON Web Key Set (JWKS).
- **`dynamo.auth.cors.allowed-origins`**: List of allowed origins for CORS.
- **`dynamo.auth.cors.allowed-methods`**: List of allowed HTTP methods for CORS.
- **`dynamo.auth.cors.allowed-headers`**: List of allowed headers for CORS.
- **`dynamo.auth.cors.exposed-headers`**: List of headers exposed to the client.

## Configuration

### CORS Configuration

The `DynamoHttpConfiguration` class is responsible for setting up the CORS configuration for the application. It allows you to specify the allowed origins, methods, and headers.

### Spring Security Configuration

The **Dynamo Auth Module** integrates with Spring Security to provide comprehensive authentication and authorization mechanisms. This includes setting up security filters, configuring JWT token processing, and handling OAuth2 authentication flows.

The security configuration involves:

1. **JWT Token Processing**: Ensuring secure token validation and authentication using JWT.
2. **OAuth2 Resource Server Setup**: Configuring the application as an OAuth2 resource server to handle token validation and access control.
3. **Security Filters**: Applying security filters to enforce authentication and authorization rules across the application.

## OAuth Authentication and Password Reset Services

The module also provides an OAuth-based authentication service along with a password reset service. These services support integration with FusionAuth and Active Directory, allowing for flexible and secure user management.

### Key Features:
- **OAuth Authentication**: Authenticate users using OAuth with multiple flows and scopes.
- **Password Reset**: Support for generating and validating OTP for password resets.
- **Token Management**: Handling refresh tokens and secure storage in cookies.
- **Logout**: A simple method to handle user logout by clearing refresh tokens.
- **Support for FusionAuth and Active Directory**: Easily configurable for both FusionAuth and Active Directory.

### Authentication Service

The `OAuthAuthenticationService` interface provides methods to authenticate users and manage OAuth tokens.

### Methods:

- **`authenticateUser(loginId, password)`**: Authenticate a user with login ID and password.
- **`authenticateUser(loginId, password, scope, clientId)`**: Authenticate a user with additional OAuth scope and client ID.
- **`authenticateUserAndStoreTokenInCookie(httpServletResponse, loginId, password)`**: Authenticate a user and store the refresh token in a cookie.
- **`authenticateUserAndStoreTokenInCookie(httpServletResponse, loginId, password, scope, clientId)`**: Authenticate a user with additional OAuth scope and client ID, storing the refresh token in a cookie.
- **`rotateRefreshToken(refreshToken)`**: Refresh the OAuth access token using a refresh token.
- **`rotateRefreshTokenAndStoreTokenInCookie(httpServletResponse, refreshToken)`**: Refresh the OAuth access token and store the refresh token in a cookie.
- **`logoutUser(httpServletResponse)`**: Logout the user by clearing the refresh token from cookies.

These services are designed to work seamlessly with both FusionAuth and Active Directory, providing robust and secure authentication mechanisms for the Dynamo platform.

## Summary

The **Dynamo Auth Module** offers a comprehensive solution for managing authentication and authorization in the Dynamo platform. By integrating Spring Security, OAuth2, JWT, and supporting FusionAuth and Active Directory, it ensures a secure and flexible environment for user access management.

