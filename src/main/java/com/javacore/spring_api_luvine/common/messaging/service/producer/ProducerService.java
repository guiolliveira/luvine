package com.javacore.spring_api_luvine.common.messaging.service.producer;

import com.javacore.spring_api_luvine.common.messaging.config.RabbitMQDLQConfig;
import com.javacore.spring_api_luvine.common.messaging.dto.EmailMessageRequest;
import com.javacore.spring_api_luvine.common.util.EmailMask;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProducerService {

    private final RabbitTemplate rabbitTemplate;

    public void producer(EmailMessageRequest request) {
        String maskedEmail = EmailMask.mask(request.to());
        log.info("event=message_publish_attempt exchange={} routingKey={} to={}",
                RabbitMQDLQConfig.EMAIL_EXCHANGE, RabbitMQDLQConfig.EMAIL_ROUTING_KEY, maskedEmail);

        try {
            rabbitTemplate.convertAndSend(
                    RabbitMQDLQConfig.EMAIL_EXCHANGE,
                    RabbitMQDLQConfig.EMAIL_ROUTING_KEY,
                    request
            );
            log.info("event=message_published exchange={} routingKey={} to={}",
                    RabbitMQDLQConfig.EMAIL_EXCHANGE, RabbitMQDLQConfig.EMAIL_ROUTING_KEY, maskedEmail);
        } catch (Exception ex) {
            log.error("event=message_publish_failed exchange={} routingKey={} to={}",
                    RabbitMQDLQConfig.EMAIL_EXCHANGE, RabbitMQDLQConfig.EMAIL_ROUTING_KEY, maskedEmail, ex);
            throw ex;
        }
    }
}