package com.javacore.spring_api_luvine.shared.messaging.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQDLQConfig {
    public static final String EMAIL_DLQ_QUEUE = "email.dlq.queue";
    public static final String EMAIL_DLX_EXCHANGE = "email.dlx.exchange";
    public static final String EMAIL_DLQ_ROUTING_KEY = "email.dlq.routingKey";
    public static final String EMAIL_QUEUE = "email.queue";
    public static final String EMAIL_EXCHANGE = "email.exchange";
    public static final String EMAIL_ROUTING_KEY = "email.routingKey";

    @Bean
    public Queue emailQueue() {
        return QueueBuilder
                .durable(EMAIL_QUEUE)
                .withArgument("x-dead-letter-exchange", EMAIL_DLX_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", EMAIL_DLQ_ROUTING_KEY)
                .build();
    }

    @Bean
    public DirectExchange emailExchange() {
        return new DirectExchange(EMAIL_EXCHANGE);
    }

    @Bean
    public Binding emailBinding() {
        return BindingBuilder
                .bind(emailQueue())
                .to(emailExchange())
                .with(EMAIL_ROUTING_KEY);
    }

    @Bean
    public Queue emailDqlQueue() {
        return new Queue(EMAIL_DLQ_QUEUE, true);
    }

    @Bean
    public DirectExchange emailDlxExchange() {
        return new DirectExchange(EMAIL_DLX_EXCHANGE);
    }

    @Bean
    public Binding emailDlqBinding() {
        return BindingBuilder
                .bind(emailDqlQueue())
                .to(emailDlxExchange())
                .with(EMAIL_DLQ_ROUTING_KEY);
    }
}