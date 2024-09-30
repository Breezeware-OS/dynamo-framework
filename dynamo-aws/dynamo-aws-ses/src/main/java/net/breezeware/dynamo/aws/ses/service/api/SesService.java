package net.breezeware.dynamo.aws.ses.service.api;

import java.util.List;
import java.util.Map;

import net.breezeware.dynamo.aws.ses.entity.Attachment;
import net.breezeware.dynamo.aws.ses.exception.DynamoSesException;

/**
 * Email service.
 */
public interface SesService {

    /**
     * Sends an simple email.
     * @param  from               Sender's email address.
     * @param  to                 Receiver's email address.
     * @param  subject            Subject of the mail.
     * @param  body               Body content for the mail.
     * @return                    MessageId , if mail sent successfully.
     * @throws DynamoSesException if error while sending email.
     */
    String sendMail(String from, String to, String subject, String body) throws DynamoSesException;

    /**
     * Sends an email with attachments.
     * @param  from               Sender's email address.
     * @param  to                 Receiver's email addresses.
     * @param  cc                 Receiver's email address.
     * @param  bcc                Receiver's email address.
     * @param  subject            subject A brief summary of the message.
     * @param  body               Email body content for the mails.
     * @param  attachments        List of attachments in Byte array.
     * @return                    MessageId if Email successFully sent.
     * @throws DynamoSesException Throws Mail not send exceptions.
     */
    String sendMail(String from, List<String> to, List<String> cc, List<String> bcc, String subject, String body,
            List<Attachment> attachments) throws DynamoSesException;

    /**
     * Creates an Email Template.
     * @param  name               Name of the template.
     * @param  subject            Subject of the email.
     * @param  htmlPart           HTML body of the email.
     * @param  textPart           Text part of the email.
     * @throws DynamoSesException if error while creating email template.
     */
    void createEmailTemplate(String name, String subject, String htmlPart, String textPart) throws DynamoSesException;

    /**
     * Send an email using an email template.
     * @param  from               Sender's email address.
     * @param  to                 Receiver's email address.
     * @param  templateName       Name of the template to use for building the
     *                            email.
     * @param  templateData       Map of placeholder values for the template.
     * @return                    MessageId if Email successFully sent.
     * @throws DynamoSesException Throws Mail not send exceptions.
     */
    String sendTemplatedEmail(String from, String to, String templateName, Map<String, Object> templateData)
            throws DynamoSesException;

    /**
     * Send an email with bcc using an email template.
     * @param  from               Sender's email address.
     * @param  to                 Receiver's email address.
     * @param  bcc                blind carbon copy(bcc) address.
     * @param  templateName       Name of the template to use for building the
     *                            email.
     * @param  templateData       Map of placeholder values for the template.
     * @return                    MessageId if Email successFully sent.
     * @throws DynamoSesException Throws Mail not send exceptions.
     */
    String sendTemplatedEmailWithBccAddress(String from, String to, String bcc, String templateName,
            Map<String, Object> templateData) throws DynamoSesException;

    /**
     * Retrieves an email template from AWS SES based on the specified template
     * name.
     * @param  templateName       The name of the email template to be retrieved.
     * @return                    The email template with the specified name,
     *                            represented as a
     *                            {@link net.breezeware.dynamo.aws.ses.entity.Template}
     *                            object.
     * @throws DynamoSesException If there is an error while retrieving the email
     *                            template.
     */
    net.breezeware.dynamo.aws.ses.entity.Template getEmailTemplate(String templateName) throws DynamoSesException;
}
