package net.breezeware.dynamo.aws.ses.entity;

import java.io.Serializable;

import lombok.Builder;
import lombok.Data;

/**
 * Represents an email template with different parts (subject, text, and HTML).
 * This class provides a convenient way to organize and manage email templates
 * for sending personalized emails.
 */
@Data
@Builder
public class Template implements Serializable {

    /**
     * The name of the template.
     */
    private String templateName;

    /**
     * The subject of the email template.
     */
    private String subjectPart;

    /**
     * The plain text content of the email template, supports placeholder variables.
     */
    private String textPart;

    /**
     * The HTML content of the email template, supports placeholder variables.
     */
    private String htmlPart;

}
