package com.javacore.spring_api_luvine.shared.messaging.service.consumer;

import com.javacore.spring_api_luvine.notification.dto.MailSenderRequest;
import com.javacore.spring_api_luvine.notification.service.MailSenderService;
import com.javacore.spring_api_luvine.shared.messaging.config.RabbitMQDLQConfig;
import com.javacore.spring_api_luvine.shared.messaging.dto.EmailMessageRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ConsumerService {

    private final MailSenderService mailSenderService;

    @RabbitListener(queues = RabbitMQDLQConfig.EMAIL_QUEUE)
    public void consumer(EmailMessageRequest request) {
        mailSenderService.sendEmail(new MailSenderRequest(
                request.to(),
                request.subject(),
                request.body()
        ));
    }
}