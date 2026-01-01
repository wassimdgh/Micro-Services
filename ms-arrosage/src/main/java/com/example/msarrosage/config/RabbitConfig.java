package com.example.msarrosage.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(name = "spring.rabbitmq.enabled", havingValue = "true", matchIfMissing = false)
public class RabbitConfig {

    @Value("${irrigation.exchange}")
    private String exchangeName;

    @Value("${irrigation.queue}")
    private String queueName;

    @Value("${irrigation.routing-key}")
    private String routingKey;

    @Bean
    public TopicExchange irrigationExchange() {
        return new TopicExchange(exchangeName, true, false);
    }

    @Bean
    public Queue arrosageQueue() {
        return new Queue(queueName, true);
    }

    @Bean
    public Binding irrigationBinding(Queue arrosageQueue, TopicExchange irrigationExchange) {
        return BindingBuilder.bind(arrosageQueue).to(irrigationExchange).with(routingKey);
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(messageConverter());
        return factory;
    }

    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
