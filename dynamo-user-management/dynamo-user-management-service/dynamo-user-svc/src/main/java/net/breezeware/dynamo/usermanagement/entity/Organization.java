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
 * Represents Organization details.
 */
@Entity
@Table(name = "organization", schema = "dynamo")
@Getter
@Setter
@SuperBuilder
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
public class Organization extends GenericEntity {

    /**
     * Organization's name.
     */
    @Schema(example = "Google", description = "Organization's name.")
    @Column(name = "name", length = 255)
    @NotBlank(message = "Organization name is missing or blank")
    private String name;

    /**
     * Organization's description.
     */
    @Schema(example = "Organization description", description = "Organization's description.")
    @Column(name = "description")
    private String description;
}
