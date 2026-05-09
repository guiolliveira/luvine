package com.javacore.spring_api_luvine.shared.messaging.service.producer;

import com.javacore.spring_api_luvine.shared.messaging.config.RabbitMQDLQConfig;
import com.javacore.spring_api_luvine.shared.messaging.dto.EmailMessageRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProducerService {

    private final RabbitTemplate rabbitTemplate;

    public void producer(EmailMessageRequest request) {
        rabbitTemplate.convertAndSend(
                RabbitMQDLQConfig.EMAIL_EXCHANGE,
                RabbitMQDLQConfig.EMAIL_ROUTING_KEY,
                request
        );
    }
}