package com.example.msarrosage.services;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import com.example.msarrosage.dto.JournalRequest;
import com.example.msarrosage.dto.PrevisionResponse;
import com.example.msarrosage.dto.ProgrammeRequest;
import com.example.msarrosage.entities.JournalArrosage;
import com.example.msarrosage.entities.ProgrammeArrosage;

public interface IArrosageService {
    
    List<ProgrammeArrosage> getProgrammes();
    
    ProgrammeArrosage getProgrammeById(Long id);
    
    ProgrammeArrosage createProgramme(ProgrammeRequest request);
    
    ProgrammeArrosage updateProgramme(Long id, ProgrammeRequest request);
    
    void deleteProgramme(Long id);
    
    List<JournalArrosage> getJournal();
    
    JournalArrosage logExecution(JournalRequest request);

    void deleteJournalEntry(Long id);
    
    void handleWeatherEvent(Map<String, Object> payload);
    
    List<PrevisionResponse> fetchPrevisions(Long stationId);
    
    void postponeProgramme(Long programmeId, LocalDateTime nouvelleDate);
}
