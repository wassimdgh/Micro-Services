package com.example.msarrosage.dto;

import java.time.LocalDateTime;

public record ProgrammeRequest(
        Long parcelleId,
        LocalDateTime datePlanifiee,
        Integer duree,
        Double volumePrevu,
        String statut) {
}
