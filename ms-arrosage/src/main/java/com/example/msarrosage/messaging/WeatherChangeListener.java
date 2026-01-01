package com.example.msarrosage.messaging;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.example.msarrosage.services.IArrosageService;

@Component
@ConditionalOnProperty(name = "spring.rabbitmq.enabled", havingValue = "true", matchIfMissing = false)
public class WeatherChangeListener {

    private static final Logger log = LoggerFactory.getLogger(WeatherChangeListener.class);

    private final IArrosageService arrosageService;

    public WeatherChangeListener(IArrosageService arrosageService) {
        this.arrosageService = arrosageService;
    }

    @RabbitListener(queues = "${irrigation.queue}")
    public void onWeatherChange(Map<String, Object> payload) {
        log.info("Message météo reçu: {}", payload);
        arrosageService.handleWeatherEvent(payload);
    }
}
