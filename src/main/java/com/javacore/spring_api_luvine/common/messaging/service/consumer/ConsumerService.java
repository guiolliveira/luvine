package com.javacore.spring_api_luvine.common.messaging.service.consumer;

import com.javacore.spring_api_luvine.notification.application.dto.MailSenderRequest;
import com.javacore.spring_api_luvine.notification.infrastructure.provider.MailSenderProvider;
import com.javacore.spring_api_luvine.common.messaging.config.RabbitMQDLQConfig;
import com.javacore.spring_api_luvine.common.messaging.dto.EmailMessageRequest;
import com.javacore.spring_api_luvine.common.util.EmailMask;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConsumerService {

    private final MailSenderProvider mailSenderService;

    @RabbitListener(queues = RabbitMQDLQConfig.EMAIL_QUEUE)
    public void consumer(EmailMessageRequest request) {
        String maskedEmail = EmailMask.mask(request.to());
        log.info("event=message_received queue={} to={}", RabbitMQDLQConfig.EMAIL_QUEUE, maskedEmail);

        try {
            mailSenderService.sendEmail(new MailSenderRequest(
                    request.to(),
                    request.name(),
                    request.subject(),
                    request.templateName(),
                    request.variables()
            ));
            log.info("event=message_processed queue={} to={}", RabbitMQDLQConfig.EMAIL_QUEUE, maskedEmail);
        } catch (Exception ex) {
            log.error("event=message_processing_failed queue={} to={}", RabbitMQDLQConfig.EMAIL_QUEUE, maskedEmail, ex);
            throw ex;
        }
    }
}