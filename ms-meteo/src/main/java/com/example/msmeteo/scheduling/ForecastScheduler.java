package com.example.msmeteo.scheduling;

import java.time.LocalDate;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.example.msmeteo.entities.Prevision;
import com.example.msmeteo.entities.StationMeteo;
import com.example.msmeteo.repositories.PrevisionRepository;
import com.example.msmeteo.services.IWeatherService;
import com.example.msmeteo.services.OpenMeteoClient;
import com.example.msmeteo.services.OpenMeteoClient.DailyForecast;

@Component
@ConditionalOnProperty(name = "spring.scheduling.enabled", havingValue = "true", matchIfMissing = true)
public class ForecastScheduler {

    private static final Logger log = LoggerFactory.getLogger(ForecastScheduler.class);
    private final IWeatherService weatherService;
    private final OpenMeteoClient openMeteoClient;
    private final PrevisionRepository previsionRepository;

    public ForecastScheduler(IWeatherService weatherService,
                             OpenMeteoClient openMeteoClient,
                             PrevisionRepository previsionRepository) {
        this.weatherService = weatherService;
        this.openMeteoClient = openMeteoClient;
        this.previsionRepository = previsionRepository;
    }

    /**
     * Fetch forecasts every 6 hours (21600000 ms)
     * In production, this would call a real weather API like Meteo France
     */
    @Scheduled(fixedDelayString = "${app.scheduling.forecast.fixed-delay:21600000}",
              initialDelayString = "${app.scheduling.forecast.initial-delay:60000}")
    @Transactional
    public void fetchAndGenerateForecasts() {
        log.info("=== Starting automatic forecast generation ===");
        
        try {
            List<StationMeteo> stations = weatherService.getAllStations();
            log.info("Found {} stations to process", stations.size());

            for (StationMeteo station : stations) {
                generateForecastsForStation(station);
            }

            log.info("=== Forecast generation completed successfully ===");
        } catch (Exception e) {
            log.error("Error during forecast generation: ", e);
        }
    }

    @Transactional
    private void generateForecastsForStation(StationMeteo station) {
        log.info("Generating forecasts for station: {} (ID: {})", station.getNom(), station.getId());
        List<DailyForecast> forecasts = openMeteoClient.fetchDailyForecasts(station);
        if (forecasts.isEmpty()) {
            log.warn("No forecasts returned for station {}", station.getId());
            return;
        }

        LocalDate today = LocalDate.now();
        LocalDate end = today.plusDays(openMeteoClient.getForecastDays());

        previsionRepository.deleteByStationAndDateBetween(station, today, end);

        forecasts.stream()
            .filter(df -> !df.date().isBefore(today))
            .forEach(df -> {
                Prevision prevision = Prevision.builder()
                    .station(station)
                    .date(df.date())
                    .temperatureMax(df.temperatureMax())
                    .temperatureMin(df.temperatureMin())
                    .pluiePrevue(df.pluiePrevue())
                    .vent(df.vent())
                    .build();
                weatherService.savePrevision(prevision);
                log.debug("Stored forecast for station {} on {}: rain={}mm, tMax={}°C, tMin={}°C, wind={}km/h",
                    station.getId(), df.date(), df.pluiePrevue(), df.temperatureMax(), df.temperatureMin(), df.vent());
            });
    }
}
