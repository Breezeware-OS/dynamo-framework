package net.breezeware.dynamo.usermanagement.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Represents the response object for retrieving user.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class UserViewResponse {

    /**
     * The unique identifier for the User.
     */
    @Schema(example = "4c2bb19c-854d-4320-b257-bae33e1fe279", description = "The unique identifier for the user")
    private UUID userId;

    /**
     * The email address of the user.
     */
    @Schema(example = "john.doe@example.com", description = "Email address of the user.")
    private String email;

    /**
     * The first name of the user.
     */
    @Schema(example = "John", description = "First name of the user.")
    private String firstName;

    /**
     * The last name of the user.
     */
    @Schema(example = "Doe", description = "Last name of the user.")
    private String lastName;

    /**
     * The phone number of the user.
     */
    @Schema(example = "+1 123-456-7890", description = "Phone number of the user.")
    private String phoneNumber;

    /**
     * The roles of the user.
     */
    @Schema(example = "admin", description = "Roles of the user.")
    private List<String> roles;

    /**
     * The groups of the user.
     */
    @Schema(example = "developer", description = "Groups of the user.")
    private List<String> groups;

    /**
     * The status of the user, e.g. active, disabled, etc.
     */
    @Schema(example = "active", description = "User's status.")
    private String status;

    /**
     * The timestamp when the user was created.
     */
    @Schema(example = "2023-09-18 11:43:47.215", description = "User's created timestamp.")
    private Instant createdOn;

}
