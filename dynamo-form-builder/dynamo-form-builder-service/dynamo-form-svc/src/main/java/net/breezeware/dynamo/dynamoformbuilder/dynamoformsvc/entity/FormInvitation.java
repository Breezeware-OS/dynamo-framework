package net.breezeware.dynamo.dynamoformbuilder.dynamoformsvc.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
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
 * Represents an invitation for a user to a form.
 */
@Entity
@Getter
@Setter
@SuperBuilder
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "form_invitation", schema = "dynamo")
public class FormInvitation extends GenericEntity {

    /**
     * The email ID of the invited user.
     */
    @Schema(example = "user@example.com", description = "The email ID of the invited user")
    @NotBlank(message = "Invited user email ID is missing or blank")
    @Email(message = "Invalid email format")
    @Column(name = "email", nullable = false, length = 100)
    private String email;

    /**
     * The status of the invitation.
     */
    @Schema(example = "PENDING", description = "The status of the invitation")
    @Column(name = "status", nullable = false, length = 20)
    private String status;

    /**
     * The form associated with the invitation.
     */
    @OneToOne(targetEntity = Form.class)
    @JoinColumn(name = "form_id", referencedColumnName = "id")
    private Form form;
}
