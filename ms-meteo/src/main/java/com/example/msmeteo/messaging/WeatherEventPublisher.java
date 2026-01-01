package com.example.msmeteo.messaging;

import java.util.HashMap;
import java.util.Map;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.example.msmeteo.entities.Prevision;

@Component
@ConditionalOnProperty(name = "spring.rabbitmq.enabled", havingValue = "true", matchIfMissing = false)
public class WeatherEventPublisher {

    private final RabbitTemplate rabbitTemplate;
    private final String exchange;
    private final String routingKey;

    public WeatherEventPublisher(RabbitTemplate rabbitTemplate,
                                 @Value("${irrigation.exchange}") String exchange,
                                 @Value("${irrigation.routing-key}") String routingKey) {
        this.rabbitTemplate = rabbitTemplate;
        this.exchange = exchange;
        this.routingKey = routingKey;
    }

    public void publishWeatherChange(Prevision prevision) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("previsionId", prevision.getId());
        payload.put("stationId", prevision.getStation().getId());
        payload.put("date", prevision.getDate());
        payload.put("temperatureMax", prevision.getTemperatureMax());
        payload.put("temperatureMin", prevision.getTemperatureMin());
        payload.put("pluiePrevue", prevision.getPluiePrevue());
        payload.put("vent", prevision.getVent());
        rabbitTemplate.convertAndSend(exchange, routingKey, payload);
    }
}
