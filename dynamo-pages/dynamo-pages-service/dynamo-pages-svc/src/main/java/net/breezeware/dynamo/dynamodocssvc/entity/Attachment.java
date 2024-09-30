package net.breezeware.dynamo.dynamodocssvc.entity;

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
 * Represents an attachment entity in the Dynamo document service. This entity
 * stores information about an attachment, such as its name, key, type, size,
 * and the user who created it.
 */
@Entity
@Table(name = "attachment", schema = "dynamo")
@Data
@EqualsAndHashCode(callSuper = false)
@AllArgsConstructor
@NoArgsConstructor
public class Attachment extends GenericEntity {

    /**
     * The unique identifier of the attachment.
     */
    @Schema(example = "f29a44c8-2e94-4891-9b3d-44c0dc0d52c6", description = "The unique identifier of the attachment.")
    @Column(name = "unique_id")
    private UUID uniqueId;

    /**
     * The document to which the attachment belongs.
     */
    @ManyToOne
    @JoinColumn(name = "document_id", referencedColumnName = "unique_id", nullable = false)
    @Schema(example = "f29a44c8-2e94-4891-9b3d-44c0dc0d52c6",
            description = "The unique identifier of the document to which the attachment belongs.")
    private Document document;

    /**
     * The name of the attachment.
     */
    @Schema(example = "sample_attachment.pdf", description = "The name of the attachment.")
    @Column(name = "name")
    private String name;

    /**
     * The key of the attachment.
     */
    @Schema(example = "attachments/sample_attachment.pdf", description = "The key of the attachment.")
    @Column(name = "key")
    private String key;

    /**
     * The type of the attachment.
     */
    @Schema(example = "application/pdf", description = "The type of the attachment.")
    @Column(name = "type")
    private String type;

    /**
     * The size of the attachment.
     */
    @Schema(example = "1024 KB", description = "The size of the attachment.")
    @Column(name = "size")
    private String size;

    /**
     * The user who created the attachment.
     */
    @Schema(example = "4985a786-5b6d-4f90-b599-717c67c3954b",
            description = "The unique identifier of the user who created the attachment.")
    @ManyToOne
    @JoinColumn(name = "created_by_user_id", referencedColumnName = "unique_id", nullable = false)
    @NotNull(message = "Attachment created user id missing")
    private User createdByUser;

}
