package main.java.com.example.msmeteo.web;

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
import com.example.msmeteo.services.IWeatherService;

@RestController
@RequestMapping("/api/meteo")
public class MeteoController {

    private final IWeatherService weatherService;

    public MeteoController(IWeatherService weatherService) {
        this.weatherService = weatherService;
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
