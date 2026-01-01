package com.example.msarrosage.web;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.msarrosage.dto.JournalRequest;
import com.example.msarrosage.dto.PrevisionResponse;
import com.example.msarrosage.dto.ProgrammeRequest;
import com.example.msarrosage.entities.JournalArrosage;
import com.example.msarrosage.entities.ProgrammeArrosage;
import com.example.msarrosage.services.IArrosageService;

@RestController
@RequestMapping("/api/arrosage")
public class ArrosageController {

    private final IArrosageService arrosageService;

    public ArrosageController(IArrosageService arrosageService) {
        this.arrosageService = arrosageService;
    }

    @GetMapping("/programmes")
    public ResponseEntity<List<ProgrammeArrosage>> listProgrammes() {
        return ResponseEntity.ok(arrosageService.getProgrammes());
    }

    @PostMapping("/programmes")
    public ResponseEntity<ProgrammeArrosage> createProgramme(@RequestBody ProgrammeRequest request) {
        return ResponseEntity.ok(arrosageService.createProgramme(request));
    }

    @GetMapping("/programmes/{id}")
    public ResponseEntity<ProgrammeArrosage> getProgramme(@PathVariable Long id) {
        return ResponseEntity.ok(arrosageService.getProgrammeById(id));
    }

    @PutMapping("/programmes/{id}")
    public ResponseEntity<ProgrammeArrosage> updateProgramme(@PathVariable Long id, @RequestBody ProgrammeRequest request) {
        return ResponseEntity.ok(arrosageService.updateProgramme(id, request));
    }

    @DeleteMapping("/programmes/{id}")
    public ResponseEntity<Void> deleteProgramme(@PathVariable Long id) {
        arrosageService.deleteProgramme(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/journal")
    public ResponseEntity<List<JournalArrosage>> listJournal() {
        return ResponseEntity.ok(arrosageService.getJournal());
    }

    @PostMapping("/journal")
    public ResponseEntity<JournalArrosage> logJournal(@RequestBody JournalRequest request) {
        return ResponseEntity.ok(arrosageService.logExecution(request));
    }

    @DeleteMapping("/journal/{id}")
    public ResponseEntity<Void> deleteJournal(@PathVariable Long id) {
        arrosageService.deleteJournalEntry(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/previsions/{stationId}")
    public ResponseEntity<List<PrevisionResponse>> getPrevisions(@PathVariable Long stationId) {
        return ResponseEntity.ok(arrosageService.fetchPrevisions(stationId));
    }
}
