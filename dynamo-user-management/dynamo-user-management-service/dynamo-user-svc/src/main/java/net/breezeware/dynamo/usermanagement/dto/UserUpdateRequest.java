package net.breezeware.dynamo.usermanagement.dto;

import java.util.List;
import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Represents a request to update user information.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdateRequest {

    /**
     * The unique identifier for the User.
     */
    @Schema(example = "4c2bb19c-854d-4320-b257-bae33e1fe279", description = "The unique identifier for the user")
    @NotNull(message = "User unique ID is missing or blank")
    private UUID userId;

    /**
     * The unique identifier for the Identity Management user.
     */
    @Schema(example = "4c2bb19c-854d-4320-b257-bae33e1fe279",
            description = "The unique identifier for the Identity Management user")
    private String idmUserId;

    /**
     * The first name of the user.
     */
    @Schema(example = "John", description = "Updated first name of the user.")
    private String firstName;

    /**
     * The last name of the user.
     */
    @Schema(example = "Doe", description = "Updated last name of the user.")
    private String lastName;

    /**
     * The email address of the user.
     */
    @Schema(example = "john.doe@example.com", description = "Updated email address of the user.")
    @NotBlank(message = "User's email is missing or blank")
    private String email;

    /**
     * The phone number of the user.
     */
    @Schema(example = "+1 123-456-7890", description = "Updated phone number of the user.")
    private String phoneNumber;

    /**
     * The roles to assign to the user.
     */
    @Schema(example = "admin", description = "Updated roles to assign to the user.")
    private List<String> roles;

    /**
     * The groups to assign to the user.
     */
    @Schema(example = "developer", description = "Updated groups to assign to the user.")
    private List<String> groups;

    /**
     * User's status.
     */
    @Schema(example = "active", description = "User's status.")
    @NotBlank(message = "User's status is missing")
    private String status;
}
