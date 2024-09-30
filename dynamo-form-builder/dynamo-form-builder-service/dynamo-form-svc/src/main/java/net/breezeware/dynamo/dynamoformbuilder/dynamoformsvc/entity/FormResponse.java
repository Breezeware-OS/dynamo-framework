package net.breezeware.dynamo.dynamoformbuilder.dynamoformsvc.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;

import com.fasterxml.jackson.databind.JsonNode;

import net.breezeware.dynamo.dynamoformbuilder.dynamoformsvc.utils.jsonvalidator.ValidJsonNode;
import net.breezeware.dynamo.generics.crud.entity.GenericEntity;

import lombok.AllArgsConstructor;
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
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "form_response", schema = "dynamo")
public class FormResponse extends GenericEntity {

    /**
     * The JSON representation of the form response.
     */
    @Schema(example = "{\"question\": \"What is your name?\"}",
            description = "The JSON representation of the form Response")
    @ValidJsonNode(message = "Response JSON is missing or blank")
    @Column(name = "response_json", nullable = false)
    private JsonNode responseJson;

    /**
     * The associated Form entity for this form response.
     */
    @Schema(description = "The associated Form entity for this form response")
    @OneToOne(targetEntity = Form.class)
    @JoinColumn(name = "form_id", referencedColumnName = "id", nullable = false)
    private Form form;

    /**
     * The email of the user who submitted the form response.
     */
    @Schema(description = "The email of the user who submitted the form response")
    @Email(message = "Invalid email format")
    @Column(name = "email", nullable = false)
    private String email;

}
