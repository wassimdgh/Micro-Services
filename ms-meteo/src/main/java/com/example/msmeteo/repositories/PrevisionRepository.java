package com.example.msmeteo.repositories;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.msmeteo.entities.Prevision;
import com.example.msmeteo.entities.StationMeteo;

public interface PrevisionRepository extends JpaRepository<Prevision, Long> {
    List<Prevision> findByStationAndDateGreaterThanEqual(StationMeteo station, LocalDate date);

    void deleteByStationAndDateBetween(StationMeteo station, LocalDate start, LocalDate end);
}
