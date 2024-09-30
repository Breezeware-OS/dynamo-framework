package net.breezeware.dynamo.usermanagement.entity;

import java.util.List;
import java.util.UUID;

import net.breezeware.dynamo.generics.crud.entity.GenericEntity;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

/**
 * Represents User details.
 */
@Entity
@Table(name = "user", schema = "dynamo")
// @Table(name = "\"user\"", schema = "dynamo")
@Getter
@Setter
@SuperBuilder
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
public class User extends GenericEntity {

    /**
     * The unique identifier for the User.
     */
    @Schema(example = "4c2bb19c-854d-4320-b257-bae33e1fe279", description = "The unique identifier for the user")
    @NotNull(message = "User unique ID is missing or blank")
    @Column(name = "unique_id", unique = true, nullable = false)
    private UUID uniqueId;

    /**
     * The unique identifier for the Identity Management user.
     */
    @Schema(example = "4c2bb19c-854d-4320-b257-bae33e1fe279",
            description = "The unique identifier for the Identity Management user")
    @NotBlank(message = "Identity Management User unique ID is missing or blank")
    @Column(name = "idm_user_id", unique = true, nullable = false)
    private String idmUserId;

    /**
     * IdmInfo mapping details.
     */
    @OneToOne
    @JoinColumn(name = "idmInfo", referencedColumnName = "id")
    private IdmInfo idmInfo;

    /**
     * User's first name.
     */
    @Schema(example = "John", description = "User's first name.")
    @Column(name = "first_name", length = 255)
    private String firstName;

    /**
     * User's last name.
     */
    @Schema(example = "Doe", description = "User's last name.")
    @Column(name = "last_name", length = 255)
    private String lastName;

    /**
     * User's email.
     */
    @Schema(example = "John@examble.com", description = "User's email.")
    @Column(name = "email", length = 255)
    @Pattern(regexp = "^$|^.+@.+\\..+", message = "Invalid email address")
    @NotBlank(message = "User's email is missing")
    private String email;

    /**
     * User's phone number.
     */
    @Schema(example = "+18574563257", description = "User's phone number.")
    @Column(name = "phone_number", length = 255)
    private String phoneNumber;

    /**
     * User's status.
     */
    @Schema(example = "active", description = "User's status.")
    @Column(name = "status", length = 255)
    @NotBlank(message = "User's status is missing")
    private String status;

    // NOTE: two-way JPA mapping is configured for filtering purpose
    /**
     * User is mapped to one to more roles. One-to-many relationship.
     */
    @OneToMany
    @JoinColumn(name = "user_id", referencedColumnName = "unique_id")
    private List<UserRoleMap> userRoleMap;

    /**
     * User is mapped to one or more groups. One-to-many relationship.
     */
    @OneToMany
    @JoinColumn(name = "user_id", referencedColumnName = "unique_id")
    private List<UserGroupMap> userGroupMap;

    /**
     * User is mapped to one or more organization. One-to-many relationship.
     */
    @OneToMany
    @JoinColumn(name = "user_id", referencedColumnName = "unique_id")
    private List<UserOrganizationMap> userOrganizationMap;

    /**
     * Additional information about the user stored as JSON string.
     */
    @Schema(example = """
            {
                "isEmailVerified":true,
                "middleName": "James",
                "age": 25
            }
            """, description = "Additional metadata about the user stored as JSON.")
    @Column(name = "additional_info")
    private String additionalInfo;
}
