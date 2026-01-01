package com.example.msarrosage.services;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.msarrosage.clients.MeteoClient;
import com.example.msarrosage.dto.JournalRequest;
import com.example.msarrosage.dto.PrevisionResponse;
import com.example.msarrosage.dto.ProgrammeRequest;
import com.example.msarrosage.entities.JournalArrosage;
import com.example.msarrosage.entities.ProgrammeArrosage;
import com.example.msarrosage.repositories.JournalArrosageRepository;
import com.example.msarrosage.repositories.ProgrammeArrosageRepository;
import org.springframework.beans.factory.annotation.Value;

@Service
public class ArrosageServiceImpl implements IArrosageService {

    private static final Logger log = LoggerFactory.getLogger(ArrosageServiceImpl.class);

    private final ProgrammeArrosageRepository programmeRepo;
    private final JournalArrosageRepository journalRepo;
    private final MeteoClient meteoClient;
    
    @Value("${app.weather.station-ids:}")
    private String stationIdsConfig;

    public ArrosageServiceImpl(ProgrammeArrosageRepository programmeRepo,
                               JournalArrosageRepository journalRepo,
                               MeteoClient meteoClient) {
        this.programmeRepo = programmeRepo;
        this.journalRepo = journalRepo;
        this.meteoClient = meteoClient;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProgrammeArrosage> getProgrammes() {
        return programmeRepo.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public ProgrammeArrosage getProgrammeById(Long id) {
        return programmeRepo.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Programme introuvable : " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<JournalArrosage> getJournal() {
        return journalRepo.findAll();
    }

    @Override
    @Transactional
    public ProgrammeArrosage createProgramme(ProgrammeRequest request) {
        ProgrammeArrosage programme = ProgrammeArrosage.builder()
            .parcelleId(request.parcelleId())
            .datePlanifiee(request.datePlanifiee())
            .duree(request.duree())
            .volumePrevu(request.volumePrevu())
            .statut(request.statut())
            .build();
        return programmeRepo.save(programme);
    }

    @Override
    @Transactional
    public ProgrammeArrosage updateProgramme(Long id, ProgrammeRequest request) {
        ProgrammeArrosage programme = programmeRepo.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Programme introuvable : " + id));
        
        programme.setParcelleId(request.parcelleId());
        programme.setDatePlanifiee(request.datePlanifiee());
        programme.setDuree(request.duree());
        programme.setVolumePrevu(request.volumePrevu());
        programme.setStatut(request.statut());
        
        return programmeRepo.save(programme);
    }

    @Override
    @Transactional
    public void deleteProgramme(Long id) {
        programmeRepo.deleteById(id);
    }

    @Override
    @Transactional
    public JournalArrosage logExecution(JournalRequest request) {
        ProgrammeArrosage programme = programmeRepo.findById(request.programmeId())
            .orElseThrow(() -> new IllegalArgumentException("Programme introuvable : " + request.programmeId()));

        JournalArrosage journal = JournalArrosage.builder()
            .programme(programme)
            .dateExecution(request.dateExecution())
            .volumeReel(request.volumeReel())
            .remarque(request.remarque())
            .build();
        return journalRepo.save(journal);
    }

    @Override
    @Transactional
    public void deleteJournalEntry(Long id) {
        if (!journalRepo.existsById(id)) {
            throw new IllegalArgumentException("Journal introuvable : " + id);
        }
        journalRepo.deleteById(id);
    }

    @Override
    @Transactional
    public void handleWeatherEvent(Map<String, Object> payload) {
        log.info("Événement météo reçu pour ajustement: {}", payload);
        Object stationValue = payload.get("stationId");
        Long stationId = null;
        if (stationValue instanceof Number number) {
            stationId = number.longValue();
        }

        if (stationId != null) {
            List<PrevisionResponse> previsions = meteoClient.getPrevisions(stationId);
            log.debug("Prévisions récupérées: {}", previsions);
            
            // Get upcoming programmes for the next 7 days
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime sevenDaysLater = now.plusDays(7);
            List<ProgrammeArrosage> upcomingProgrammes = programmeRepo
                .findUpcomingProgrammesByDateRange(now, sevenDaysLater);
            
            log.info("Nombre de programmes à ajuster: {}", upcomingProgrammes.size());
            
            // Process each upcoming programme based on weather previsions
            for (ProgrammeArrosage programme : upcomingProgrammes) {
                adjustProgrammeBasedOnWeather(programme, previsions);
            }
        }
    }

    /**
     * Adjusts a programme's volume and schedule based on weather forecasts.
     * Water Adjustment Algorithm:
     * - If pluiePrevue > threshold (5mm) → reduce volume (30% reduction per 10mm of rain)
     * - If temperatureMax > 30°C → increase volume (20% base, scales with temperature)
     * - If vent > 20 km/h → increase volume to compensate for evaporation
     * - Handles edge cases: null values, invalid data, no programmes
     * - Postpones only for extreme conditions (heavy rain > 15mm OR strong wind > 30 km/h)
     */
    private void adjustProgrammeBasedOnWeather(ProgrammeArrosage programme, List<PrevisionResponse> previsions) {
        // Edge case: null programme
        if (programme == null) {
            log.warn("Tentative d'ajustement d'un programme null");
            return;
        }
        
        LocalDate programmeDateOnly = programme.getDatePlanifiee().toLocalDate();
        
        // Find prevision matching the programme date
        PrevisionResponse relevantPrevision = previsions.stream()
            .filter(p -> p.date().equals(programmeDateOnly))
            .findFirst()
            .orElse(null);
        
        if (relevantPrevision == null) {
            log.debug("Aucune prévision trouvée pour la date: {}", programmeDateOnly);
            return;
        }
        
        // Edge case: null volumePrevu
        if (programme.getVolumePrevu() == null || programme.getVolumePrevu() <= 0) {
            log.warn("Volume prévu invalide pour le programme {}: {}", programme.getId(), programme.getVolumePrevu());
            return;
        }
        
        log.info("Ajustement du programme {} basé sur les prévisions météo", programme.getId());
        
        boolean shouldPostpone = false;
        double volumeAdjustmentMultiplier = 1.0; // Multiplier for volume adjustment
        StringBuilder adjustmentReason = new StringBuilder();
        
        // ===== RAIN ADJUSTMENT (Priority 1: Can trigger postponement) =====
        double rainAdjustment = calculateRainAdjustment(relevantPrevision);
        if (rainAdjustment < 1.0) {
            volumeAdjustmentMultiplier *= rainAdjustment;
            adjustmentReason.append(String.format("Pluie: %.1f mm (ajustement: %.1f%%). ", 
                relevantPrevision.pluiePrevue() != null ? relevantPrevision.pluiePrevue() : 0,
                (rainAdjustment - 1.0) * 100));
            
            // Heavy rain threshold - consider postponement
            if (relevantPrevision.pluiePrevue() != null && relevantPrevision.pluiePrevue() > 15.0) {
                shouldPostpone = true;
                log.info("Pluie très importante ({} mm) prévue pour la date: {}. Report de l'irrigation.",
                    relevantPrevision.pluiePrevue(), programmeDateOnly);
            }
        }
        
        // ===== WIND ADJUSTMENT (Priority 2: Can trigger postponement) =====
        double windAdjustment = calculateWindAdjustment(relevantPrevision);
        if (windAdjustment > 1.0) {
            volumeAdjustmentMultiplier *= windAdjustment;
            adjustmentReason.append(String.format("Vent: %.1f km/h (ajustement: +%.1f%%). ", 
                relevantPrevision.vent() != null ? relevantPrevision.vent() : 0,
                (windAdjustment - 1.0) * 100));
        }
        
        // Strong wind threshold - consider postponement
        if (relevantPrevision.vent() != null && relevantPrevision.vent() > 30.0) {
            shouldPostpone = true;
            log.info("Vent fort ({} km/h) prévu pour la date: {}. Report de l'irrigation.", 
                relevantPrevision.vent(), programmeDateOnly);
        }
        
        // ===== TEMPERATURE ADJUSTMENT (Priority 3: Never triggers postponement) =====
        double temperatureAdjustment = calculateTemperatureAdjustment(relevantPrevision);
        if (temperatureAdjustment > 1.0) {
            volumeAdjustmentMultiplier *= temperatureAdjustment;
            adjustmentReason.append(String.format("Température: %.1f°C (ajustement: +%.1f%%). ", 
                relevantPrevision.temperatureMax() != null ? relevantPrevision.temperatureMax() : 0,
                (temperatureAdjustment - 1.0) * 100));
        }
        
        // Apply adjustments
        if (shouldPostpone) {
            // Postpone to next viable date (skip 2 days forward)
            LocalDateTime newDate = programme.getDatePlanifiee().plusDays(2);
            programme.setDatePlanifiee(newDate);
            programme.setStatut("REPLANIFIE");
            log.info("Programme {} reporté au {}. Raison: {}", 
                programme.getId(), newDate, adjustmentReason.toString().trim());
        } else if (Math.abs(volumeAdjustmentMultiplier - 1.0) > 0.01) { // Only adjust if difference > 1%
            // Apply volume adjustment if not postponing
            Double originalVolume = programme.getVolumePrevu();
            Double adjustedVolume = originalVolume * volumeAdjustmentMultiplier;
            
            // Enforce minimum and maximum volume bounds (20% to 200% of original)
            adjustedVolume = Math.max(originalVolume * 0.2, Math.min(adjustedVolume, originalVolume * 2.0));
            
            programme.setVolumePrevu(adjustedVolume);
            programme.setStatut("AJUSTE");
            log.info("Programme {} ajusté. Volume: {}L -> {}L (multiplier: {}x). Raisons: {}", 
                programme.getId(), String.format("%.2f", originalVolume), String.format("%.2f", adjustedVolume), 
                String.format("%.2f", volumeAdjustmentMultiplier), adjustmentReason.toString().trim());
        } else {
            log.debug("Aucun ajustement nécessaire pour le programme {}", programme.getId());
        }
        
        // Persist the changes
        try {
            programmeRepo.save(programme);
        } catch (Exception e) {
            log.error("Erreur lors de la sauvegarde du programme {}: {}", programme.getId(), e.getMessage(), e);
        }
    }

    /**
     * Calculates rain-based volume adjustment.
     * Rain reduces irrigation needs as it supplements natural water supply.
     * 
     * Thresholds:
     * - 0-5mm: No adjustment (threshold not reached)
     * - 5-15mm: 30% reduction (0.7x multiplier)
     * - 15-25mm: 60% reduction (0.4x multiplier)
     * - >25mm: 80% reduction or postpone (0.2x multiplier, but postponement likely)
     */
    private double calculateRainAdjustment(PrevisionResponse prevision) {
        if (prevision.pluiePrevue() == null || prevision.pluiePrevue() <= 5.0) {
            return 1.0; // No adjustment below threshold
        }
        
        double rain = prevision.pluiePrevue();
        double reduction;
        
        if (rain <= 15.0) {
            // Moderate rain: 30% reduction (0.7x)
            reduction = 0.30;
        } else if (rain <= 25.0) {
            // Heavy rain: 60% reduction (0.4x)
            reduction = 0.60;
        } else {
            // Very heavy rain: 80% reduction (0.2x)
            reduction = 0.80;
        }
        
        double adjustmentMultiplier = 1.0 - reduction;
        log.debug("Ajustement pluie: {} mm → multiplicateur de {:.2f}", rain, adjustmentMultiplier);
        return adjustmentMultiplier;
    }

    /**
     * Calculates temperature-based volume adjustment.
     * Higher temperatures increase evaporation, requiring more irrigation.
     * 
     * Scaling:
     * - Below 30°C: No adjustment
     * - 30-35°C: 20% increase (1.2x multiplier)
     * - 35-40°C: 35% increase (1.35x multiplier)
     * - Above 40°C: 50% increase (1.5x multiplier) - but irrigation may be inadvisable
     */
    private double calculateTemperatureAdjustment(PrevisionResponse prevision) {
        if (prevision.temperatureMax() == null || prevision.temperatureMax() <= 30.0) {
            return 1.0; // No adjustment below threshold
        }
        
        double temp = prevision.temperatureMax();
        double increase;
        
        if (temp <= 35.0) {
            // Moderate heat: 20% increase (1.2x)
            increase = 0.20;
        } else if (temp <= 40.0) {
            // High heat: 35% increase (1.35x)
            increase = 0.35;
        } else {
            // Extreme heat: 50% increase (1.5x)
            increase = 0.50;
        }
        
        double adjustmentMultiplier = 1.0 + increase;
        log.debug("Ajustement température: {} °C → multiplicateur de {:.2f}", temp, adjustmentMultiplier);
        return adjustmentMultiplier;
    }

    /**
     * Calculates wind-based volume adjustment.
     * Wind increases evaporation and water loss, requiring more irrigation.
     * 
     * Thresholds:
     * - 0-20 km/h: No adjustment (threshold not reached)
     * - 20-30 km/h: 15% increase (1.15x multiplier)
     * - 30-40 km/h: 30% increase (1.3x multiplier) - but postponement likely
     * - Above 40 km/h: 40% increase (1.4x multiplier) - strong postponement likely
     */
    private double calculateWindAdjustment(PrevisionResponse prevision) {
        if (prevision.vent() == null || prevision.vent() <= 20.0) {
            return 1.0; // No adjustment below threshold
        }
        
        double wind = prevision.vent();
        double increase;
        
        if (wind <= 30.0) {
            // Moderate wind: 15% increase (1.15x)
            increase = 0.15;
        } else if (wind <= 40.0) {
            // Strong wind: 30% increase (1.3x)
            increase = 0.30;
        } else {
            // Very strong wind: 40% increase (1.4x)
            increase = 0.40;
        }
        
        double adjustmentMultiplier = 1.0 + increase;
        log.debug("Ajustement vent: {} km/h → multiplicateur de {:.2f}", wind, adjustmentMultiplier);
        return adjustmentMultiplier;
    }

    @Override
    @Transactional(readOnly = true)
    public List<PrevisionResponse> fetchPrevisions(Long stationId) {
        return meteoClient.getPrevisions(stationId);
    }

    @Override
    @Transactional
    public void postponeProgramme(Long programmeId, LocalDateTime nouvelleDate) {
        ProgrammeArrosage programme = programmeRepo.findById(programmeId)
            .orElseThrow(() -> new IllegalArgumentException("Programme introuvable : " + programmeId));
        programme.setDatePlanifiee(nouvelleDate);
        programme.setStatut("REPLANIFIE");
        programmeRepo.save(programme);
    }

    /**
     * Scheduled task that runs daily to auto-create and adjust upcoming irrigation programmes
     * based on weather forecasts for the next 7 days.
     * 
     * Schedule: Every day at 2:00 AM (02:00)
     * Cron expression: "0 0 2 * * *" (second, minute, hour, day, month, day-of-week)
     * 
     * This task:
     * 1. Fetches weather forecasts for all configured stations
     * 2. Adjusts existing programmes based on weather predictions
     * 3. Auto-creates new programmes for favorable weather conditions
     * 4. Handles errors gracefully to ensure the service continues
     */
    @Scheduled(cron = "0 0 2 * * *")
    @Transactional
    public void autoCreateAndAdjustProgrammesBasedOnWeather() {
        log.info("========== DÉMARRAGE DE LA TÂCHE PLANIFIÉE D'AJUSTEMENT AUTOMATIQUE DES PROGRAMMES ==========");
        log.info("Timestamp: {}", LocalDateTime.now());
        
        long startTime = System.currentTimeMillis();
        int totalAdjustments = 0;
        int totalCreations = 0;
        
        try {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime sevenDaysLater = now.plusDays(7);
            
            log.info("Plage de traitement: {} à {}", now, sevenDaysLater);
            
            // Fetch configured station IDs
            List<Long> stationIds = getConfiguredStationIds();
            
            if (stationIds == null || stationIds.isEmpty()) {
                log.warn("Aucune station météo configurée. Veuillez configurer les stations dans application.properties");
                return;
            }
            
            log.info("Nombre de stations à traiter: {}", stationIds.size());
            
            // Process each weather station
            for (Long stationId : stationIds) {
                try {
                    log.info("Traitement de la station ID: {}", stationId);
                    int stationAdjustments = processAutomaticProgrammesForStation(stationId, now, sevenDaysLater);
                    totalAdjustments += stationAdjustments;
                } catch (Exception e) {
                    log.error("Erreur lors du traitement de la station {}: {}", stationId, e.getMessage(), e);
                    // Continue with next station even if one fails
                }
            }
            
            long duration = System.currentTimeMillis() - startTime;
            log.info("========== TÂCHE PLANIFIÉE COMPLÉTÉE ==========");
            log.info("Résumé: {} programmes ajustés, {} programmes créés, Durée: {} ms", 
                totalAdjustments, totalCreations, duration);
            
        } catch (Exception e) {
            log.error("Erreur critique lors de l'exécution de la tâche planifiée d'ajustement des programmes: {}", 
                e.getMessage(), e);
        }
    }

    /**
     * Alternative scheduled method for more frequent auto-planning (every 6 hours).
     * Useful for real-time weather changes and rapid adjustments.
     * 
     * Schedule: Every 6 hours (02:00, 08:00, 14:00, 20:00)
     * Cron expression: "0 0 0/6 * * *"
     */
    @Scheduled(cron = "0 0 0/6 * * *")
    @Transactional
    public void autoAdjustProgrammesFrequent() {
        log.info("========== TÂCHE RAPIDE D'AJUSTEMENT DES PROGRAMMES (6h) ==========");
        
        try {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime threeDaysLater = now.plusDays(3);
            
            // Get upcoming programmes for the next 3 days only (more urgent adjustments)
            List<ProgrammeArrosage> upcomingProgrammes = programmeRepo
                .findUpcomingProgrammesByDateRange(now, threeDaysLater);
            
            log.info("Nombre de programmes à vérifier rapidement: {}", upcomingProgrammes.size());
            
            List<Long> stationIds = getConfiguredStationIds();
            
            if (stationIds != null && !stationIds.isEmpty()) {
                for (Long stationId : stationIds) {
                    try {
                        List<PrevisionResponse> previsions = meteoClient.getPrevisions(stationId);
                        
                        // Only adjust programmes scheduled for the next 3 days
                        upcomingProgrammes.stream()
                            .filter(p -> !p.getDatePlanifiee().isAfter(threeDaysLater))
                            .forEach(p -> adjustProgrammeBasedOnWeather(p, previsions));
                        
                    } catch (Exception e) {
                        log.warn("Erreur lors de l'ajustement rapide pour station {}: {}", stationId, e.getMessage());
                    }
                }
            }
            
            log.info("========== TÂCHE RAPIDE COMPLÉTÉE ==========");
            
        } catch (Exception e) {
            log.error("Erreur lors de l'ajustement rapide des programmes: {}", e.getMessage(), e);
        }
    }

    /**
     * Process automatic programme creation and adjustment for a specific weather station
     * @return Number of programmes adjusted
     */
    private int processAutomaticProgrammesForStation(Long stationId, LocalDateTime startDate, LocalDateTime endDate) {
        int adjustmentCount = 0;
        try {
            // Fetch weather forecasts
            List<PrevisionResponse> previsions = meteoClient.getPrevisions(stationId);
            log.debug("Prévisions reçues pour la station {}: {} entrées", stationId, previsions.size());
            
            if (previsions == null || previsions.isEmpty()) {
                log.warn("Aucune prévision disponible pour la station {}", stationId);
                return 0;
            }
            
            // Get all programmes scheduled for the date range
            List<ProgrammeArrosage> existingProgrammes = programmeRepo
                .findUpcomingProgrammesByDateRange(startDate, endDate);
            
            log.info("Traitement de {} programmes existants pour la station {}", existingProgrammes.size(), stationId);
            
            // Adjust existing programmes based on weather
            for (ProgrammeArrosage programme : existingProgrammes) {
                if (programme != null) {
                    adjustProgrammeBasedOnWeather(programme, previsions);
                    adjustmentCount++;
                }
            }
            
            // Auto-create programmes for dates with favorable conditions but no existing programme
            int createdCount = createAutoProgammesForFavorableWeather(stationId, previsions, startDate, endDate);
            log.info("Station {}: {} programmes ajustés, {} programmes créés", stationId, adjustmentCount, createdCount);
            
        } catch (Exception e) {
            log.error("Erreur lors du traitement automatique pour la station {}: {}", stationId, e.getMessage(), e);
        }
        
        return adjustmentCount;
    }

    /**
     * Auto-creates irrigation programmes for dates with favorable weather conditions
     * if no programme exists for that parcel on that day.
     * @return Number of programmes created
     */
    private int createAutoProgammesForFavorableWeather(Long stationId, List<PrevisionResponse> previsions, 
                                                        LocalDateTime startDate, LocalDateTime endDate) {
        int createdCount = 0;
        log.debug("Analyse des conditions météo favorables pour création automatique de programmes");
        
        if (previsions == null || previsions.isEmpty()) {
            return 0;
        }
        
        for (PrevisionResponse prevision : previsions) {
            // Check if conditions are favorable for irrigation
            boolean isFavorable = isFavorableForIrrigation(prevision);
            
            if (isFavorable) {
                LocalDate date = prevision.date();
                
                // Only process dates within the specified range
                if (!date.isBefore(startDate.toLocalDate()) && !date.isAfter(endDate.toLocalDate())) {
                    log.debug("Conditions favorables détectées pour la date: {}", date);
                    
                    // In a real scenario, you would:
                    // 1. Get list of parcels associated with this weather station
                    // 2. Check if programmes already exist for each parcel on this date
                    // 3. Create programmes for parcels without existing schedules
                    
                    createdCount += createProgrammeIfNotExists(prevision, stationId);
                }
            }
        }
        
        return createdCount;
    }

    /**
     * Determines if weather conditions are favorable for irrigation
     * Favorable conditions:
     * - Rain < 10mm (not too much natural watering)
     * - Wind < 25 km/h (minimal evaporation loss)
     * - Temperature between 10°C and 40°C (safe operating range)
     */
    private boolean isFavorableForIrrigation(PrevisionResponse prevision) {
        if (prevision == null) {
            return false;
        }
        
        boolean noHeavyRain = prevision.pluiePrevue() == null || prevision.pluiePrevue() < 10.0;
        boolean notTooWindy = prevision.vent() == null || prevision.vent() < 25.0;
        boolean temperatureOk = (prevision.temperatureMax() == null || prevision.temperatureMax() > 10.0) &&
                                (prevision.temperatureMax() == null || prevision.temperatureMax() < 40.0);
        
        return noHeavyRain && notTooWindy && temperatureOk;
    }

    /**
     * Creates a programme for a parcel on a specific date if one doesn't already exist.
     * @return 1 if created, 0 if not created (already exists or error)
     */
    private int createProgrammeIfNotExists(PrevisionResponse prevision, Long stationId) {
        if (prevision == null) {
            return 0;
        }
        
        try {
            LocalDate date = prevision.date();
            
            // In a real implementation, you would:
            // 1. Query the database for parcels associated with this station
            // 2. For each parcel, check if a programme exists for this date
            // 3. If not, create one with appropriate volume based on weather
            
            // Calculate base volume based on weather conditions
            double baseVolume = calculateBaseVolumeFromWeather(prevision);
            
            log.debug("Vérification pour création de programme auto pour la date: {} et station: {}. Volume prévu: {}", 
                date, stationId, baseVolume);
            
            // TODO: Implement actual programme creation logic
            // Example:
            // List<Parcel> parcels = parcelService.getParcelsForStation(stationId);
            // for (Parcel parcel : parcels) {
            //     if (!programmeExistsForParcelAndDate(parcel.getId(), date)) {
            //         createProgramme(parcel.getId(), date, baseVolume);
            //         return 1;
            //     }
            // }
            
            return 0;
            
        } catch (Exception e) {
            log.warn("Erreur lors de la création automatique de programme: {}", e.getMessage());
            return 0;
        }
    }

    /**
     * Calculates base irrigation volume based on weather conditions
     */
    private double calculateBaseVolumeFromWeather(PrevisionResponse prevision) {
        double baseVolume = 50.0; // Default base volume in liters
        
        if (prevision == null) {
            return baseVolume;
        }
        
        // Adjust based on rain (less rain = more irrigation needed)
        if (prevision.pluiePrevue() != null && prevision.pluiePrevue() > 0) {
            baseVolume -= prevision.pluiePrevue() * 2; // 2L reduction per mm of rain
        }
        
        // Adjust based on temperature (higher temp = more irrigation needed)
        if (prevision.temperatureMax() != null && prevision.temperatureMax() > 25.0) {
            double tempFactor = (prevision.temperatureMax() - 25.0) / 10.0;
            baseVolume += baseVolume * (tempFactor * 0.1); // Up to 10% increase per 10°C above 25°C
        }
        
        // Ensure volume stays within reasonable bounds
        return Math.max(20.0, Math.min(baseVolume, 150.0));
    }

    /**
     * Retrieves the list of configured weather station IDs from application properties.
     * Format in application.properties or application.yml:
     * app.weather.station-ids=1,2,3
     * 
     * @return List of station IDs, empty list if none configured
     */
    private List<Long> getConfiguredStationIds() {
        if (stationIdsConfig == null || stationIdsConfig.trim().isEmpty()) {
            log.debug("Aucune station météo configurée dans app.weather.station-ids");
            return List.of();
        }
        
        try {
            return java.util.Arrays.stream(stationIdsConfig.trim().split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(Long::parseLong)
                .toList();
        } catch (NumberFormatException e) {
            log.error("Format invalide pour app.weather.station-ids. Doit être une liste séparée par des virgules (ex: 1,2,3): {}", 
                stationIdsConfig, e);
            return List.of();
        }
    }
}
