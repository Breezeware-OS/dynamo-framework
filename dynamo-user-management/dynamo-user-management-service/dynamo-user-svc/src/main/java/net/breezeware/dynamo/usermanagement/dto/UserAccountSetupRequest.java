package net.breezeware.dynamo.usermanagement.dto;

import jakarta.validation.constraints.NotBlank;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Represents a request for user account setup, including user meta information
 * and password.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class UserAccountSetupRequest {

    /**
     * The email address of the user.
     */
    @Schema(example = "john.doe@example.com", description = "Email address of the user.")
    @NotBlank(message = "User's email is missing or blank")
    private String email;

    /**
     * The first name of the user.
     */
    @Schema(example = "John", description = "First name of the user.")
    @NotBlank(message = "User's first name is missing or blank")
    private String firstName;

    /**
     * The last name of the user.
     */
    @Schema(example = "Doe", description = "Last name of the user.")
    @NotBlank(message = "User's last name is missing or blank")
    private String lastName;

    /**
     * The phone number of the user.
     */
    @Schema(example = "+1 123-456-7890", description = "Phone number of the user.")
    @NotBlank(message = "User's phone number is missing or blank")
    private String phoneNumber;

    /**
     * The password for the user's account.
     */
    @Schema(example = "Password123", description = "Password for the user's account.")
    @NotBlank(message = "User's password is missing or blank")
    private String password;
}
