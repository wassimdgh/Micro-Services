package com.example.msmeteo.services;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.msmeteo.dto.PrevisionRequest;
import com.example.msmeteo.entities.Prevision;
import com.example.msmeteo.entities.StationMeteo;
import com.example.msmeteo.messaging.WeatherEventPublisher;
import com.example.msmeteo.repositories.PrevisionRepository;
import com.example.msmeteo.repositories.StationMeteoRepository;

@Service
public class WeatherServiceImpl implements IWeatherService {

    private final StationMeteoRepository stationRepo;
    private final PrevisionRepository previsionRepo;
    private final WeatherEventPublisher eventPublisher;

    public WeatherServiceImpl(StationMeteoRepository stationRepo,
                              PrevisionRepository previsionRepo,
                              @Autowired(required = false) WeatherEventPublisher eventPublisher) {
        this.stationRepo = stationRepo;
        this.previsionRepo = previsionRepo;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Prevision> findUpcomingByStation(Long stationId) {
        StationMeteo station = stationRepo.findById(stationId)
            .orElseThrow(() -> new IllegalArgumentException("Station introuvable : " + stationId));
        return previsionRepo.findByStationAndDateGreaterThanEqual(station, LocalDate.now());
    }

    @Override
    @Transactional
    public Prevision createPrevision(PrevisionRequest request) {
        StationMeteo station = stationRepo.findById(request.stationId())
            .orElseThrow(() -> new IllegalArgumentException("Station introuvable : " + request.stationId()));

        Prevision prevision = Prevision.builder()
            .station(station)
            .date(request.date())
            .temperatureMax(request.temperatureMax())
            .temperatureMin(request.temperatureMin())
            .pluiePrevue(request.pluiePrevue())
            .vent(request.vent())
            .build();

        return savePrevision(prevision);
    }

    @Override
    @Transactional
    public Prevision savePrevision(Prevision prevision) {
        Prevision saved = previsionRepo.save(prevision);
        if (eventPublisher != null) {
            eventPublisher.publishWeatherChange(saved);
        }
        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public List<StationMeteo> getAllStations() {
        return stationRepo.findAll();
    }

    @Override
    @Transactional
    public StationMeteo createStation(StationMeteo station) {
        return stationRepo.save(station);
    }

    @Override
    @Transactional(readOnly = true)
    public StationMeteo getStation(Long stationId) {
        return stationRepo.findById(stationId)
            .orElseThrow(() -> new IllegalArgumentException("Station introuvable : " + stationId));
    }
}
