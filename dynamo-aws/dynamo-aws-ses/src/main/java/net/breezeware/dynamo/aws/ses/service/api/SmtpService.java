package net.breezeware.dynamo.aws.ses.service.api;

import net.breezeware.dynamo.aws.ses.exception.DynamoSesException;

/**
 * Email service.
 */
public interface SmtpService {
    /**
     * Sends an email using SMTP protocol.
     * @param  from               Sender's email address.
     * @param  to                 Receiver's email address.
     * @param  subject            Subject of the mail.
     * @param  htmlBody           Body content for the mail.
     * @throws DynamoSesException if error while sending email.
     */
    void sendSmtpMail(String from, String to, String subject, String htmlBody) throws DynamoSesException;
}
