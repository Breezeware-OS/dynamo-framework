package net.breezeware.dynamo.auth.dto;

import lombok.Data;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Authenticated user details.
 */
@Data
public class AuthUser {

    /**
     * User's firstName.
     */
    @Schema(example = "John", description = "User's firstName.")
    private String firstName;

    /**
     * User's lastName.
     */
    @Schema(example = "Doe", description = "User's lastName")
    private String lastName;

    /**
     * User's phone number.
     */
    @Schema(example = "123456789", description = "User's phone number.")
    private String phoneNumber;

    /**
     * User's email.
     */
    @Schema(example = "johnDoe@gmail.com", description = "User's email.")
    private String email;

}
