package net.breezeware.dynamo.usermanagement.entity;

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
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;

/**
 * Represents Role details.
 */
@Entity
@Table(name = "role", schema = "dynamo")
// @Table(name = "\"role\"", schema = "dynamo")
@Getter
@Setter
@SuperBuilder
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
public class Role extends GenericEntity {

    /**
     * Role's name.
     */
    @Schema(example = "admin", description = "Role's name.")
    @Column(name = "name", length = 255)
    @NotBlank(message = "Role name is missing or blank")
    private String name;

    /**
     * Role's description.
     */
    @Schema(example = "Role description", description = "Role's description.")
    @Column(name = "description")
    private String description;
}
