package net.breezeware.dynamo.auth.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Response entity after successful OAuth Authentication. Contains details that
 * can be used to access resources.
 */
@Data
public class AuthenticationResponse {
    @JsonProperty("access_token")
    @Schema(example = "", description = "Access token to access secured API resources")
    private String accessToken;

    @JsonProperty("refresh_token")
    @Schema(example = "", description = "Refresh token used to get an access token, when access token expires")
    private String refreshToken;

    @JsonProperty("expires_in")
    @Schema(example = "3599", description = "Access token expires value in seconds")
    private int accessTokenExpiresIn;

    @Schema(example = "1234", description = "User's unique identifier.")
    private String userId;

    @Schema(example = "[\"User\",\"Admin\"]", description = "Roles associated with the user.")
    private List<String> roles;
}
