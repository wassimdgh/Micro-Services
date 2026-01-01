package com.example.msarrosage.repositories;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.msarrosage.entities.ProgrammeArrosage;

public interface ProgrammeArrosageRepository extends JpaRepository<ProgrammeArrosage, Long> {
    
    @Query("SELECT p FROM ProgrammeArrosage p WHERE p.parcelleId = :parcelleId AND p.datePlanifiee BETWEEN :startDate AND :endDate")
    List<ProgrammeArrosage> findUpcomingProgrammes(@Param("parcelleId") Long parcelleId, 
                                                    @Param("startDate") LocalDateTime startDate, 
                                                    @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT p FROM ProgrammeArrosage p WHERE p.statut IN ('PLANIFIE', 'REPLANIFIE') AND p.datePlanifiee BETWEEN :startDate AND :endDate")
    List<ProgrammeArrosage> findUpcomingProgrammesByDateRange(@Param("startDate") LocalDateTime startDate, 
                                                               @Param("endDate") LocalDateTime endDate);
}
