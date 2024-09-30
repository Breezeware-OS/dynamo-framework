package net.breezeware.dynamo.dynamoformbuilder.dynamoformsvc.entity;

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
 * Represents a Form entity containing form details and JSON data.
 */
@Entity
@Getter
@Setter
@SuperBuilder
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "form", schema = "dynamo")
public class Form extends GenericEntity {
    /**
     * The name of the form.
     */
    @Schema(example = "Employee Information", description = "The name of the form")
    @NotBlank(message = "Form name is missing or blank")
    @Column(name = "name", nullable = false, length = 50)
    private String name;

    /**
     * The description of the form.
     */
    @Schema(example = "A form for collecting employee information.", description = "The description of the form")
    @NotBlank(message = "Form description is missing or blank")
    @Column(name = "description", nullable = false)
    private String description;

    /**
     * The version of the form.
     */
    @Schema(example = "v1.0.0", description = "The version of the form")
    @Column(name = "version", length = 50)
    private String version;

    /**
     * The status of the form.
     */
    @Schema(example = "Published", description = "The status of the form")
    @NotBlank(message = "Form status is missing or blank")
    @Column(name = "status", nullable = false, length = 20)
    private String status;

    /**
     * Represents the unique id of the form.
     */
    @Schema(example = "6b47e3a3", description = "Represents the unique id of the form")
    @Column(name = "unique_id")
    private String uniqueId;

    /**
     * Represents the access type of the form.
     */
    @Schema(example = "public", description = "Represents the access type  of the form.")
    @Column(name = "access_type")
    private String accessType;

    /**
     * Represents the owner of the form.
     */
    @Schema(example = "owner@gmail.com", description = "Represents the owner  of the form.")
    @Column(name = "owner")
    private String owner;
}
