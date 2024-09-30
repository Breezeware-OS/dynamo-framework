package net.breezeware.dynamo.dynamodocssvc.entity;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.ToString;
import net.breezeware.dynamo.generics.crud.entity.GenericEntity;
import net.breezeware.dynamo.usermanagement.entity.User;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Represents a document entity in the Dynamo document service. This entity
 * stores information about a document, such as its title, content, version,
 * status, and related metadata.
 */
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "document", schema = "dynamo")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Document extends GenericEntity {

    /**
     * The unique identifier of the document.
     */
    @Schema(example = "f29a44c8-2e94-4891-9b3d-44c0dc0d52c6", description = "The unique identifier of the document.")
    @Column(name = "unique_id")
    private UUID uniqueId;

    /**
     * The collection to which the document belongs.
     */
    @ManyToOne
    @JoinColumn(name = "collection_id", referencedColumnName = "unique_id")
    @Schema(example = "f29a44c8-2e94-4891-9b3d-44c0dc0d52c6",
            description = "The unique identifier of the collection to which the document belongs.")
    private Collection collection;

    /**
     * The parent document of this document.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_document_id", referencedColumnName = "unique_id")
    @JsonIgnoreProperties("children")
    private Document parent;

    /**
     * The title of the document.
     */
    @Schema(example = "Introduction to Dynamo Document Service", description = "The title of the document.")
    @Column(name = "title")
    private String title;

    /**
     * The content of the document.
     */
    @Schema(example = "This document provides an overview of the Dynamo Document Service.",
            description = "The content of the document.")
    @Column(name = "content")
    private String content;

    /**
     * The version number of the document.
     */
    @Schema(example = "1", description = "The version number of the document.")
    @Column(name = "version")
    private long version;

    /**
     * The status of the document.
     */
    @Schema(example = "Published", description = "The status of the document.")
    @Column(name = "status")
    private String status;

    /**
     * The timestamp when the document was published.
     */
    @Schema(example = "2023-12-31T23:59:59Z", description = "The timestamp when the document was published.")
    @Column(name = "published_on")
    private Instant publishedOn;

    /**
     * The timestamp when the document was archived.
     */
    @Schema(example = "2023-12-31T23:59:59Z", description = "The timestamp when the document was archived.")
    @Column(name = "archived_on")
    private Instant archivedOn;

    /**
     * The timestamp when the document was deleted.
     */
    @Schema(example = "2023-12-31T23:59:59Z", description = "The timestamp when the document was deleted.")
    @Column(name = "deleted_on")
    private Instant deletedOn;

    /**
     * The user who created the document.
     */
    @Schema(example = "4985a786-5b6d-4f90-b599-717c67c3954b",
            description = "The unique identifier of the user who created the document.")
    @ManyToOne
    @JoinColumn(name = "created_by_user_id", referencedColumnName = "unique_id", nullable = false)
    @NotNull(message = "Document created user id missing")
    private User createdByUser;

    /**
     * The list of child documents of this document.
     */
    @ToString.Exclude
    @Schema(description = "The list of child documents of this document.")
    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnoreProperties("parent")
    private List<Document> children;

}
