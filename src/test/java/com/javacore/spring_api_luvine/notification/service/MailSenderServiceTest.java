package com.javacore.spring_api_luvine.notification.service;

import com.javacore.spring_api_luvine.notification.infrastructure.config.MailSenderProperties;
import com.javacore.spring_api_luvine.notification.application.dto.MailSenderRequest;
import com.javacore.spring_api_luvine.notification.infrastructure.provider.MailSenderService;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.*;

@DisplayName("MailSenderService")
@ExtendWith(MockitoExtension.class)
class MailSenderServiceTest {

    @Mock private JavaMailSender javaMailSender;
    @Mock private TemplateEngine templateEngine;
    @Mock private MailSenderProperties properties;

    @InjectMocks
    private MailSenderService mailSenderService;

    // --- HELPERS ------------------------------------------------------------------

    private static final String VALID_EMAIL    = "user@example.com";
    private static final String VALID_NAME     = "User";
    private static final String VALID_SUBJECT  = "Assunto de Teste";
    private static final String VALID_TEMPLATE = "template-teste";
    private static final String FROM_EMAIL     = "no-reply@luvine.com";
    private static final String RENDERED_HTML  = "<html><body>Olá User</body></html>";

    private MailSenderRequest validRequest() {
        return new MailSenderRequest(
                VALID_EMAIL,
                VALID_NAME,
                VALID_SUBJECT,
                VALID_TEMPLATE,
                Map.of("key", "value")
        );
    }

    private MailSenderRequest requestWithoutVariables() {
        return new MailSenderRequest(
                VALID_EMAIL,
                VALID_NAME,
                VALID_SUBJECT,
                VALID_TEMPLATE,
                null
        );
    }

    private void setupDefaultMocks() {
        MimeMessage mimeMessage = mock(MimeMessage.class);
        given(javaMailSender.createMimeMessage()).willReturn(mimeMessage);
        given(templateEngine.process(eq(VALID_TEMPLATE), any(Context.class))).willReturn(RENDERED_HTML);
        given(properties.fromEmail()).willReturn(FROM_EMAIL);
    }

    // --- SEND EMAIL ------------------------------------------------------------------

    @Nested
    @DisplayName("sendEmail()")
    class SendEmail {

        @Test
        @DisplayName("deve enviar email com sucesso sem lançar exceção")
        void sendEmail_validRequest_doesNotThrow() {
            setupDefaultMocks();

            assertThatNoException().isThrownBy(() -> mailSenderService.sendEmail(validRequest()));
        }

        @Test
        @DisplayName("deve chamar javaMailSender.send() exatamente uma vez")
        void sendEmail_validRequest_callsSendExactlyOnce() {
            setupDefaultMocks();

            mailSenderService.sendEmail(validRequest());

            then(javaMailSender).should(times(1)).send(any(MimeMessage.class));
        }

        @Test
        @DisplayName("deve processar o template correto via TemplateEngine")
        void sendEmail_validRequest_processesCorrectTemplate() {
            setupDefaultMocks();

            mailSenderService.sendEmail(validRequest());

            then(templateEngine).should().process(eq(VALID_TEMPLATE), any(Context.class));
        }

        @Test
        @DisplayName("deve criar MimeMessage a partir do JavaMailSender")
        void sendEmail_validRequest_createsMimeMessage() {
            setupDefaultMocks();

            mailSenderService.sendEmail(validRequest());

            then(javaMailSender).should().createMimeMessage();
        }

        @Test
        @DisplayName("deve usar o fromEmail configurado nas properties")
        void sendEmail_validRequest_usesConfiguredFromEmail() {
            setupDefaultMocks();

            mailSenderService.sendEmail(validRequest());

            then(properties).should().fromEmail();
        }

        @Test
        @DisplayName("não deve lançar exceção quando variables é null")
        void sendEmail_nullVariables_doesNotThrow() {
            setupDefaultMocks();

            assertThatNoException()
                    .isThrownBy(() -> mailSenderService.sendEmail(requestWithoutVariables()));
        }

        @Test
        @DisplayName("não deve propagar exceção quando templateEngine lança RuntimeException")
        void sendEmail_templateEngineThrows_doesNotPropagate() {
            MimeMessage mimeMessage = mock(MimeMessage.class);
            given(javaMailSender.createMimeMessage()).willReturn(mimeMessage);
            given(templateEngine.process(anyString(), any(Context.class)))
                    .willThrow(new RuntimeException("template não encontrado"));

            assertThatNoException().isThrownBy(() -> mailSenderService.sendEmail(validRequest()));

            then(javaMailSender).should(never()).send(any(MimeMessage.class));
        }

        @Test
        @DisplayName("não deve propagar exceção quando javaMailSender.send() lança RuntimeException")
        void sendEmail_mailSenderThrows_doesNotPropagate() {
            setupDefaultMocks();
            willThrow(new RuntimeException("SMTP indisponível"))
                    .given(javaMailSender).send(any(MimeMessage.class));

            assertThatNoException().isThrownBy(() -> mailSenderService.sendEmail(validRequest()));
        }

        @Test
        @DisplayName("não deve chamar send() quando createMimeMessage lança exceção")
        void sendEmail_createMimeMessageThrows_doesNotCallSend() {
            given(javaMailSender.createMimeMessage())
                    .willThrow(new RuntimeException("falha ao criar mensagem"));

            assertThatNoException().isThrownBy(() -> mailSenderService.sendEmail(validRequest()));

            then(javaMailSender).should(never()).send(any(MimeMessage.class));
        }
    }
}