package net.breezeware.dynamo.aws.ses.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;

import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;

import net.breezeware.dynamo.aws.ses.entity.Attachment;
import net.breezeware.dynamo.aws.ses.exception.DynamoSesException;
import net.breezeware.dynamo.aws.ses.service.api.SesService;
import net.breezeware.dynamo.aws.ses.service.api.SmtpService;

import lombok.extern.slf4j.Slf4j;

import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.CreateTemplateRequest;
import software.amazon.awssdk.services.ses.model.GetTemplateRequest;
import software.amazon.awssdk.services.ses.model.GetTemplateResponse;
import software.amazon.awssdk.services.ses.model.SendEmailRequest;
import software.amazon.awssdk.services.ses.model.SendEmailResponse;
import software.amazon.awssdk.services.ses.model.SendRawEmailRequest;
import software.amazon.awssdk.services.ses.model.SendRawEmailResponse;
import software.amazon.awssdk.services.ses.model.SendTemplatedEmailRequest;
import software.amazon.awssdk.services.ses.model.SendTemplatedEmailResponse;
import software.amazon.awssdk.services.ses.model.Template;

@Slf4j
@ExtendWith(MockitoExtension.class)
class SesServiceImplTest {

    @Mock
    private static SesClient sesClient;
    @Mock
    private static JavaMailSender javaMailSender;

    private SesService sesService;
    private SmtpService smtpService;

    @BeforeEach
    void setUp() {
        sesService = new SesServiceImpl(sesClient);
        smtpService = new SmtpServiceImpl(javaMailSender);
    }

    @Test
    void sendsEmail() throws DynamoSesException {
        log.info("Testing sendsEmail()");

        // given
        String from = "from@example.com";
        String to = "to@example.com";
        String subject = "Unit testing SES service";
        String body = "Unit testing SES service using Junit5 and Mockito";
        // when
        when(sesClient.sendEmail(any(SendEmailRequest.class)))
                .thenReturn(SendEmailResponse.builder().messageId("mail-sent-123").build());
        // then
        String messageId = sesService.sendMail(from, to, subject, body);
        assertThat(messageId).isEqualTo("mail-sent-123");

        log.info("Completed testing sendsEmail()");
    }

    @Test
    void sendsEmailWithAttachment() throws DynamoSesException, IOException {
        log.info("Testing sendsEmail()");

        // given
        String from = "from@example.com";
        List<String> to = List.of("to@example.com");
        List<String> cc = List.of("cc@example.com");
        List<String> bcc = List.of("bcc@example.com");
        String subject = "Unit testing SES service";
        String body = "Unit testing SES service using Junit5 and Mockito";
        File attachment = Path.of("src/test/resources/test.txt").toFile();
        byte[] attachmentByteArr = Files.readAllBytes(attachment.toPath());
        // when
        when(sesClient.sendRawEmail(any(SendRawEmailRequest.class)))
                .thenReturn(SendRawEmailResponse.builder().messageId("mail-sent-123").build());
        // then
        String messageId = sesService.sendMail(from, to, cc, bcc, subject, body,
                List.of(Attachment.builder().attachmentContent(attachmentByteArr)
                        .attachmentMetaData(Attachment.MetaData.builder().attachmentName(attachment.getName())
                                .attachmentSize(String.valueOf(Files.size(attachment.toPath())))
                                .attachmentType(Files.probeContentType(attachment.toPath())).build())
                        .build()));
        assertThat(messageId).isEqualTo("mail-sent-123");
        log.info("Completed testing sendsEmail()");
    }

    @Test
    void createsEmailTemplate() throws DynamoSesException {
        log.info("Testing createsEmailTemplate()");

        // given
        String templateName = "email-template-name";
        String subject = "email subject";
        String htmlPart = "<h1>HTML part</h1>";
        String textPart = "Text part";
        // when
        sesService.createEmailTemplate(templateName, subject, htmlPart, textPart);
        // then
        verify(sesClient, times(1)).createTemplate(any(CreateTemplateRequest.class));

        log.info("Completed testing createEmailTemplate()");
    }

    @Test
    void sendsTemplatedEmail() throws DynamoSesException {
        log.info("Testing sendsTemplatedEmail()");

        // given
        String from = "from@example.com";
        String to = "to@example.com";
        String templateName = "template-name";
        String templateData = "{\"name\":\"Unit test\"}";
        Map<String, Object> templateDataMap = Map.of("name", "Unit test");
        Object templateDataObj = JSONObject.wrap(templateDataMap);
        // when
        when(sesClient.sendTemplatedEmail(any(SendTemplatedEmailRequest.class)))
                .thenReturn(SendTemplatedEmailResponse.builder().messageId("mail-sent-123").build());
        // then
        String messageId = sesService.sendTemplatedEmail(from, to, templateName, templateDataMap);
        assertThat(templateData).isEqualTo(templateDataObj.toString());
        assertThat(messageId).isEqualTo("mail-sent-123");

        log.info("Completed testing sendsTemplatedEmail()");
    }

    @Test
    void sendsSmtpEmail() throws DynamoSesException {
        log.info("Testing sendsSmtpEmail()");

        // given
        String from = "from@example.com";
        String to = "to@example.com";
        String subject = "Unit testing SES service";
        String htmlBody = "<h1>HTML part</h1>";
        final Session mailSession = Session.getDefaultInstance(new Properties());
        // when
        when(javaMailSender.createMimeMessage()).thenReturn(new MimeMessage(mailSession));
        smtpService.sendSmtpMail(from, to, subject, htmlBody);
        // then
        verify(javaMailSender, times(1)).send(any(MimeMessage.class));

        log.info("Completed testing sendsSmtpEmail()");
    }

    @Test
    void testGetEmailTemplate() throws DynamoSesException {
        log.info("Testing testGetEmailTemplate()");
        // Given
        String templateName = "template-name";
        String templateHtmlPart = "<html><body>Template Content</body></html>";
        String templateSubjectPart = "Template Subject";
        String templateTextPart = "Template Text";
        GetTemplateRequest getTemplateRequest = GetTemplateRequest.builder().templateName(templateName).build();

        // Mocking the getTemplate response
        GetTemplateResponse getTemplateResponse = GetTemplateResponse.builder()
                .template(Template.builder().templateName(templateName).htmlPart(templateHtmlPart)
                        .subjectPart(templateSubjectPart).textPart(templateTextPart).build())
                .build();
        when(sesClient.getTemplate(any(GetTemplateRequest.class))).thenReturn(getTemplateResponse);

        // When
        net.breezeware.dynamo.aws.ses.entity.Template result = sesService.getEmailTemplate(templateName);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getTemplateName()).isEqualTo(templateName);
        assertThat(result.getHtmlPart()).isEqualTo(templateHtmlPart);
        assertThat(result.getSubjectPart()).isEqualTo(templateSubjectPart);
        assertThat(result.getTextPart()).isEqualTo(templateTextPart);

        // Verify that the sesClient.getTemplate was called with the correct request
        verify(sesClient).getTemplate(eq(getTemplateRequest));
        log.info("Completed testing testGetEmailTemplate()");
    }
}