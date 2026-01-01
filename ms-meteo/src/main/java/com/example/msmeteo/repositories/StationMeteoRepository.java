package com.example.msmeteo.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.msmeteo.entities.StationMeteo;

public interface StationMeteoRepository extends JpaRepository<StationMeteo, Long> {
}
