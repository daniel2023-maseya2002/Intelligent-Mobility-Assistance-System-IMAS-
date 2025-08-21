package IMAS.ImasProject.controller;

import IMAS.ImasProject.model.MaintenanceRecord;
import IMAS.ImasProject.services.MaintenanceRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/maintenance-records")
@CrossOrigin(origins = "*")
public class MaintenanceRecordController {

    @Autowired
    private MaintenanceRecordService maintenanceRecordService;

    // Créer un nouvel enregistrement de maintenance
    @PostMapping
    public ResponseEntity<MaintenanceRecord> createMaintenanceRecord(@Valid @RequestBody MaintenanceRecord maintenanceRecord) {
        try {
            MaintenanceRecord savedRecord = maintenanceRecordService.createMaintenanceRecord(maintenanceRecord);
            return new ResponseEntity<>(savedRecord, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
    }

    // Obtenir tous les enregistrements de maintenance
    @GetMapping
    public ResponseEntity<List<MaintenanceRecord>> getAllMaintenanceRecords() {
        try {
            List<MaintenanceRecord> records = maintenanceRecordService.getAllMaintenanceRecords();
            if (records.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            }
            return new ResponseEntity<>(records, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Obtenir un enregistrement par ID
    @GetMapping("/{id}")
    public ResponseEntity<MaintenanceRecord> getMaintenanceRecordById(@PathVariable("id") Long id) {
        try {
            MaintenanceRecord record = maintenanceRecordService.getMaintenanceRecordById(id);
            return new ResponseEntity<>(record, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
    }

    // Mettre à jour un enregistrement de maintenance
    @PutMapping("/{id}")
    public ResponseEntity<MaintenanceRecord> updateMaintenanceRecord(
            @PathVariable("id") Long id,
            @Valid @RequestBody MaintenanceRecord maintenanceRecord) {
        try {
            MaintenanceRecord updatedRecord = maintenanceRecordService.updateMaintenanceRecord(id, maintenanceRecord);
            return new ResponseEntity<>(updatedRecord, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
    }

    // Supprimer un enregistrement de maintenance
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteMaintenanceRecord(@PathVariable("id") Long id) {
        try {
            maintenanceRecordService.deleteMaintenanceRecord(id);
            Map<String, String> response = new HashMap<>();
            response.put("message", "MaintenanceRecord deleted successfully");
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", "MaintenanceRecord not found");
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        }
    }

    // Rechercher par équipement
    @GetMapping("/equipment/{equipmentId}")
    public ResponseEntity<List<MaintenanceRecord>> getMaintenanceRecordsByEquipmentId(@PathVariable("equipmentId") Long equipmentId) {
        try {
            List<MaintenanceRecord> records = maintenanceRecordService.getMaintenanceRecordsByEquipmentId(equipmentId);
            return new ResponseEntity<>(records, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Rechercher par priorité
    @GetMapping("/priority/{priority}")
    public ResponseEntity<List<MaintenanceRecord>> getMaintenanceRecordsByPriority(@PathVariable("priority") MaintenanceRecord.Priority priority) {
        try {
            List<MaintenanceRecord> records = maintenanceRecordService.getMaintenanceRecordsByPriority(priority);
            return new ResponseEntity<>(records, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Rechercher par plage de dates
    @GetMapping("/date-range")
    public ResponseEntity<List<MaintenanceRecord>> getMaintenanceRecordsByDateRange(
            @RequestParam("startDate") String startDateStr,
            @RequestParam("endDate") String endDateStr) {
        try {
            LocalDate startDate = LocalDate.parse(startDateStr);
            LocalDate endDate = LocalDate.parse(endDateStr);
            List<MaintenanceRecord> records = maintenanceRecordService.getMaintenanceRecordsByDateRange(startDate, endDate);
            return new ResponseEntity<>(records, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
    }

    // Rechercher par heures maximales
    @GetMapping("/max-hours/{maxHours}")
    public ResponseEntity<List<MaintenanceRecord>> getMaintenanceRecordsByMaxHours(@PathVariable("maxHours") Integer maxHours) {
        try {
            List<MaintenanceRecord> records = maintenanceRecordService.getMaintenanceRecordsByMaxHours(maxHours);
            return new ResponseEntity<>(records, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Rechercher par mot-clé
    @GetMapping("/search")
    public ResponseEntity<List<MaintenanceRecord>> getMaintenanceRecordsByKeyword(@RequestParam("keyword") String keyword) {
        try {
            List<MaintenanceRecord> records = maintenanceRecordService.getMaintenanceRecordsByKeyword(keyword);
            return new ResponseEntity<>(records, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Obtenir les maintenances en cours
    @GetMapping("/ongoing")
    public ResponseEntity<List<MaintenanceRecord>> getOngoingMaintenanceRecords() {
        try {
            List<MaintenanceRecord> records = maintenanceRecordService.getOngoingMaintenanceRecords();
            return new ResponseEntity<>(records, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Obtenir les maintenances terminées
    @GetMapping("/completed")
    public ResponseEntity<List<MaintenanceRecord>> getCompletedMaintenanceRecords() {
        try {
            List<MaintenanceRecord> records = maintenanceRecordService.getCompletedMaintenanceRecords();
            return new ResponseEntity<>(records, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Obtenir les maintenances prévues
    @GetMapping("/scheduled")
    public ResponseEntity<List<MaintenanceRecord>> getScheduledMaintenanceRecords() {
        try {
            List<MaintenanceRecord> records = maintenanceRecordService.getScheduledMaintenanceRecords();
            return new ResponseEntity<>(records, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Recherche avancée
    @GetMapping("/advanced-search")
    public ResponseEntity<List<MaintenanceRecord>> searchMaintenanceRecords(
            @RequestParam(required = false) Long equipmentId,
            @RequestParam(required = false) MaintenanceRecord.Priority priority,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) Integer maxHours,
            @RequestParam(required = false) String keyword) {
        try {
            LocalDate parsedStartDate = startDate != null ? LocalDate.parse(startDate) : null;
            LocalDate parsedEndDate = endDate != null ? LocalDate.parse(endDate) : null;

            List<MaintenanceRecord> records = maintenanceRecordService.searchMaintenanceRecords(
                    equipmentId, priority, parsedStartDate, parsedEndDate, maxHours, keyword);
            return new ResponseEntity<>(records, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
    }

    // Compter les maintenances par priorité
    @GetMapping("/count/priority/{priority}")
    public ResponseEntity<Map<String, Long>> countMaintenanceRecordsByPriority(@PathVariable("priority") MaintenanceRecord.Priority priority) {
        try {
            Long count = maintenanceRecordService.countMaintenanceRecordsByPriority(priority);
            Map<String, Long> response = new HashMap<>();
            response.put("count", count);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Obtenir les maintenances récentes
    @GetMapping("/recent")
    public ResponseEntity<List<MaintenanceRecord>> getRecentMaintenanceRecords() {
        try {
            List<MaintenanceRecord> records = maintenanceRecordService.getRecentMaintenanceRecords();
            return new ResponseEntity<>(records, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Marquer une maintenance comme terminée
    @PutMapping("/{id}/complete")
    public ResponseEntity<MaintenanceRecord> completeMaintenanceRecord(@PathVariable("id") Long id) {
        try {
            MaintenanceRecord completedRecord = maintenanceRecordService.completeMaintenanceRecord(id);
            return new ResponseEntity<>(completedRecord, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
    }

    // Obtenir les maintenances par équipement et plage de dates
    @GetMapping("/equipment/{equipmentId}/date-range")
    public ResponseEntity<List<MaintenanceRecord>> getMaintenanceRecordsByEquipmentAndDateRange(
            @PathVariable("equipmentId") Long equipmentId,
            @RequestParam("startDate") String startDateStr,
            @RequestParam("endDate") String endDateStr) {
        try {
            LocalDate startDate = LocalDate.parse(startDateStr);
            LocalDate endDate = LocalDate.parse(endDateStr);
            List<MaintenanceRecord> records = maintenanceRecordService.getMaintenanceRecordsByEquipmentAndDateRange(
                    equipmentId, startDate, endDate);
            return new ResponseEntity<>(records, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
    }

    // Endpoint pour obtenir des statistiques générales
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getMaintenanceStatistics() {
        try {
            Map<String, Object> statistics = new HashMap<>();

            // Compter par priorité
            statistics.put("highPriorityCount", maintenanceRecordService.countMaintenanceRecordsByPriority(MaintenanceRecord.Priority.HIGH));
            statistics.put("mediumPriorityCount", maintenanceRecordService.countMaintenanceRecordsByPriority(MaintenanceRecord.Priority.MEDIUM));
            statistics.put("lowPriorityCount", maintenanceRecordService.countMaintenanceRecordsByPriority(MaintenanceRecord.Priority.LOW));

            // Compter les maintenances par statut
            statistics.put("ongoingCount", maintenanceRecordService.getOngoingMaintenanceRecords().size());
            statistics.put("completedCount", maintenanceRecordService.getCompletedMaintenanceRecords().size());
            statistics.put("scheduledCount", maintenanceRecordService.getScheduledMaintenanceRecords().size());

            // Total
            statistics.put("totalCount", maintenanceRecordService.getAllMaintenanceRecords().size());

            return new ResponseEntity<>(statistics, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}