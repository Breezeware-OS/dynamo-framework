package net.breezeware.dynamo.usermanagement.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import net.breezeware.dynamo.generics.crud.entity.GenericEntity;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * Represents UserOrganizationMap details.
 */
@Entity
@Table(name = "user_organization_map", schema = "dynamo")
@Getter
@Setter
@SuperBuilder
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
public class UserOrganizationMap extends GenericEntity {

    /**
     * User mapping details.
     */
    @OneToOne
    @JoinColumn(name = "user_id", referencedColumnName = "unique_id")
    private User user;

    /**
     * Role mapping details.
     */
    @OneToOne
    @JoinColumn(name = "organization_id", referencedColumnName = "id")
    private Organization organization;
}
