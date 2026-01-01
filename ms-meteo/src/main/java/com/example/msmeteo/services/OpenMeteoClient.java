package com.example.msmeteo.services;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.example.msmeteo.entities.StationMeteo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@Component
public class OpenMeteoClient {

    private static final Logger log = LoggerFactory.getLogger(OpenMeteoClient.class);

    private final RestTemplate restTemplate;
    private final String baseUrl;
    private final int forecastDays;
    private final String hourlyParams;
    private final String timezone;

    public OpenMeteoClient(RestTemplateBuilder restTemplateBuilder,
                           @Value("${app.weather.base-url:https://api.open-meteo.com/v1/forecast}") String baseUrl,
                           @Value("${app.weather.forecast-days:7}") int forecastDays,
                           @Value("${app.weather.hourly:temperature_2m,precipitation,wind_speed_10m}") String hourlyParams,
                           @Value("${app.weather.timezone:auto}") String timezone) {
        this.restTemplate = restTemplateBuilder.build();
        this.baseUrl = baseUrl;
        this.forecastDays = forecastDays;
        this.hourlyParams = hourlyParams;
        this.timezone = timezone;
    }

    public List<DailyForecast> fetchDailyForecasts(StationMeteo station) {
        if (station.getLatitude() == null || station.getLongitude() == null) {
            log.warn("Station {} missing coordinates, skipping Open-Meteo call", station.getId());
            return List.of();
        }

        String uri = UriComponentsBuilder.fromHttpUrl(baseUrl)
            .queryParam("latitude", station.getLatitude())
            .queryParam("longitude", station.getLongitude())
            .queryParam("hourly", hourlyParams)
            .queryParam("forecast_days", forecastDays)
            .queryParam("timezone", timezone)
            .build()
            .toUriString();

        try {
            log.debug("Calling Open-Meteo for station {} at {}", station.getId(), uri);
            OpenMeteoResponse response = restTemplate.getForObject(uri, OpenMeteoResponse.class);
            return toDailyForecasts(response);
        } catch (RestClientException ex) {
            log.error("Open-Meteo call failed for station {}: {}", station.getId(), ex.getMessage());
            return List.of();
        }
    }

    private List<DailyForecast> toDailyForecasts(OpenMeteoResponse response) {
        if (response == null || response.hourly() == null || response.hourly().time() == null) {
            return List.of();
        }

        List<String> times = response.hourly().time();
        List<Double> temps = response.hourly().temperature2m();
        List<Double> rains = response.hourly().precipitation();
        List<Double> winds = response.hourly().windSpeed10m();

        int size = times.size();
        if (temps == null || rains == null || winds == null || temps.size() != size || rains.size() != size || winds.size() != size) {
            log.warn("Open-Meteo response size mismatch: time={}, temp={}, rain={}, wind={}",
                size, temps != null ? temps.size() : -1, rains != null ? rains.size() : -1, winds != null ? winds.size() : -1);
            return List.of();
        }

        Map<LocalDate, DailyAccumulator> accumulator = new LinkedHashMap<>();
        for (int i = 0; i < size; i++) {
            LocalDate date = parseDate(times.get(i));
            if (date == null) {
                continue;
            }
            DailyAccumulator acc = accumulator.computeIfAbsent(date, d -> new DailyAccumulator());
            acc.register(temps.get(i), rains.get(i), winds.get(i));
        }

        List<DailyForecast> result = new ArrayList<>();
        accumulator.forEach((date, acc) -> {
            if (acc.count > 0) {
                double maxTemp = acc.maxTemp == Double.NEGATIVE_INFINITY ? 0d : acc.maxTemp;
                double minTemp = acc.minTemp == Double.POSITIVE_INFINITY ? 0d : acc.minTemp;
                double avgWind = acc.count > 0 ? acc.windSum / acc.count : 0d;
                result.add(new DailyForecast(
                    date,
                    round(maxTemp),
                    round(minTemp),
                    round(acc.totalRain),
                    round(avgWind)
                ));
            }
        });
        return result;
    }

    private LocalDate parseDate(String isoDateTime) {
        try {
            return LocalDate.parse(isoDateTime.substring(0, 10));
        } catch (DateTimeParseException | IndexOutOfBoundsException ex) {
            log.warn("Cannot parse Open-Meteo time value: {}", isoDateTime);
            return null;
        }
    }

    private double round(double value) {
        return Math.round(value * 10.0) / 10.0;
    }

    public int getForecastDays() {
        return forecastDays;
    }

    private static class DailyAccumulator {
        double maxTemp = Double.NEGATIVE_INFINITY;
        double minTemp = Double.POSITIVE_INFINITY;
        double totalRain = 0d;
        double windSum = 0d;
        int count = 0;

        void register(Double temp, Double rain, Double wind) {
            boolean hasValue = false;
            if (temp != null) {
                maxTemp = Math.max(maxTemp, temp);
                minTemp = Math.min(minTemp, temp);
                hasValue = true;
            }
            if (rain != null) {
                totalRain += rain;
                hasValue = true;
            }
            if (wind != null) {
                windSum += wind;
                hasValue = true;
            }
            if (hasValue) {
                count++;
            }
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record OpenMeteoResponse(Hourly hourly) { }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record Hourly(List<String> time,
                          @JsonProperty("temperature_2m") List<Double> temperature2m,
                          @JsonProperty("precipitation") List<Double> precipitation,
                          @JsonProperty("wind_speed_10m") List<Double> windSpeed10m) { }

    public record DailyForecast(LocalDate date,
                                double temperatureMax,
                                double temperatureMin,
                                double pluiePrevue,
                                double vent) { }
}
