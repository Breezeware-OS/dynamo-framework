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
 * Represents a request to invite a new user to the system. It includes the
 * email address, first name, last name, phone number, roles, and groups of the
 * user to be invited.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class UserInviteRequest {

    /**
     * The unique identifier for the User.
     */
    @Schema(example = "4c2bb19c-854d-4320-b257-bae33e1fe279", description = "The unique identifier for the user")
    private UUID userId;

    /**
     * The email address of the user being invited.
     */
    @Schema(example = "john.doe@example.com", description = "Email address of the user being invited.")
    @NotBlank(message = "User's email is missing or blank")
    private String email;

    /**
     * The first name of the user being invited.
     */
    @Schema(example = "John", description = "First name of the user being invited.")
    private String firstName;

    /**
     * The last name of the user being invited.
     */
    @Schema(example = "Doe", description = "Last name of the user being invited.")
    private String lastName;

    /**
     * The phone number of the user being invited.
     */
    @Schema(example = "+1 123-456-7890", description = "Phone number of the user being invited.")
    private String phoneNumber;

    /**
     * The roles to assign to the user upon creating the invitation.
     */
    @Schema(example = "admin", description = "Roles to assign to the user.")
    private List<String> roles;

    /**
     * The groups to assign to the user upon creating the invitation.
     */
    @Schema(example = "developer", description = "Groups to assign to the user.")
    private List<String> groups;

    /**
     * The unique identifier of the user who invited the user.
     */
    @Schema(example = "4985a786-5b6d-4f90-b599-717c67c3954b",
            description = "The unique identifier of the user who invited the user.")
    @NotNull(message = "Invited user id missing")
    private UUID invitedBy;
}
