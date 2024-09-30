package net.breezeware.dynamo.workflow.entity;

import java.io.Serializable;
import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import org.hibernate.annotations.JdbcTypeCode;

import com.fasterxml.jackson.databind.JsonNode;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
import org.hibernate.type.SqlTypes;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Entity
@Table(name = "process_domain_entity", schema = "dynamo")
@Convert(attributeName = "entityAttrName", converter = JsonBinaryType.class)
public class ProcessDomainEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private long id;

    @Column(name = "entity_name", nullable = false)
    private String entityName;

    @JdbcTypeCode(SqlTypes.JSON)
    @Convert(disableConversion = true)
    @Column(name = "entity_properties", columnDefinition = "json")
    private JsonNode entityProperties;

    @Column(name = "process_instance_user_definition_key", nullable = false)
    private String processInstanceUserDefinitionKey;

    @Column(name = "created_on", nullable = false)
    private Instant createdOn;

    @Column(name = "modified_on", nullable = false)
    private Instant modifiedOn;
}
