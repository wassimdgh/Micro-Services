package com.example.msarrosage.scheduling;

import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.example.msarrosage.entities.JournalArrosage;
import com.example.msarrosage.entities.ProgrammeArrosage;
import com.example.msarrosage.repositories.JournalArrosageRepository;
import com.example.msarrosage.repositories.ProgrammeArrosageRepository;

/**
 * Scheduled task that automatically executes planned irrigation schedules
 * and creates execution logs when their planned time arrives.
 */
@Component
@ConditionalOnProperty(name = "spring.scheduling.enabled", havingValue = "true", matchIfMissing = true)
public class IrrigationExecutor {

    private static final Logger log = LoggerFactory.getLogger(IrrigationExecutor.class);

    private final ProgrammeArrosageRepository programmeRepo;
    private final JournalArrosageRepository journalRepo;

    public IrrigationExecutor(ProgrammeArrosageRepository programmeRepo,
                              JournalArrosageRepository journalRepo) {
        this.programmeRepo = programmeRepo;
        this.journalRepo = journalRepo;
    }

    /**
     * Automatically execute planned irrigation schedules
     * Runs every 5 minutes to check for schedules that need to be executed
     * 
     * Process:
     * 1. Find all PLANIFIE programmes where datePlanifiee <= now
     * 2. Simulate execution (in production, this would trigger real irrigation hardware)
     * 3. Create JournalArrosage entry with execution details
     * 4. Update programme status to EXECUTED
     */
    @Scheduled(cron = "0 */5 * * * *") // Every 5 minutes
    @Transactional
    public void executeScheduledIrrigations() {
        log.info("=== Checking for irrigation schedules ready to execute ===");

        try {
            LocalDateTime now = LocalDateTime.now();

            // Find all schedules that are planned and their time has arrived
            List<ProgrammeArrosage> readyToExecute = programmeRepo
                .findAll()
                .stream()
                .filter(p -> "PLANIFIE".equals(p.getStatut()))
                .filter(p -> p.getDatePlanifiee() != null && !p.getDatePlanifiee().isAfter(now))
                .toList();

            if (readyToExecute.isEmpty()) {
                log.debug("No irrigation schedules ready for execution at {}", now);
                return;
            }

            log.info("Found {} irrigation schedule(s) ready to execute", readyToExecute.size());

            for (ProgrammeArrosage programme : readyToExecute) {
                executeIrrigation(programme, now);
            }

            log.info("=== Irrigation execution completed ===");

        } catch (Exception e) {
            log.error("Error during automatic irrigation execution: ", e);
        }
    }

    /**
     * Execute a single irrigation schedule and create execution log
     */
    @Transactional
    private void executeIrrigation(ProgrammeArrosage programme, LocalDateTime executionTime) {
        String remark;
        String newStatus;
        double actualVolume;
        
        try {
            log.info("Executing irrigation for programme ID {} (Parcelle {}, planned volume {} L)",
                programme.getId(), programme.getParcelleId(), programme.getVolumePrevu());

            // In production, this would trigger actual irrigation hardware
            // For now, we simulate execution with 90% success rate
            
            boolean executionSuccess = Math.random() > 0.1; // 90% success rate
            
            if (executionSuccess) {
                // Successful execution
                // Calculate actual volume (in production, this would come from sensors)
                // For simulation, add slight variation (±5%) to planned volume
                double variation = 0.95 + (Math.random() * 0.1); // 0.95 to 1.05
                actualVolume = programme.getVolumePrevu() * variation;
                
                remark = "Executed successfully - Irrigation completed as planned";
                newStatus = "EXECUTED";
                
                log.info("Programme {} executed successfully with {} L", programme.getId(), actualVolume);
            } else {
                // Simulated failure (hardware issue, sensor error, etc.)
                actualVolume = 0.0;
                remark = "Execution failed - Hardware malfunction or system error";
                newStatus = "FAILED";
                
                log.warn("Programme {} execution failed", programme.getId());
            }

            // Create execution log
            JournalArrosage journal = JournalArrosage.builder()
                .programme(programme)
                .dateExecution(executionTime)
                .volumeReel(actualVolume)
                .remarque(remark)
                .build();

            journalRepo.save(journal);
            log.info("Created execution log ID {} for programme {} with status {}", 
                journal.getId(), programme.getId(), newStatus);

            // Update programme status
            programme.setStatut(newStatus);
            programmeRepo.save(programme);
            log.info("Updated programme {} status to {}", programme.getId(), newStatus);

        } catch (Exception e) {
            log.error("Critical error executing irrigation for programme {}: ", programme.getId(), e);
            
            // Create failure log even if exception occurred
            try {
                JournalArrosage failureJournal = JournalArrosage.builder()
                    .programme(programme)
                    .dateExecution(executionTime)
                    .volumeReel(0.0)
                    .remarque("Execution failed - System exception: " + e.getMessage())
                    .build();
                
                journalRepo.save(failureJournal);
                
                programme.setStatut("FAILED");
                programmeRepo.save(programme);
            } catch (Exception logError) {
                log.error("Failed to create failure log: ", logError);
            }
        }
    }
}
