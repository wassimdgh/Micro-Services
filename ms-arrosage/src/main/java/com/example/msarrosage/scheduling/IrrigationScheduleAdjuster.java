package com.example.msarrosage.scheduling;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.example.msarrosage.clients.MeteoClient;
import com.example.msarrosage.dto.PrevisionResponse;
import com.example.msarrosage.entities.ProgrammeArrosage;
import com.example.msarrosage.repositories.ProgrammeArrosageRepository;
import com.example.msarrosage.services.IArrosageService;

@Component
@ConditionalOnProperty(name = "spring.scheduling.enabled", havingValue = "true", matchIfMissing = true)
public class IrrigationScheduleAdjuster {

    private static final Logger log = LoggerFactory.getLogger(IrrigationScheduleAdjuster.class);

    private final IArrosageService arrosageService;
    private final ProgrammeArrosageRepository programmeRepo;
    private final MeteoClient meteoClient;

    public IrrigationScheduleAdjuster(IArrosageService arrosageService,
                                      ProgrammeArrosageRepository programmeRepo,
                                      MeteoClient meteoClient) {
        this.arrosageService = arrosageService;
        this.programmeRepo = programmeRepo;
        this.meteoClient = meteoClient;
    }

    /**
     * Automatically adjust irrigation schedules based on weather forecasts
     * Runs every 4 hours (14400000 ms)
     * 
     * Process:
     * 1. Find all PENDING programmes for the next 7 days
     * 2. Fetch latest weather forecasts for each station
     * 3. Auto-adjust volumes and postpone if necessary
     * 4. Log all adjustments for audit trail
     */
    @Scheduled(fixedDelay = 14400000, initialDelay = 120000)
    @Transactional
    public void autoAdjustSchedules() {
        log.info("=== Starting automatic irrigation schedule adjustment ===");

        try {
            // Get upcoming programmes for next 7 days
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime sevenDaysLater = now.plusDays(7);

            List<ProgrammeArrosage> upcomingProgrammes = programmeRepo
                .findUpcomingProgrammesByDateRange(now, sevenDaysLater);

            log.info("Found {} upcoming irrigation schedules to evaluate", upcomingProgrammes.size());

            if (upcomingProgrammes.isEmpty()) {
                log.info("No upcoming schedules found for adjustment");
                return;
            }

            // Group programmes by station/date and adjust based on weather
            for (ProgrammeArrosage programme : upcomingProgrammes) {
                adjustScheduleBasedOnWeather(programme);
            }

            log.info("=== Automatic schedule adjustment completed ===");

        } catch (Exception e) {
            log.error("Error during automatic schedule adjustment: ", e);
        }
    }

    /**
     * Adjust a single irrigation schedule based on latest weather forecast
     * 
     * Decision Logic:
     * - RAIN (> 5mm): Reduce water volume
     * - HEAVY RAIN (> 15mm): Cancel/Postpone irrigation
     * - HIGH TEMP (> 30°C): Increase water volume
     * - STRONG WIND (> 30 km/h): Cancel/Postpone irrigation (evaporation risk)
     */
    @Transactional
    private void adjustScheduleBasedOnWeather(ProgrammeArrosage programme) {
        log.info("Evaluating schedule ID {} for date {}", programme.getId(), programme.getDatePlanifiee());

        try {
            // Fetch forecasts - Note: In real implementation, this calls Meteo Service
            // via MeteoClient to get weather for that specific location
            List<PrevisionResponse> forecasts = meteoClient.getPrevisions(1L); // Default to station 1
            
            if (forecasts == null || forecasts.isEmpty()) {
                log.debug("No forecasts available for adjustment");
                return;
            }

            // Find forecast for the programme date
            LocalDate programmeDate = programme.getDatePlanifiee().toLocalDate();
            PrevisionResponse relevantForecast = forecasts.stream()
                .filter(f -> f.date().equals(programmeDate))
                .findFirst()
                .orElse(null);

            if (relevantForecast == null) {
                log.debug("No forecast found for date: {}", programmeDate);
                return;
            }

            // Trigger the existing adjustment logic via handleWeatherEvent
            // This maintains consistency with manual weather event processing
            arrosageService.handleWeatherEvent(convertForecastToPayload(relevantForecast));

        } catch (Exception e) {
            log.error("Error adjusting schedule ID {}: ", programme.getId(), e);
        }
    }

    /**
     * Convert PrevisionResponse to a Map payload that matches the RabbitMQ event format
     * This allows reusing the existing weather event handling logic
     */
    private java.util.Map<String, Object> convertForecastToPayload(PrevisionResponse forecast) {
        java.util.Map<String, Object> payload = new java.util.HashMap<>();
        payload.put("previsionId", forecast.previsionId());
        payload.put("stationId", forecast.stationId());
        payload.put("date", forecast.date());
        payload.put("temperatureMax", forecast.temperatureMax());
        payload.put("temperatureMin", forecast.temperatureMin());
        payload.put("pluiePrevue", forecast.pluiePrevue());
        payload.put("vent", forecast.vent());
        return payload;
    }
}
