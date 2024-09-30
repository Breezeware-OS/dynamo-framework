package net.breezeware.dynamo.dynamodocssvc.entity;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;

import net.breezeware.dynamo.generics.crud.entity.GenericEntity;
import net.breezeware.dynamo.usermanagement.entity.User;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Represents a revision entity in the Dynamo document service. This entity
 * stores information about a revision, such as its title, content, version,
 * status, and related metadata.
 */
@Entity
@Table(name = "revision", schema = "dynamo")
@Data
@EqualsAndHashCode(callSuper = false)
@AllArgsConstructor
@NoArgsConstructor
public class Revision extends GenericEntity {

    /**
     * The unique identifier of the revision.
     */
    @Schema(example = "f29a44c8-2e94-4891-9b3d-44c0dc0d52c6", description = "The unique identifier of the revision.")
    @Column(name = "unique_id")
    private UUID uniqueId;

    /**
     * The document to which the revision belongs.
     */
    @ManyToOne
    @JoinColumn(name = "document_id", referencedColumnName = "unique_id", nullable = false)
    @Schema(example = "f29a44c8-2e94-4891-9b3d-44c0dc0d52c6",
            description = "The unique identifier of the document to which the revision belongs.")
    private Document document;

    /**
     * The title of the revision.
     */
    @Schema(example = "Introduction Revision 1", description = "The title of the revision.")
    @Column(name = "title")
    private String title;

    /**
     * The content of the revision.
     */
    @Schema(example = "This is the content of Revision 1.", description = "The content of the revision.")
    @Column(name = "content")
    private String content;

    /**
     * The version number of the revision.
     */
    @Schema(example = "1", description = "The version number of the revision.")
    @Column(name = "version")
    private long version;

    /**
     * The status of the revision.
     */
    @Schema(example = "Published", description = "The status of the revision.")
    @Column(name = "status")
    private String status;

    /**
     * The user who edited the document.
     */
    @Schema(example = "4985a786-5b6d-4f90-b599-717c67c3954b",
            description = "The unique identifier of the user who edited the document.")
    @ManyToOne
    @JoinColumn(name = "edited_by_user_id", referencedColumnName = "unique_id", nullable = false)
    @NotNull(message = "Document edited user id missing")
    private User editedByUser;

    /**
     * The timestamp when the revision was edited.
     */
    @Schema(example = "2023-12-31T23:59:59Z", description = "The timestamp when the revision was edited.")
    @Column(name = "edited_on")
    private Instant editedOn;

}
