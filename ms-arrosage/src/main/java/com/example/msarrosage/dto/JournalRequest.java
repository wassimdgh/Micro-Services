package com.example.msarrosage.dto;

import java.time.LocalDateTime;

public record JournalRequest(
        Long programmeId,
        LocalDateTime dateExecution,
        Double volumeReel,
        String remarque) {
}
