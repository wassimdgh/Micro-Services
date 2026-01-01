package com.example.msmeteo.web;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.msmeteo.dto.PrevisionRequest;
import com.example.msmeteo.entities.Prevision;
import com.example.msmeteo.entities.StationMeteo;
import com.example.msmeteo.services.IWeatherService;

@RestController
@RequestMapping("/api/meteo")
public class MeteoController {

    private final IWeatherService weatherService;

    public MeteoController(IWeatherService weatherService) {
        this.weatherService = weatherService;
    }

    @GetMapping("/stations")
    public ResponseEntity<List<StationMeteo>> listStations() {
        return ResponseEntity.ok(weatherService.getAllStations());
    }

    @PostMapping("/stations")
    public ResponseEntity<StationMeteo> createStation(@RequestBody StationMeteo station) {
        return ResponseEntity.ok(weatherService.createStation(station));
    }

    @GetMapping("/stations/{stationId}")
    public ResponseEntity<StationMeteo> getStation(@PathVariable Long stationId) {
        return ResponseEntity.ok(weatherService.getStation(stationId));
    }

    @GetMapping("/previsions")
    public ResponseEntity<List<Prevision>> getAllPrevisions() {
        List<StationMeteo> stations = weatherService.getAllStations();
        List<Prevision> allPrevisions = new java.util.ArrayList<>();
        for (StationMeteo station : stations) {
            allPrevisions.addAll(weatherService.findUpcomingByStation(station.getId()));
        }
        return ResponseEntity.ok(allPrevisions);
    }

    @GetMapping("/previsions/{stationId}")
    public ResponseEntity<List<Prevision>> getPrevisions(@PathVariable Long stationId) {
        return ResponseEntity.ok(weatherService.findUpcomingByStation(stationId));
    }

    @PostMapping("/previsions")
    public ResponseEntity<Prevision> createPrevision(@RequestBody PrevisionRequest request) {
        return ResponseEntity.ok(weatherService.createPrevision(request));
    }
}