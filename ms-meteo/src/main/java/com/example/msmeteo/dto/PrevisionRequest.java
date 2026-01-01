package com.example.msmeteo.dto;

import java.time.LocalDate;

public record PrevisionRequest(
        Long stationId,
        LocalDate date,
        Double temperatureMax,
        Double temperatureMin,
        Double pluiePrevue,
        Double vent) {
}
