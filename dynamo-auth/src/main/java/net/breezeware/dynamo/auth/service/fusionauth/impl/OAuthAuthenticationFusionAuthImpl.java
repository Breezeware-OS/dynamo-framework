package net.breezeware.dynamo.auth.service.fusionauth.impl;

import java.text.ParseException;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import com.nimbusds.jwt.SignedJWT;

import net.breezeware.dynamo.auth.dto.AuthenticationResponse;
import net.breezeware.dynamo.auth.service.api.OAuthAuthenticationService;
import net.breezeware.dynamo.auth.service.fusionauth.exception.FusionAuthError;
import net.breezeware.dynamo.auth.service.fusionauth.properties.FusionAuthProperties;
import net.breezeware.dynamo.utils.exception.DynamoException;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@Profile("fusionauth")
public class OAuthAuthenticationFusionAuthImpl implements OAuthAuthenticationService {
    @Autowired
    FusionAuthProperties fusionAuthProperties;

    @Override
    public AuthenticationResponse authenticateUser(String loginId, String password) throws DynamoException {
        log.info("Entering authenticateUser()");
        if (loginId == null || loginId.isEmpty() || loginId.isBlank()) {
            throw new DynamoException("Invalid login id.", HttpStatus.BAD_REQUEST);
        }

        if (password == null || password.isEmpty() || password.isBlank()) {
            throw new DynamoException("Invalid password.", HttpStatus.BAD_REQUEST);
        }

        AuthenticationResponse authenticationResponse = buildAuthenticationRequest(loginId, password);
        if (authenticationResponse == null || authenticationResponse.getAccessToken().isEmpty()
                || authenticationResponse.getAccessToken().isBlank()) {
            throw new DynamoException("Something went wrong.Try again", HttpStatus.BAD_REQUEST);
        }

        Map<String, Object> claims = retrieveClaims(authenticationResponse.getAccessToken());
        List<String> roles = (List<String>) claims.get("roles");
        authenticationResponse.setRoles(roles);
        log.info("Leaving authenticateUser()");
        return authenticationResponse;
    }

    /**
     * Builds Login Request from login credentials.
     * @param  loginId  Provided user's login identifier.
     * @param  password Provided user's password.
     * @return          returns {@link AuthenticationResponse}.
     */
    private AuthenticationResponse buildAuthenticationRequest(String loginId, String password) {
        log.info("Entering buildAuthenticationRequest()");
        MultiValueMap<String, String> authenticationHeaders = new HttpHeaders();
        authenticationHeaders.add("username", loginId);
        authenticationHeaders.add("password", password);
        authenticationHeaders.add("grant_type", "password");
        authenticationHeaders.add("client_id", fusionAuthProperties.getClientId());
        authenticationHeaders.add("client_secret", fusionAuthProperties.getClientSecret());
        authenticationHeaders.add("scope", "offline_access");
        AuthenticationResponse authenticationResponse = authenticate(authenticationHeaders);
        log.info("Leaving buildAuthenticationRequest()");
        return authenticationResponse;
    }

    /**
     * Returns token from Azure Active directory,if request data are valid or else
     * throws Exception.
     * @param  authenticationHeaders the authentication header values.
     * @return                       returns {@link AuthenticationResponse} it
     *                               contains JWt tokens.
     */
    private AuthenticationResponse authenticate(MultiValueMap<String, String> authenticationHeaders) {
        log.info("Entering authenticate()");
        WebClient webClient = WebClient.create();
        AuthenticationResponse authenticationResponse = webClient.post().uri(fusionAuthProperties.getTokenUrl())
                .header("Authorization",
                        "Basic " + Base64.getEncoder()
                                .encodeToString((authenticationHeaders.get("client_id").get(0) + ":"
                                        + authenticationHeaders.get("client_secret").get(0)).getBytes()))
                .body(BodyInserters.fromFormData(authenticationHeaders)).accept(MediaType.APPLICATION_JSON).retrieve()
                .onStatus(HttpStatusCode::isError,
                        clientResponse -> clientResponse.bodyToMono(FusionAuthError.class)
                                .handle((error,
                                        sink) -> sink.error(new DynamoException(error.getErrorDescription(),
                                                HttpStatus.valueOf(clientResponse.statusCode().value())))))
                .bodyToMono(AuthenticationResponse.class).block();
        log.info("Leaving authenticate()");
        return authenticationResponse;
    }

    /**
     * Retrieve claims from authentication token.
     * @param  accessToken     Access token to access secured API resources.
     * @return                 Claims retrieved from access token.
     * @throws DynamoException throws exception if access token could not be parsed.
     */
    private Map<String, Object> retrieveClaims(String accessToken) throws DynamoException {
        try {
            SignedJWT signedJwt = SignedJWT.parse(accessToken);
            Map<String, Object> claims = signedJwt.getJWTClaimsSet().getClaims();
            return claims;
        } catch (ParseException e) {
            throw new DynamoException(e.getMessage(), HttpStatus.BAD_REQUEST);
        }

    }

    @Override
    public AuthenticationResponse authenticateUser(String loginId, String password, String scope, String clientId)
            throws DynamoException {
        throw new DynamoException("Method not implemented.", HttpStatus.NOT_IMPLEMENTED);
    }

    @Override
    public AuthenticationResponse authenticateUserAndStoreTokenInCookie(HttpServletResponse httpServletResponse,
            String loginId, String password) throws DynamoException {
        log.info("Entering authenticateUserAndStoreTokenInCookie()");
        AuthenticationResponse authenticationResponse = authenticateUser(loginId, password);
        storeRefreshTokenInCookie(httpServletResponse, authenticationResponse.getRefreshToken());
        authenticationResponse.setRefreshToken("");
        log.info("Leaving authenticateUserAndStoreTokenInCookie()");
        return authenticationResponse;
    }

    @Override
    public AuthenticationResponse authenticateUserAndStoreTokenInCookie(HttpServletResponse httpServletResponse,
            String loginId, String password, String scope, String clientId) throws DynamoException {
        throw new DynamoException("Method not implemented.", HttpStatus.NOT_IMPLEMENTED);
    }

    /**
     * Stores refresh token in the cookie.
     * @param  httpServletResponse {@link HttpServletResponse}
     * @param  refreshToken        Refresh token used to get an access token, when
     *                             access token expires.
     * @throws DynamoException     Throws exception when provided credentials are
     *                             invalid.
     */
    private void storeRefreshTokenInCookie(HttpServletResponse httpServletResponse, String refreshToken)
            throws DynamoException {
        log.info("Entering storeRefreshTokenInCookie()");
        // create a cookie
        Cookie cookie = new Cookie("refresh_token", refreshToken);
        cookie.setPath("/");

        // expires in 5 mins
        cookie.setMaxAge(300);

        // add cookie to response
        httpServletResponse.addCookie(cookie);
        log.info("Leaving storeRefreshTokenInCookie()");
    }

    @Override
    public AuthenticationResponse rotateRefreshToken(String refreshToken) throws DynamoException {
        log.info("Entering refreshTokenRotation()");
        if (refreshToken == null || refreshToken.isEmpty() || refreshToken.isBlank()) {
            throw new DynamoException("Invalid refresh token.", HttpStatus.BAD_REQUEST);
        }

        MultiValueMap<String, String> authenticationHeaders = new HttpHeaders();
        authenticationHeaders.add("grant_type", "refresh_token");
        authenticationHeaders.add("client_id", fusionAuthProperties.getClientId());
        authenticationHeaders.add("client_secret", fusionAuthProperties.getClientSecret());
        authenticationHeaders.add("refresh_token", refreshToken);

        AuthenticationResponse authenticationResponse = authenticate(authenticationHeaders);
        if (authenticationResponse == null || authenticationResponse.getAccessToken().isEmpty()
                || authenticationResponse.getAccessToken().isBlank()) {
            throw new DynamoException("Something went wrong.Try again", HttpStatus.BAD_REQUEST);
        }

        Map<String, Object> claims = retrieveClaims(authenticationResponse.getAccessToken());
        List<String> roles = (List<String>) claims.get("roles");
        authenticationResponse.setRoles(roles);
        log.info("Leaving refreshTokenRotation()");
        return authenticationResponse;
    }

    @Override
    public AuthenticationResponse rotateRefreshTokenAndStoreTokenInCookie(HttpServletResponse httpServletResponse,
            String refreshToken) throws DynamoException {
        log.info("Entering authenticateUserAndStoreTokenInCookie()");
        AuthenticationResponse authenticationResponse = rotateRefreshToken(refreshToken);
        storeRefreshTokenInCookie(httpServletResponse, authenticationResponse.getRefreshToken());
        authenticationResponse.setRefreshToken("");
        log.info("Leaving authenticateUserAndStoreTokenInCookie()");
        return authenticationResponse;
    }
}
