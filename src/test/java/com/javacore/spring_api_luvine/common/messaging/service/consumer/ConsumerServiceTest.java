package com.javacore.spring_api_luvine.common.messaging.service.consumer;

import com.javacore.spring_api_luvine.notification.application.dto.MailSenderRequest;
import com.javacore.spring_api_luvine.notification.infrastructure.provider.MailSenderService;
import com.javacore.spring_api_luvine.common.messaging.dto.EmailMessageRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;

@DisplayName("ConsumerService")
@ExtendWith(MockitoExtension.class)
class ConsumerServiceTest {

    @Mock private MailSenderService mailSenderService;

    @InjectMocks
    private ConsumerService consumerService;

    // --- HELPERS ------------------------------------------------------------------

    private static final String VALID_EMAIL    = "user@example.com";
    private static final String VALID_NAME     = "User";
    private static final String VALID_SUBJECT  = "Assunto de Teste";
    private static final String VALID_TEMPLATE = "template-teste";

    private EmailMessageRequest validRequest() {
        return new EmailMessageRequest(
                VALID_EMAIL,
                VALID_NAME,
                VALID_SUBJECT,
                VALID_TEMPLATE,
                Map.of("key", "value")
        );
    }

    // --- CONSUMER ------------------------------------------------------------------

    @Nested
    @DisplayName("consumer()")
    class Consumer {

        @Test
        @DisplayName("deve chamar mailSenderService com os dados corretos do request")
        void consumer_validRequest_sendsEmailWithCorrectData() {
            EmailMessageRequest request = validRequest();
            ArgumentCaptor<MailSenderRequest> captor = ArgumentCaptor.forClass(MailSenderRequest.class);

            consumerService.consumer(request);

            then(mailSenderService).should().sendEmail(captor.capture());

            MailSenderRequest sent = captor.getValue();
            assertThat(sent.to()).isEqualTo(VALID_EMAIL);
            assertThat(sent.name()).isEqualTo(VALID_NAME);
            assertThat(sent.subject()).isEqualTo(VALID_SUBJECT);
            assertThat(sent.templateName()).isEqualTo(VALID_TEMPLATE);
            assertThat(sent.variables()).isEqualTo(Map.of("key", "value"));
        }

        @Test
        @DisplayName("deve completar sem exceção quando mailSenderService processa com sucesso")
        void consumer_validRequest_doesNotThrow() {
            assertThatNoException().isThrownBy(() -> consumerService.consumer(validRequest()));
        }

        @Test
        @DisplayName("deve chamar sendEmail exatamente uma vez por mensagem recebida")
        void consumer_validRequest_callsSendEmailExactlyOnce() {
            consumerService.consumer(validRequest());

            then(mailSenderService).should(times(1)).sendEmail(any(MailSenderRequest.class));
        }

        @Test
        @DisplayName("deve propagar exceção quando mailSenderService falha")
        void consumer_mailSenderThrows_propagatesException() {
            willThrow(new RuntimeException("falha ao enviar email"))
                    .given(mailSenderService).sendEmail(any(MailSenderRequest.class));

            assertThatExceptionOfType(RuntimeException.class)
                    .isThrownBy(() -> consumerService.consumer(validRequest()));
        }

        @Test
        @DisplayName("não deve engolir exceção silenciosamente quando processamento falha")
        void consumer_mailSenderThrows_doesNotSwallowException() {
            RuntimeException cause = new RuntimeException("erro interno");
            willThrow(cause).given(mailSenderService).sendEmail(any(MailSenderRequest.class));

            assertThatExceptionOfType(RuntimeException.class)
                    .isThrownBy(() -> consumerService.consumer(validRequest()))
                    .isSameAs(cause);
        }

        @Test
        @DisplayName("deve mapear variables do request corretamente para o MailSenderRequest")
        void consumer_validRequest_mapsVariablesCorrectly() {
            Map<String, Object> variables = Map.of("code", "123456", "name", "João");
            EmailMessageRequest request = new EmailMessageRequest(
                    VALID_EMAIL, VALID_NAME, VALID_SUBJECT, VALID_TEMPLATE, variables
            );
            ArgumentCaptor<MailSenderRequest> captor = ArgumentCaptor.forClass(MailSenderRequest.class);

            consumerService.consumer(request);

            then(mailSenderService).should().sendEmail(captor.capture());
            assertThat(captor.getValue().variables()).isEqualTo(variables);
        }
    }
}