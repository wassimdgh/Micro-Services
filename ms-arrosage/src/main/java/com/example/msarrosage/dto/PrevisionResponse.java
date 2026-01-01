package com.example.msarrosage.dto;

import java.time.LocalDate;

public record PrevisionResponse(
        Long previsionId,
        Long stationId,
        LocalDate date,
        Double temperatureMax,
        Double temperatureMin,
        Double pluiePrevue,
        Double vent) {
}
