package net.breezeware.dynamo.aws.ses.entity;

import java.io.Serializable;

import lombok.Builder;
import lombok.Data;

/**
 * Represents an email attachment with its content and metadata. This class
 * allows you to store attachments along with additional information such as the
 * attachment's name, size, and type.
 */
@Data
@Builder
public class Attachment implements Serializable {

    /**
     * The binary content of the attachment.
     */
    private byte[] attachmentContent;

    /**
     * The metadata of the attachment containing the attachment's name, size, and
     * type.
     */
    private MetaData attachmentMetaData;

    /**
     * Represents the metadata of an email attachment. This class contains
     * information such as the attachment's name, size, and type.
     */
    @Data
    @Builder
    public static class MetaData implements Serializable {
        /**
         * The name of the attachment.
         */
        private String attachmentName;

        /**
         * The size of the attachment.
         */
        private String attachmentSize;

        /**
         * The type of the attachment.
         */
        private String attachmentType;
    }

}
