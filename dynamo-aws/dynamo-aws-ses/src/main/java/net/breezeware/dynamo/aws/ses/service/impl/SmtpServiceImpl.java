package net.breezeware.dynamo.aws.ses.service.impl;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

import org.springframework.context.annotation.Profile;
import org.springframework.mail.MailAuthenticationException;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import net.breezeware.dynamo.aws.ses.exception.DynamoSesException;
import net.breezeware.dynamo.aws.ses.service.api.SmtpService;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementation of the {@link SmtpService} interface that uses the SMTP
 * protocol to send emails using Spring's {@link JavaMailSender}. This class is
 * responsible for sending emails with HTML content via the SMTP server.
 */
@Service
@Slf4j
@AllArgsConstructor
@Profile("SmtpEmail")
public class SmtpServiceImpl implements SmtpService {

    private final JavaMailSender javaMailSender;

    @Override
    public void sendSmtpMail(String from, String to, String subject, String htmlBody) throws DynamoSesException {
        log.debug("Entering sendSmtpMail(), from = {}, to = {}, subject = {}, htmlBody = {}", from, to, subject,
                htmlBody);
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        try {
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, false, "UTF-8");
            mimeMessageHelper.setFrom(from);
            mimeMessageHelper.setTo(to);
            mimeMessageHelper.setSubject(subject);
            // configuring content type as "text/html" by setting setText() 2nd parameter to
            // 'true'. If not configured, default value is "false", setting content type as
            // "text/plain".
            mimeMessageHelper.setText(htmlBody, true);
            javaMailSender.send(mimeMessage);
            log.info("Successfully sent mail using SMTP protocol from '{}' to '{}'", from, to);
            log.debug("Leaving sendSmtpMail()");
        } catch (MessagingException e) {
            log.error("Error while creating MIME message. Mail not sent! error = {}", e.getMessage());
            throw new DynamoSesException(e);
        } catch (MailAuthenticationException e) {
            log.error("Mail authentication failed error. Mail not sent! error = {}", e.getMessage());
            throw new DynamoSesException(e);
        } catch (MailException e) {
            log.error("Error while sending mail. Mail not sent! error = {}", e.getMessage());
            throw new DynamoSesException(e);
        }

    }
}
