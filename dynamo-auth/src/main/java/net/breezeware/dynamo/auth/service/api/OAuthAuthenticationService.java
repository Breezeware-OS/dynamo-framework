package net.breezeware.dynamo.auth.service.api;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.breezeware.dynamo.auth.dto.AuthenticationResponse;
import net.breezeware.dynamo.utils.exception.DynamoException;

/**
 * Provides Oauth2 authentication API.
 */
public interface OAuthAuthenticationService {
    Logger log = LoggerFactory.getLogger(OAuthAuthenticationService.class);

    /**
     * Authenticates user with provided valid login id and password returns
     * {@link AuthenticationResponse} . otherwise throws exception.
     * @param  loginId         Provided user's login identifier.
     * @param  password        Provided user's password.
     * @return                 returns {@link AuthenticationResponse}.
     * @throws DynamoException throws an exception if the provided login id and
     *                         password are invalid.
     */
    AuthenticationResponse authenticateUser(String loginId, String password) throws DynamoException;

    /**
     * Authenticates user with provided valid login id ,password,scope and login id
     * returns {@link AuthenticationResponse} . otherwise throws exception.
     * @param  loginId         Provided user's login identifier.
     * @param  password        Provided user's password.
     * @param  scope           Provided scope.
     * @param  clientId        Provided client identifier.
     * @return                 returns {@link AuthenticationResponse}.
     * @throws DynamoException throws an exception if the provided login id and
     *                         password are invalid.
     */
    AuthenticationResponse authenticateUser(String loginId, String password, String scope, String clientId)
            throws DynamoException;

    /**
     * Authenticates user with provided valid login id and password ,and stores
     * refresh token in the cookie ,returns {@link AuthenticationResponse} .
     * otherwise throws exception.
     * @param  httpServletResponse {@link HttpServletResponse}
     * @param  loginId             Provided user's login identifier.
     * @param  password            Provided user's password.
     * @return                     returns {@link AuthenticationResponse}.
     * @throws DynamoException     throws an exception if the provided login id and
     *                             password are invalid.
     */
    AuthenticationResponse authenticateUserAndStoreTokenInCookie(HttpServletResponse httpServletResponse,
            String loginId, String password) throws DynamoException;

    /**
     * Authenticates user with provided valid login id ,password,scope and login id
     * returns {@link AuthenticationResponse} ,and stores refresh token in the
     * cookie. otherwise throws exception.
     * @param  httpServletResponse {@link HttpServletResponse}
     * @param  loginId             Provided user's login identifier.
     * @param  password            Provided user's password.
     * @param  scope               Provided scope.
     * @param  clientId            Provided client identifier.
     * @return                     returns {@link AuthenticationResponse}.
     * @throws DynamoException     throws an exception if the provided login id and
     *                             password are invalid.
     */
    AuthenticationResponse authenticateUserAndStoreTokenInCookie(HttpServletResponse httpServletResponse,
            String loginId, String password, String scope, String clientId) throws DynamoException;

    /**
     * OAuth's Token API, refresh token grant flow to rotate the access token.
     * @param  refreshToken    A refresh token is a credential artifact that lets a
     *                         client application get new access tokens without
     *                         having to ask the user to log in again.
     * @return                 returns {@link AuthenticationResponse}.
     * @throws DynamoException throws an exception if the provided refresh token is
     *                         invalid.
     */
    AuthenticationResponse rotateRefreshToken(String refreshToken) throws DynamoException;

    /**
     * OAuth's Token API, refresh token grant flow to rotate the access token and
     * stores refresh token in the cookie.
     * @param  httpServletResponse {@link HttpServletResponse}
     * @param  refreshToken        A refresh token is a credential artifact that
     *                             lets a client application get new access tokens
     *                             without having to ask the user to log in again.
     * @return                     returns {@link AuthenticationResponse}.
     * @throws DynamoException     throws an exception if the provided refresh token
     *                             is invalid.
     */
    AuthenticationResponse rotateRefreshTokenAndStoreTokenInCookie(HttpServletResponse httpServletResponse,
            String refreshToken) throws DynamoException;

    /**
     * Handles logout request,if the user uses
     * {@link #authenticateUserAndStoreTokenInCookie(HttpServletResponse, String,String)}
     * method for authenticating the user.
     * @param httpServletResponse {@link HttpServletResponse}
     */
    default void logoutUser(HttpServletResponse httpServletResponse) {
        log.info("Entering logoutUser()");
        // create a cookie
        Cookie cookie = new Cookie("refresh_token", "");
        cookie.setPath("/");

        // set age value to zero(0)
        cookie.setMaxAge(0);

        // add cookie to response
        httpServletResponse.addCookie(cookie);
        log.info("Leaving logoutUser()");
    }
}
