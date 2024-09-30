package net.breezeware.dynamo.dynamoformbuilder.dynamoformsvc.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;

import com.fasterxml.jackson.databind.JsonNode;

import net.breezeware.dynamo.dynamoformbuilder.dynamoformsvc.utils.jsonvalidator.ValidJsonNode;
import net.breezeware.dynamo.generics.crud.entity.GenericEntity;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Represents a version of a Form entity, including form details and JSON data.
 */
@Entity
@Getter
@Setter
@SuperBuilder
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "form_version", schema = "dynamo")
public class FormVersion extends GenericEntity {

    /**
     * The version identifier of the form version.
     */
    @Schema(example = "v2.0.0", description = "The version identifier of the form version")
    @Column(name = "version")
    private String version;

    /**
     * The status of the form version.
     */
    @Schema(example = "Draft", description = "The status of the form version")
    @NotBlank(message = "Form status is missing or blank")
    @Column(name = "status", nullable = false)
    private String status;

    /**
     * The JSON representation of the form version.
     */
    @Schema(example = "{\"question\": \"What is your name?\"}",
            description = "The JSON representation of the form version")
    @ValidJsonNode(message = "Form JSON is missing or blank")
    @Column(name = "form_json", nullable = false)
    private JsonNode formJson;

    /**
     * The associated Form entity for this form version.
     */
    @Schema(description = "The associated Form entity for this form version")
    @OneToOne(targetEntity = Form.class)
    @JoinColumn(name = "form", referencedColumnName = "id", nullable = false)
    private Form form;
}
