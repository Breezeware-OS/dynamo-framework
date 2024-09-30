package net.breezeware.dynamo.usermanagement.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;

import net.breezeware.dynamo.generics.crud.entity.GenericEntity;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Represents Identity Management Provider details.
 */
@Entity
@Table(name = "idm_info", schema = "dynamo")
@Getter
@Setter
@SuperBuilder
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
public class IdmInfo extends GenericEntity {

    /**
     * The unique identifier for the Identity Management(ex:- user pool id).
     */
    @Schema(example = "4c2bb19c-854d-4320-b257-bae33e1fe279",
            description = "The unique identifier for the Identity Management")
    @NotBlank(message = "Identity Management unique ID is missing or blank")
    @Column(name = "idm_unique_id", unique = true, nullable = false)
    private String idmUniqueId;

    /**
     * Identity Management's provider name.
     */
    @Schema(example = "John", description = "Identity Management's provider name.")
    @Column(name = "name", length = 255)
    @NotBlank(message = "Identity Management provider name is missing or blank")
    private String name;

    /**
     * Identity Management's provider description.
     */
    @Schema(example = "User Identity Management system", description = "Identity Management's provider description.")
    @Column(name = "description")
    private String description;
}
