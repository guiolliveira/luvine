package com.javacore.spring_api_luvine.common.messaging.service.producer;

import com.javacore.spring_api_luvine.common.messaging.config.RabbitMQDLQConfig;
import com.javacore.spring_api_luvine.common.messaging.dto.EmailMessageRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;
import static org.mockito.BDDMockito.*;

@DisplayName("ProducerService")
@ExtendWith(MockitoExtension.class)
class ProducerServiceTest {

    @Mock private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private ProducerService producerService;

    // --- HELPERS ------------------------------------------------------------------

    private EmailMessageRequest validRequest() {
        return new EmailMessageRequest(
                "user@example.com",
                "User",
                "Assunto de Teste",
                "template-teste",
                Map.of("key", "value")
        );
    }

    // --- PRODUCER ------------------------------------------------------------------

    @Nested
    @DisplayName("producer()")
    class Producer {

        @Test
        @DisplayName("deve publicar mensagem com exchange e routingKey corretos")
        void producer_validRequest_sendsToCorrectExchangeAndRoutingKey() {
            producerService.producer(validRequest());

            then(rabbitTemplate).should().convertAndSend(
                    RabbitMQDLQConfig.EMAIL_EXCHANGE,
                    RabbitMQDLQConfig.EMAIL_ROUTING_KEY,
                    validRequest()
            );
        }

        @Test
        @DisplayName("deve publicar o request completo como payload da mensagem")
        void producer_validRequest_sendsFullRequestAsPayload() {
            EmailMessageRequest request = validRequest();

            producerService.producer(request);

            then(rabbitTemplate).should().convertAndSend(
                    anyString(),
                    anyString(),
                    eq(request)
            );
        }

        @Test
        @DisplayName("deve completar sem exceção quando RabbitTemplate publica com sucesso")
        void producer_validRequest_doesNotThrow() {
            assertThatNoException().isThrownBy(() -> producerService.producer(validRequest()));
        }

        @Test
        @DisplayName("deve propagar exceção quando RabbitTemplate falha ao publicar")
        void producer_rabbitTemplateThrows_propagatesException() {
            willThrow(new AmqpException("connection refused"))
                    .given(rabbitTemplate).convertAndSend(anyString(), anyString(), any(Object.class));

            assertThatExceptionOfType(AmqpException.class)
                    .isThrownBy(() -> producerService.producer(validRequest()));
        }

        @Test
        @DisplayName("não deve engolir exceção silenciosamente quando publicação falha")
        void producer_rabbitTemplateThrows_doesNotSwallowException() {
            RuntimeException cause = new RuntimeException("erro interno");
            willThrow(cause)
                    .given(rabbitTemplate).convertAndSend(anyString(), anyString(), any(Object.class));

            assertThatExceptionOfType(RuntimeException.class)
                    .isThrownBy(() -> producerService.producer(validRequest()))
                    .isSameAs(cause);
        }

        @Test
        @DisplayName("deve chamar convertAndSend exatamente uma vez por publicação")
        void producer_validRequest_callsConvertAndSendExactlyOnce() {
            producerService.producer(validRequest());

            then(rabbitTemplate).should(times(1))
                    .convertAndSend(anyString(), anyString(), any(Object.class));
        }
    }
}