package com.example.msmeteo.services;

import java.util.List;

import com.example.msmeteo.dto.PrevisionRequest;
import com.example.msmeteo.entities.Prevision;
import com.example.msmeteo.entities.StationMeteo;

public interface IWeatherService {
    
    List<Prevision> findUpcomingByStation(Long stationId);
    
    Prevision createPrevision(PrevisionRequest request);
    
    Prevision savePrevision(Prevision prevision);
    
    List<StationMeteo> getAllStations();
    
    StationMeteo createStation(StationMeteo station);
    
    StationMeteo getStation(Long stationId);
}
