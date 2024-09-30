package net.breezeware.dynamo.aws.ses.service.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;

import jakarta.activation.DataHandler;
import jakarta.activation.DataSource;
import jakarta.mail.Message.RecipientType;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import jakarta.mail.util.ByteArrayDataSource;

import org.json.JSONObject;
import org.springframework.stereotype.Service;

import net.breezeware.dynamo.aws.ses.entity.Attachment;
import net.breezeware.dynamo.aws.ses.exception.DynamoSesException;
import net.breezeware.dynamo.aws.ses.service.api.SesService;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.AccountSendingPausedException;
import software.amazon.awssdk.services.ses.model.AlreadyExistsException;
import software.amazon.awssdk.services.ses.model.Body;
import software.amazon.awssdk.services.ses.model.ConfigurationSetDoesNotExistException;
import software.amazon.awssdk.services.ses.model.ConfigurationSetSendingPausedException;
import software.amazon.awssdk.services.ses.model.Content;
import software.amazon.awssdk.services.ses.model.CreateTemplateRequest;
import software.amazon.awssdk.services.ses.model.CreateTemplateResponse;
import software.amazon.awssdk.services.ses.model.Destination;
import software.amazon.awssdk.services.ses.model.GetTemplateRequest;
import software.amazon.awssdk.services.ses.model.GetTemplateResponse;
import software.amazon.awssdk.services.ses.model.InvalidTemplateException;
import software.amazon.awssdk.services.ses.model.LimitExceededException;
import software.amazon.awssdk.services.ses.model.MailFromDomainNotVerifiedException;
import software.amazon.awssdk.services.ses.model.Message;
import software.amazon.awssdk.services.ses.model.MessageRejectedException;
import software.amazon.awssdk.services.ses.model.RawMessage;
import software.amazon.awssdk.services.ses.model.SendEmailRequest;
import software.amazon.awssdk.services.ses.model.SendEmailResponse;
import software.amazon.awssdk.services.ses.model.SendRawEmailRequest;
import software.amazon.awssdk.services.ses.model.SendRawEmailResponse;
import software.amazon.awssdk.services.ses.model.SendTemplatedEmailRequest;
import software.amazon.awssdk.services.ses.model.SendTemplatedEmailResponse;
import software.amazon.awssdk.services.ses.model.Template;
import software.amazon.awssdk.services.ses.model.TemplateDoesNotExistException;

/**
 * Email service implementation.
 */
@Service
@Slf4j
@AllArgsConstructor
public class SesServiceImpl implements SesService {

    private static final Session mailSession = Session.getDefaultInstance(new Properties());
    private final SesClient sesClient;

    @Override
    public String sendMail(String from, String to, String subject, String body) throws DynamoSesException {
        log.debug("Entering sendMail(), from = {}, to = {}, subject = {}, body = {}", from, to, subject, body);

        try {
            SendEmailRequest sendEmailRequest = SendEmailRequest.builder().source(from)
                    .destination(Destination.builder().toAddresses(List.of(to)).build())
                    .message(Message.builder().subject(Content.builder().data(subject).charset("UTF-8").build())
                            .body(Body.builder().text(Content.builder().data(body).charset("UTF-8").build()).build())
                            .build())
                    .build();

            SendEmailResponse sendEmailResponse = sesClient.sendEmail(sendEmailRequest);

            String messageId = sendEmailResponse.messageId();

            log.info("Email sent successfully from '{}' to '{}'", from, to);
            log.debug("Leaving sendMail(), messageId = {}", messageId);
            return messageId;
        } catch (MessageRejectedException e) {
            log.error("AWS SES service send email error. Email not sent! error = {}",
                    e.awsErrorDetails().errorMessage());
            throw new DynamoSesException("AWS SES SendEmail action failed. Email not sent.", e);
        } catch (MailFromDomainNotVerifiedException e) {
            log.error("AWS SES service email from domain not verified error. Email not sent! error = {}",
                    e.awsErrorDetails().errorMessage());
            throw new DynamoSesException("AWS SES Mail from Domain not verified. Email not sent.", e);
        } catch (ConfigurationSetDoesNotExistException e) {
            log.error("AWS SES service configuration set not exists error. Email not sent! error = {}",
                    e.awsErrorDetails().errorMessage());
            throw new DynamoSesException("AWS SES Configuration set not exists error. Email not sent.", e);
        } catch (ConfigurationSetSendingPausedException e) {
            log.error("AWS SES service email sending paused for configuration set error. Email not sent! error = {}",
                    e.awsErrorDetails().errorMessage());
            throw new DynamoSesException("AWS SES Mail sending paused for configuration set error. Email not sent.", e);
        } catch (AccountSendingPausedException e) {
            log.error("AWS SES service email sending disabled for account error. Email not sent! error = {}",
                    e.awsErrorDetails().errorMessage());
            throw new DynamoSesException("AWS SES Mail sending disabled error. Email not sent.", e);
        } catch (SdkClientException e) {
            log.error("AWS SDK client error while sending mail using SES, error = {}", e.getMessage());
            throw new DynamoSesException(e.getMessage(), e);
        }

    }

    @Override
    public String sendMail(String from, List<String> to, List<String> cc, List<String> bcc, String subject, String body,
            List<Attachment> attachments) throws DynamoSesException {
        log.debug(
                "Entering sendMail(), from = {}, to = {}, cc = {}, bcc = {}, subject = {}, body = {}, attachments = {}",
                from, to, cc, bcc, subject, body, attachments);

        try {
            MimeMessage mimeMessage = new MimeMessage(mailSession);
            mimeMessage.setFrom(from);
            setRecipients(to, RecipientType.TO, mimeMessage);
            setRecipients(cc, RecipientType.CC, mimeMessage);
            setRecipients(bcc, RecipientType.BCC, mimeMessage);
            mimeMessage.setSubject(subject, "UTF-8");

            MimeBodyPart bodyPart = new MimeBodyPart();
            bodyPart.setContent(body, "text/html; charset=UTF-8");
            log.debug("Configured MIME body part with body content, bodyPart = {}", bodyPart);

            // body text
            MimeMultipart parts = new MimeMultipart();
            parts.addBodyPart(bodyPart);

            // body attachment
            if (Objects.nonNull(attachments) && !attachments.isEmpty()) {

                for (Attachment attachment : attachments) {
                    log.debug("Handling attachmentPart, attachment = {}", attachment);
                    MimeBodyPart attachmentPart = new MimeBodyPart();
                    ByteArrayInputStream byteArrayInputStream =
                            new ByteArrayInputStream(attachment.getAttachmentContent());
                    DataSource source = new ByteArrayDataSource(byteArrayInputStream,
                            attachment.getAttachmentMetaData().getAttachmentType());
                    attachmentPart.setDataHandler(new DataHandler(source));
                    attachmentPart.setFileName(attachment.getAttachmentMetaData().getAttachmentName());
                    log.debug("Configured MIME body part with attachment, attachmentPart = {}", attachmentPart);
                    parts.addBodyPart(attachmentPart);
                }

            }

            log.info("# of body parts = {}", parts.getCount());
            mimeMessage.setContent(parts);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            mimeMessage.writeTo(outputStream);

            ByteBuffer buf = ByteBuffer.wrap(outputStream.toByteArray());
            byte[] arr = new byte[buf.remaining()];
            buf.get(arr);

            SdkBytes data = SdkBytes.fromByteArray(arr);
            RawMessage rawMessage = RawMessage.builder().data(data).build();
            SendRawEmailRequest rawEmailRequest = SendRawEmailRequest.builder().rawMessage(rawMessage).build();

            SendRawEmailResponse sendRawEmailResponse = sesClient.sendRawEmail(rawEmailRequest);
            String messageId = sendRawEmailResponse.messageId();
            log.info("Email sent successfully from '{}' to '{}'", from, to);
            log.debug("Leaving sendMail(), messageId = {}", messageId);
            return messageId;

        } catch (MessagingException | IOException e) {
            log.error("Error while building MimeMessage, error = {}", e.getMessage());
            throw new DynamoSesException(e);
        } catch (MessageRejectedException e) {
            log.error("AWS SES service send email error. Email not sent! error = {}",
                    e.awsErrorDetails().errorMessage());
            throw new DynamoSesException("AWS SES SendEmail action failed. Email not sent.", e);
        } catch (MailFromDomainNotVerifiedException e) {
            log.error("AWS SES service email from domain not verified error. Email not sent! error = {}",
                    e.awsErrorDetails().errorMessage());
            throw new DynamoSesException("AWS SES Mail from Domain not verified. Email not sent.", e);
        } catch (ConfigurationSetDoesNotExistException e) {
            log.error("AWS SES service configuration set not exists error. Email not sent! error = {}",
                    e.awsErrorDetails().errorMessage());
            throw new DynamoSesException("AWS SES Configuration set not exists error. Email not sent.", e);
        } catch (ConfigurationSetSendingPausedException e) {
            log.error("AWS SES service email sending paused for configuration set error. Email not sent! error = {}",
                    e.awsErrorDetails().errorMessage());
            throw new DynamoSesException("AWS SES Mail sending paused for configuration set error. Email not sent.", e);
        } catch (AccountSendingPausedException e) {
            log.error("AWS SES service email sending disabled for account error. Email not sent! error = {}",
                    e.awsErrorDetails().errorMessage());
            throw new DynamoSesException("AWS SES Mail sending disabled error. Email not sent.", e);
        } catch (SdkClientException e) {
            log.error("AWS SDK client error while sending mail using SES, error = {}", e.getMessage());
            throw new DynamoSesException(e.getMessage(), e);
        }

    }

    /**
     * Sets the recipient addresses for the given recipient type in the provided
     * MimeMessage. This method takes a list of email addresses in string format,
     * converts them to InternetAddress objects, and sets them as the recipients of
     * the specified recipient type in the MimeMessage.
     * @param  addressList        A List of String representing the email addresses
     *                            of the recipients. Must not be null or empty.
     * @param  recipientType      The type of the recipients, such as TO, CC, or
     *                            BCC, defined in javax.mail.Message.RecipientType.
     *                            Must not be null.
     * @param  mimeMessage        The MimeMessage instance to which the recipient
     *                            addresses will be set. Must not be null.
     * @throws MessagingException if an error occurs while setting the recipients in
     *                            the MimeMessage.
     */
    private void setRecipients(List<String> addressList, RecipientType recipientType, MimeMessage mimeMessage)
            throws MessagingException {
        log.info("Entering setRecipients()");
        InternetAddress[] internetAddresses;
        if (Objects.nonNull(addressList) && !addressList.isEmpty()) {
            internetAddresses = new InternetAddress[addressList.size()];
            for (int addressIndex = 0; addressIndex < addressList.size(); addressIndex++) {
                internetAddresses[addressIndex] = new InternetAddress(addressList.get(addressIndex));
            }

            mimeMessage.setRecipients(recipientType, internetAddresses);
        }

        log.info("Leaving setRecipients()");
    }

    public void createEmailTemplate(String name, String subject, String htmlPart, String textPart)
            throws DynamoSesException {
        log.info("Entering createEmailTemplate(), name = {}", name);
        Template template = Template.builder().templateName(name).subjectPart(subject).htmlPart(htmlPart)
                .textPart(textPart).build();
        CreateTemplateRequest createTemplateRequest = CreateTemplateRequest.builder().template(template).build();
        try {
            CreateTemplateResponse createTemplateResponse = sesClient.createTemplate(createTemplateRequest);
            log.debug("Leaving createEmailTemplate(), createTemplateResponse = {}", createTemplateResponse);
        } catch (AlreadyExistsException e) {
            log.error("AWS SES service email template exists error. Error creating email template, error = {}",
                    e.awsErrorDetails().errorMessage());
            throw new DynamoSesException("AWS SES email template creation error. Email template not created", e);
        } catch (InvalidTemplateException e) {
            log.error("AWS SES service invalid email template error. Error creating email template, error = {}",
                    e.awsErrorDetails().errorMessage());
            throw new DynamoSesException("AWS SES invalid email template error. Email template not created", e);
        } catch (LimitExceededException e) {
            log.error("AWS SES service email template creation limit exceeded error. Email template note created", e);
            throw new DynamoSesException(
                    "AWS SES email template creation limit exceeded error. Email template not created", e);
        } catch (SdkClientException e) {
            log.error("AWS SDK client error while sending mail using SES, error = {}", e.getMessage());
            throw new DynamoSesException(e.getMessage(), e);
        }

    }

    public String sendTemplatedEmail(String from, String to, String templateName, Map<String, Object> templateData)
            throws DynamoSesException {
        log.debug("Entering sendTemplatedEmail(), from = {}, to = {}, templateName = {}, templateData = {}", from, to,
                templateName, templateData);

        String templateDataString = JSONObject.wrap(templateData).toString();

        try {
            SendTemplatedEmailRequest sendTemplatedEmailRequest =
                    SendTemplatedEmailRequest.builder().template(templateName).templateData(templateDataString)
                            .source(from).destination(Destination.builder().toAddresses(List.of(to)).build()).build();

            SendTemplatedEmailResponse sendTemplatedEmailResponse =
                    sesClient.sendTemplatedEmail(sendTemplatedEmailRequest);
            String messageId = sendTemplatedEmailResponse.messageId();
            log.info("Email sent successfully using template = {} from '{}' to '{}'", templateName, from, to);
            log.debug("Leaving sendTemplatedEmail(), messageId = {}", messageId);
            return messageId;
        } catch (MessageRejectedException e) {
            log.error("AWS SES service send email error. Email not sent! error = {}",
                    e.awsErrorDetails().errorMessage());
            throw new DynamoSesException("AWS SES SendEmail action failed. Email not sent.", e);
        } catch (MailFromDomainNotVerifiedException e) {
            log.error("AWS SES service email from domain not verified error. Email not sent! error = {}",
                    e.awsErrorDetails().errorMessage());
            throw new DynamoSesException("AWS SES Mail from Domain not verified. Email not sent.", e);
        } catch (ConfigurationSetDoesNotExistException e) {
            log.error("AWS SES service configuration set not exists error. Email not sent! error = {}",
                    e.awsErrorDetails().errorMessage());
            throw new DynamoSesException("AWS SES Configuration set not exists error. Email not sent.", e);
        } catch (ConfigurationSetSendingPausedException e) {
            log.error("AWS SES service email sending paused for configuration set error. Email not sent! error = {}",
                    e.awsErrorDetails().errorMessage());
            throw new DynamoSesException("AWS SES Mail sending paused for configuration set error. Email not sent.", e);
        } catch (AccountSendingPausedException e) {
            log.error("AWS SES service email sending disabled for account error. Email not sent! error = {}",
                    e.awsErrorDetails().errorMessage());
            throw new DynamoSesException("AWS SES Mail sending disabled error. Email not sent.", e);
        } catch (SdkClientException e) {
            log.error("AWS SDK client error while sending mail using SES, error = {}", e.getMessage());
            throw new DynamoSesException(e.getMessage(), e);
        }

    }

    @Override
    public String sendTemplatedEmailWithBccAddress(String from, String to, String bcc, String templateName,
            Map<String, Object> templateData) throws DynamoSesException {
        log.debug("Entering sendTemplatedEmailWithBccAddress(),from = {}, to = {},templateName = {}, templateData = {}",
                from, to, templateName, templateData);

        String templateDataString = JSONObject.wrap(templateData).toString();

        try {
            SendTemplatedEmailRequest sendTemplatedEmailRequest = SendTemplatedEmailRequest.builder()
                    .template(templateName).templateData(templateDataString).source(from)
                    .destination(Destination.builder().toAddresses(List.of(to)).bccAddresses(List.of(bcc)).build())
                    .build();

            SendTemplatedEmailResponse sendTemplatedEmailResponse =
                    sesClient.sendTemplatedEmail(sendTemplatedEmailRequest);
            String messageId = sendTemplatedEmailResponse.messageId();
            log.info("Email sent successfully using template = {} from '{}' to '{}' bcc '{}'", templateName, from, to,
                    bcc);
            log.debug("Leaving sendTemplatedEmailWithBccAddress(), messageId = {}", messageId);
            return messageId;
        } catch (MessageRejectedException e) {
            log.error("AWS SES service send email error. Email not sent! error = {}",
                    e.awsErrorDetails().errorMessage());
            throw new DynamoSesException("AWS SES SendEmail action failed. Email not sent.", e);
        } catch (MailFromDomainNotVerifiedException e) {
            log.error("AWS SES service email from domain not verified error. Email not sent! error = {}",
                    e.awsErrorDetails().errorMessage());
            throw new DynamoSesException("AWS SES Mail from Domain not verified. Email not sent.", e);
        } catch (ConfigurationSetDoesNotExistException e) {
            log.error("AWS SES service configuration set not exists error. Email not sent! error = {}",
                    e.awsErrorDetails().errorMessage());
            throw new DynamoSesException("AWS SES Configuration set not exists error. Email not sent.", e);
        } catch (ConfigurationSetSendingPausedException e) {
            log.error("AWS SES service email sending paused for configuration set error. Email not sent! error = {}",
                    e.awsErrorDetails().errorMessage());
            throw new DynamoSesException("AWS SES Mail sending paused for configuration set error. Email not sent.", e);
        } catch (AccountSendingPausedException e) {
            log.error("AWS SES service email sending disabled for account error. Email not sent! error = {}",
                    e.awsErrorDetails().errorMessage());
            throw new DynamoSesException("AWS SES Mail sending disabled error. Email not sent.", e);
        } catch (SdkClientException e) {
            log.error("AWS SDK client error while sending mail using SES, error = {}", e.getMessage());
            throw new DynamoSesException(e.getMessage(), e);
        }

    }

    @Override
    public net.breezeware.dynamo.aws.ses.entity.Template getEmailTemplate(String templateName)
            throws DynamoSesException {
        log.info("Entering getEmailTemplate(), templateName = {}", templateName);
        try {
            GetTemplateRequest getTemplateRequest = GetTemplateRequest.builder().templateName(templateName).build();
            GetTemplateResponse getTemplateResponse = sesClient.getTemplate(getTemplateRequest);
            Template retrievedSesTemplate = getTemplateResponse.template();
            net.breezeware.dynamo.aws.ses.entity.Template template = net.breezeware.dynamo.aws.ses.entity.Template
                    .builder().templateName(retrievedSesTemplate.templateName())
                    .htmlPart(retrievedSesTemplate.htmlPart()).subjectPart(retrievedSesTemplate.subjectPart())
                    .textPart(retrievedSesTemplate.textPart()).build();
            log.info("Leaving getEmailTemplate , templateName = {}", template.getTemplateName());
            return template;
        } catch (TemplateDoesNotExistException e) {
            log.error("AWS SES service email template does not exist. Email not sent! Error: {}",
                    e.awsErrorDetails().errorMessage());
            throw new DynamoSesException("AWS SES Mail sending disabled due to email template not found.", e);
        } catch (AwsServiceException e) {
            log.error("AWS SES service error occurred. Email not sent! Error: {}", e.awsErrorDetails().errorMessage());
            throw new DynamoSesException("AWS SES Mail sending disabled due to service error.", e);
        } catch (SdkClientException e) {
            log.error("AWS SDK client error occurred. Email not sent! Error: {}", e.getMessage());
            throw new DynamoSesException("AWS SES Mail sending disabled due to SDK client error.", e);
        } catch (SdkException e) {
            log.error("AWS SDK error occurred. Email not sent! Error: {}", e.getMessage());
            throw new DynamoSesException("AWS SES Mail sending disabled due to SDK error.", e);
        }

    }

}
