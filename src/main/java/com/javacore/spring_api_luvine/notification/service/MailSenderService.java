package com.javacore.spring_api_luvine.notification.service;

import com.javacore.spring_api_luvine.notification.config.MailSenderProperties;
import com.javacore.spring_api_luvine.notification.dto.MailSenderRequest;
import com.javacore.spring_api_luvine.shared.util.EmailMask;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.UnsupportedEncodingException;

@Slf4j
@Service
@RequiredArgsConstructor
@EnableConfigurationProperties(MailSenderProperties.class)
public class MailSenderService {

    private final JavaMailSender javaMailSender;
    private final TemplateEngine templateEngine;
    private final MailSenderProperties properties;

    public void sendEmail(MailSenderRequest request) {
        String maskedEmail = EmailMask.mask(request.to());

        log.info("event=email_send_attempt to={}", maskedEmail);

        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            Context context = new Context();
            context.setVariable("name", request.name());
            context.setVariable("code", request.code());

            String html = templateEngine.process("email-template", context);

            helper.setTo(request.to());
            helper.setSubject("Email de Verificação");
            helper.setText(html, true);
            helper.setFrom(properties.fromEmail(), "Luvine");

            javaMailSender.send(message);

            log.info("event=email_sent to={}", maskedEmail);
        } catch (MessagingException | UnsupportedEncodingException | RuntimeException ex) {
            log.error("event=email_send_failed to={}", maskedEmail, ex);
        }
    }
}